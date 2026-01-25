#include "PIDControl.h"

// --- Instancias PID ---
PIDControl pidTemp;
PIDControl pidHum;

// --- Constantes PID ---
const float KpTemp = 2.0, KiTemp = 0.1, KdTemp = 0.5;
const float KpHum  = 1.5, KiHum  = 0.05, KdHum  = 0.4;

// --- Variables externas ---
extern float temperaturaActual;
extern float sueloPct;
extern float temperaturaObjetivo;
extern float humedadObjetivo;

// --- Variables internas ---
bool regandoPID = false;
unsigned long tCooldown = 0;
const unsigned long COOLDOWN_RIEGO = 30000;

float leerNivel();
bool verificarSensoresDuranteRiego();
bool sensoresOK();

// --- Activación bomba por PID humedad ---
void activarBombaPorPID(float salidaPID) {
  static unsigned long tEvaluacion = 0;
  static unsigned long tInicioRiego = 0;
  static unsigned long duracionRiego = 0;
  static bool bombaActiva = false;

  unsigned long ahora = millis();

  // 1. Ignorar si modo manual activo
  if (modoManual) {
    digitalWrite(CH1_IN, bombaOn ? HIGH : LOW);
    bombaActiva = bombaOn;
    mqtt.publish("invernadero/debug/bloqueo", "Modo manual activo, PID ignorado");
    return;
  }

  // 2. Verificaciones de seguridad: solo sensores críticos
  bool nivelOK = verificarSensorNivelDuranteRiego();
  bool sueloOK = verificarSensorSueloDuranteRiego();

  if (!nivelOK || !sueloOK) {
    mostrarEstadoBloqueoRiego(); // rojo
    return;
  }

  // 3. Supervisión de apagado si ya está regando
  if (regandoPID) {
    if (ahora - tInicioRiego >= duracionRiego) {
      digitalWrite(CH1_IN, LOW);
      mostrarEstadoNormal();
      regandoPID = false;
      bombaActiva = false;
      bombaOn = false;
      tCooldown = ahora;
      gestionarEvento("notificacion", "Bomba desactivada tras ciclo PID");
      mqtt.publish("invernadero/estado/bomba", "Desactivada tras ciclo PID");
    }
    return;
  }

 // 4. Evaluación PID cada 5 segundos
    if (ahora - tEvaluacion >= 5000) {
      tEvaluacion = ahora;
      duracionRiego = calcularTiempoPID(salidaPID, 5000);

      // --- Publicación para trazabilidad de gráfica ---
      unsigned long tiempoSeg = ahora / 1000;
      //Hora
      time_t now = time(nullptr);
      struct tm* timeinfo = localtime(&now);

      char fechaHora[30];
      strftime(fechaHora, sizeof(fechaHora), "%Y-%m-%d %H:%M:%S", timeinfo);
      String datos = "{";
      datos += "\"Fecha\":" + String(fechaHora) + ",";
      datos += "\"hum_actual\":" + String(sueloPct) + ",";
      datos += "\"hum_objetivo\":" + String(humedadObjetivo) + ",";
      datos += "\"tiempo_riego\":" + String(duracionRiego);
      datos += "}";
      mqtt.publish("invernadero/datosGrafica", datos.c_str());

    if (duracionRiego > 0) {
      digitalWrite(CH1_IN, HIGH);
      delay(50); // estabilización
      mostrarEstadoRiego();
      gestionarEvento("notificacion", "Bomba activada por PID");
      mqtt.publish("invernadero/estado/bomba", "Activada por PID");

      String json = "{\"hum_actual\":" + String(sueloPct) +
                    ",\"hum_objetivo\":" + String(humedadObjetivo) + "}";
      mqtt.publish("invernadero/evento/humedad", json.c_str());

      regandoPID = true;
      bombaActiva = true;
      bombaOn = true;
      tInicioRiego = ahora;
    } else {
      digitalWrite(CH1_IN, LOW);
      mostrarEstadoNormal();
      // mqtt.publish("invernadero/estado/bomba", "No activada, tiempo de riego = 0");
    }
  }
}


// --- Activación ventilador por PID temperatura ---
void activarVentiladorPorPID(float salidaPIDTemp) {
  static unsigned long instanteCalculo = 0;
  static unsigned long instanteEncendido = 0;
  static unsigned long tiempoVentilacion = 0;
  static bool ventiladorPIDActivo = false;

  unsigned long ahora = millis();

  // 1. Ignorar si modo manual activo
  if (modoManualVentilador) {
    digitalWrite(FAN_CTRL_PIN, ventiladorOn ? HIGH : LOW);
    ventiladorPIDActivo = ventiladorOn;
    mqtt.publish("invernadero/debug/bloqueo", "Modo manual ventilador activo, PID ignorado");
    return;
  }

  // 2. Verificación de seguridad: sensor DHT
  bool dhtOK = verificarSensorDHTDuranteRiego();
  if (!dhtOK) {
    mostrarEstadoBloqueoVentilacion(); // naranja
    return;
  }

  // 3. Evaluación PID cada 5 segundos
  if (ahora - instanteCalculo >= 5000) {
    instanteCalculo = ahora;

    if (salidaPIDTemp < 0) {
      if (salidaPIDTemp > -5.0) {
        tiempoVentilacion = abs(salidaPIDTemp) * 1000;
      } else {
        tiempoVentilacion = 5000;
      }

      digitalWrite(FAN_CTRL_PIN, HIGH);
      if (!ventiladorPIDActivo) {
        gestionarEvento("notificacion", "Ventilador activado por PID inverso");
        mqtt.publish("invernadero/estado/ventilador", "Activado por PID");
        ventiladorPIDActivo = true;
      }

      servoMotor.write(150);
      servoMotor2.write(150);
      tapaAbierta = true;
      mostrarEstadoVentilando(); // amarillo

      instanteEncendido = ahora;

    } else {
      tiempoVentilacion = 0;
      digitalWrite(FAN_CTRL_PIN, LOW);

      if (ventiladorPIDActivo) {
        gestionarEvento("notificacion", "Ventilador apagado por PID inverso");
        mqtt.publish("invernadero/estado/ventilador", "Apagado por PID");
        ventiladorPIDActivo = false;
      }

      servoMotor.write(0);
      servoMotor2.write(0);
      tapaAbierta = false;
      mostrarEstadoNormal(); // verde
    }
  }

  // 4. Apagado tras el tiempo de ventilación
  if (tiempoVentilacion > 0 && (ahora - instanteEncendido >= tiempoVentilacion)) {
    digitalWrite(FAN_CTRL_PIN, LOW);
    gestionarEvento("notificacion", "Ventilador apagado tras ciclo PID");
    mqtt.publish("invernadero/estado/ventilador", "Apagado tras ciclo PID");
    servoMotor.write(0);
    servoMotor2.write(0);
    tapaAbierta = false;
    tiempoVentilacion = 0;
    ventiladorPIDActivo = false;
    mostrarEstadoNormal();
  }
}



// --- Cálculo de tiempo proporcional de riego ---
unsigned long calcularTiempoPID(float salidaPID, unsigned long periodoMaximo) {
  if (salidaPID <= 0) return 0;
  if (salidaPID < 5.0) return max((unsigned long)(salidaPID * 1000), 1000UL);
  if (salidaPID < 100.0) return periodoMaximo;  // valores altos → riego máximo
  return periodoMaximo;  // incluso si se pasa, riega lo máximo permitido
}




// --- Inicialización de controladores PID ---
void inicializarPID() {
  pidTemp.configurar(KpTemp, KiTemp, KdTemp);
  pidHum.configurar(KpHum, KiHum, KdHum);
}
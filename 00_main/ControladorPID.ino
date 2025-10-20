#include "PIDControl.h"

// --- Instancias PID ---
PIDControl pidTemp;
PIDControl pidHum;

// --- Constantes PID ---
const float KpTemp = 2.0, KiTemp = 0.1, KdTemp = 0.5;
const float KpHum  = 3.0, KiHum  = 0.2, KdHum  = 0.8;

// --- Variables externas ---

extern float temperaturaActual;
extern float humedadActual;
extern float temperaturaObjetivo;
extern float humedadObjetivo;

// --- Variables  ---
bool regandoPID = false;
float leerNivel();
bool verificarSensoresDuranteRiego();

void activarBombaPorPID(float salidaPID) {
  static unsigned long tEvaluacion = 0;
  static unsigned long tInicioRiego = 0;
  static unsigned long duracionRiego = 0;
  unsigned long ahora = millis();

  // Trazabilidad base
  //mqtt.publish("invernadero/debug/activacion_pid", "Evaluando riego por PID");

  // Modo manual activo → ignorar PID
  if (modoManual) {
    mqtt.publish("invernadero/debug/bloqueo", "Modo manual activo, PID ignorado");
    digitalWrite(CH1_IN, bombaOn ? HIGH : LOW);
    return;
  }

  // Verificaciones de seguridad antes de iniciar
  if (!sensorSueloOK) {
    mqtt.publish("invernadero/debug/bloqueo", "Sensor de humedad del suelo no OK");
    gestionarEvento("alerta", "Fallo en sensor de humedad del suelo");
    return;
  }

  if (!sensorNivelOK) {
    mqtt.publish("invernadero/debug/bloqueo", "Sensor de nivel no OK");
    gestionarEvento("alerta", "Fallo en sensor de nivel del depósito");
    return;
  }

  float nivel = leerNivel();
  if (nivel < 1.0 || nivel == -1.0) {
    mqtt.publish("invernadero/debug/bloqueo", "Nivel demasiado bajo para regar");
    gestionarEvento("alerta", "No se puede regar, nivel demasiado bajo");
    return;
  }

  if (salidaPID <= 0) {
    //mqtt.publish("invernadero/debug/bloqueo", "PID indica no regar (salida <= 0)");
    return;
  }

  // Evaluación periódica cada 5 s
  if (!regandoPID && ahora - tEvaluacion >= 5000) {
    tEvaluacion = ahora;
    duracionRiego = calcularTiempoPID(salidaPID, 5000);

    String mensaje = "Condiciones OK, tiempo calculado: " + String(duracionRiego);
    mqtt.publish("invernadero/debug/activacion_pid", mensaje.c_str());

    if (duracionRiego > 0) {
      digitalWrite(CH1_IN, HIGH);
      gestionarEvento("notificacion", "Bomba activada por PID");
      mqtt.publish("invernadero/estado/bomba", "Activada por PID");
      String json = "{\"hum_actual\":" + String(humedadActual) +
              ",\"hum_objetivo\":" + String(humedadObjetivo) + "}";
      mqtt.publish("invernadero/evento/humedad", json.c_str());

      regandoPID = true;
      tInicioRiego = ahora;
    } else {
      digitalWrite(CH1_IN, LOW);
      mqtt.publish("invernadero/estado/bomba", "No activada, tiempo de riego = 0");
      gestionarEvento("notificacion", "Bomba no activada por PID");
    }
  }

  // Supervisión activa de los sensores durante el riego
  if (regandoPID) {
    if (!verificarSensoresDuranteRiego()) {
      digitalWrite(CH1_IN, LOW);
      regandoPID = false;
      gestionarEvento("alerta", "Riego por PID interrumpido por fallo de sensor");
      mqtt.publish("invernadero/estado/bomba", "Interrumpido por fallo de sensor");
      return;
    }

    // Finalizar riego por tiempo
    if (ahora - tInicioRiego >= duracionRiego) {
      digitalWrite(CH1_IN, LOW);
      regandoPID = false;
      gestionarEvento("notificacion", "Bomba desactivada tras ciclo PID");
      mqtt.publish("invernadero/estado/bomba", "Desactivada tras ciclo PID");
    }
  }
}

void activarVentiladorPorPID(float salidaPID) {
  if (modoManualVentilador) {
  digitalWrite(FAN_CTRL_PIN,ventiladorOn ? HIGH : LOW);
  return;
  }

  static unsigned long instanteAnterior = 0;
  static unsigned long tiempoVentilacion = 0;
  unsigned long ahora = millis();



  if (!sensorTempOK) {
    gestionarEvento("alerta", "Fallo en sensor de temperatura");
    return;
  }

  // Calcular tiempo de ventilación según lógica inversa
  if (ahora - instanteAnterior >= 5000) {
    instanteAnterior = ahora;

    if (salidaPID >= 0) {
      tiempoVentilacion = 0;
    } else if (salidaPID > -5.0) {
      tiempoVentilacion = abs(salidaPID) * 1000;
    } else {
      tiempoVentilacion = 5000;
    }

    if (tiempoVentilacion > 0) {
      digitalWrite(FAN_CTRL_PIN, HIGH);
      gestionarEvento("notificacion", "Ventilador activado por PID inverso");

      // Apertura de tapa
      servoMotor.write(150);
      servoMotor2.write(150);
      tapaAbierta = true;
    } else {
      digitalWrite(FAN_CTRL_PIN, LOW);
      gestionarEvento("notificacion", "Ventilador apagado por PID inverso");

      // Cierre de tapa
      servoMotor.write(0);
      servoMotor2.write(0);
      tapaAbierta = false;
    }
  }

  // Apagar ventilador cuando se cumple el tiempo proporcional
  if (ahora - instanteAnterior >= tiempoVentilacion) {
    digitalWrite(FAN_CTRL_PIN, LOW);

    // Cierre de tapa si estaba abierta
    if (tapaAbierta) {
      servoMotor.write(0);
      servoMotor2.write(0);
      tapaAbierta = false;
    }
  }
}
 
unsigned long calcularTiempoPID(float salidaPID, unsigned long periodo) {
  if (salidaPID <= 0) return 0;
  if (salidaPID < 5.0) return salidaPID * 1000;
  return periodo;
}

void inicializarPID() {
  pidTemp.configurar(KpTemp, KiTemp, KdTemp);
  pidHum.configurar(KpHum, KiHum, KdHum);
}
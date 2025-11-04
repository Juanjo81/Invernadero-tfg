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

  // Ignorar si modo manual activo
  if (modoManual) {
    digitalWrite(CH1_IN, bombaOn ? HIGH : LOW);
    if (bombaOn && !bombaActiva) {
      bombaActiva = true;
    } else if (!bombaOn && bombaActiva) {
      bombaActiva = false;
    }
    mqtt.publish("invernadero/debug/bloqueo", "Modo manual activo, PID ignorado");
    return;
  }

  // Verificaciones de seguridad
  if (!verificarSensoresDuranteRiego()) {
    mqtt.publish("invernadero/debug/bloqueo", "Fallo de sensor, riego PID bloqueado");
    gestionarEvento("alerta", "Riego PID bloqueado por fallo de sensor");
    mostrarEstadoBloqueo();
    return;
  }

  float nivel = leerNivel();
  if (nivel < 1.0 || nivel == -1.0) {
    mqtt.publish("invernadero/debug/bloqueo", "Nivel demasiado bajo para regar");
    gestionarEvento("alerta", "No se puede regar, nivel demasiado bajo");
    mostrarEstadoBloqueo();
    return;
  }

  if (salidaPID <= 0 || regandoPID) return;

  // Evaluación PID cada 5 segundos
  if (ahora - tEvaluacion >= 5000) {
    tEvaluacion = ahora;
    duracionRiego = calcularTiempoPID(salidaPID, 5000);

    mqtt.publish("invernadero/debug/activacion_pid", ("Condiciones OK, tiempo calculado: " + String(duracionRiego)).c_str());

    if (duracionRiego > 0) {
      digitalWrite(CH1_IN, HIGH);
      mostrarEstadoRiego();
      gestionarEvento("notificacion", "Bomba activada por PID");
      mqtt.publish("invernadero/estado/bomba", "Activada por PID");

      String json = "{\"hum_actual\":" + String(humedadActual) +
                    ",\"hum_objetivo\":" + String(humedadObjetivo) + "}";
      mqtt.publish("invernadero/evento/humedad", json.c_str());

      regandoPID = true;
      bombaActiva = true;
      bombaOn = true;
      tInicioRiego = ahora;
    } else {
      digitalWrite(CH1_IN, LOW);
      mostrarEstadoNormal();
      mqtt.publish("invernadero/estado/bomba", "No activada, tiempo de riego = 0");
      gestionarEvento("notificacion", "Bomba no activada por PID");
    }
  }

  // Supervisión de apagado por duración cumplida
  if (regandoPID && ahora - tInicioRiego >= duracionRiego) {
    digitalWrite(CH1_IN, LOW);
    mostrarEstadoNormal();
    regandoPID = false;
    bombaActiva = false;
    bombaOn = false;
    tCooldown = ahora;
    gestionarEvento("notificacion", "Bomba desactivada tras ciclo PID");
    mqtt.publish("invernadero/estado/bomba", "Desactivada tras ciclo PID");
  }
}

// --- Activación ventilador por PID temperatura ---
void activarVentiladorPorPID(float salidaPIDTemp) {
  if (modoManualVentilador) {
    digitalWrite(FAN_CTRL_PIN, ventiladorOn ? HIGH : LOW);
    return;
  }

  static unsigned long instanteAnterior = 0;
  static unsigned long tiempoVentilacion = 0;
  unsigned long ahora = millis();

  if (!sensorTempOK) {
    gestionarEvento("alerta", "Fallo en sensor de temperatura");
    return;
  }

  if (ahora - instanteAnterior >= 5000) {
    instanteAnterior = ahora;

    if (salidaPIDTemp >= 0) {
      tiempoVentilacion = 0;
    } else if (salidaPIDTemp > -5.0) {
      tiempoVentilacion = abs(salidaPIDTemp) * 1000;
    } else {
      tiempoVentilacion = 5000;
    }

    if (tiempoVentilacion > 0) {
      digitalWrite(FAN_CTRL_PIN, HIGH);
      gestionarEvento("notificacion", "Ventilador activado por PID inverso");
      servoMotor.write(150);
      servoMotor2.write(150);
      tapaAbierta = true;
    } else {
      digitalWrite(FAN_CTRL_PIN, LOW);
      gestionarEvento("notificacion", "Ventilador apagado por PID inverso");
      servoMotor.write(0);
      servoMotor2.write(0);
      tapaAbierta = false;
    }
  }

  if (ahora - instanteAnterior >= tiempoVentilacion) {
    digitalWrite(FAN_CTRL_PIN, LOW);
    if (tapaAbierta) {
      servoMotor.write(0);
      servoMotor2.write(0);
      tapaAbierta = false;
    }
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
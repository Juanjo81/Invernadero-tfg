#include "PIDControl.h"

// ====== VARIABLES ======
PIDControl pidTemp;
PIDControl pidHum;
const unsigned long EVALUACION_PID = 30000;
extern const float KpTemp, KiTemp, KdTemp;
extern const float KpHum, KiHum, KdHum;
extern bool tapaAbierta;
bool regandoPID = false;
unsigned long tCooldown = 0;
const unsigned long COOLDOWN_RIEGO = 75000; 

void activarBombaPorPID(float salidaPID) {
  static unsigned long tEvaluacion = 0;
  static unsigned long tInicioRiego = 0;
  static unsigned long duracionRiego = 0;
  unsigned long ahora = millis();

   if (modoManual) {
    digitalWrite(CH1_IN, bombaOn ? HIGH : LOW);
    return;
  }

  bool nivelOK = verificarSensorNivelDuranteRiego();
  bool sueloOK = verificarSensorSueloDuranteRiego();

  if (!nivelOK || !sueloOK) {
    mostrarEstadoBloqueoRiego(); 
    return;
  }

  if (regandoPID) {
    if (ahora - tInicioRiego >= duracionRiego) {
      digitalWrite(CH1_IN, LOW);
      mostrarEstadoNormal();
      regandoPID = false;
      bombaOn = false;
      tCooldown = ahora;
      gestionarEvento("notificacion", "Bomba desactivada tras ciclo PID");
    }
    return;
  }

  if (ahora - tEvaluacion >= EVALUACION_PID) {
    tEvaluacion = ahora;
    salidaPID = constrain(salidaPID, PID_MIN, PID_MAX);
    duracionRiego = calcularTiempoPID(salidaPID, EVALUACION_PID);
    const float BANDA_MUERTA = 0.3;
    if (sueloPct >= humedadObjetivo - BANDA_MUERTA) {
      duracionRiego = 0;
    }
    //Hora
    time_t now = time(nullptr);
    struct tm* timeinfo = localtime(&now);

    if (duracionRiego > 0 && (ahora - tCooldown < COOLDOWN_RIEGO)) {
      duracionRiego = 0; 
    }

    
    
    char fechaHora[30];
    strftime(fechaHora, sizeof(fechaHora), "%Y-%m-%d %H:%M:%S", timeinfo);
    String datos = "{";
    datos += "\"Fecha\":\"" + String(fechaHora) + ",";
    datos += "\"hum_actual\":" + String(sueloPct) + ",";
    datos += "\"hum_objetivo\":" + String(humedadObjetivo) + ",";
    datos += "\"tiempo_riego\":" + String(duracionRiego);
    datos += "}";
    mqtt.publish("invernadero/datosGrafica", datos.c_str());

    
    if (duracionRiego > 0) {
      digitalWrite(CH1_IN, HIGH);
      delay(50); 
      mostrarEstadoRiego();
      gestionarEvento("notificacion", "Bomba activada por PID");
      regandoPID = true;
      bombaOn = true;
      tInicioRiego = ahora;
    } else {
      digitalWrite(CH1_IN, LOW);
      mostrarEstadoNormal();
    }
  } 
}

void activarVentiladorPorPID(float salidaPIDTemp) {
  static unsigned long instanteCalculo = 0;
  static unsigned long instanteEncendido = 0;
  static unsigned long tiempoVentilacion = 0;
  static bool ventiladorPIDActivo = false;
  unsigned long ahora = millis();

  if (modoManualVentilador) {
    digitalWrite(FAN_CTRL_PIN, ventiladorOn ? HIGH : LOW);
    ventiladorPIDActivo = ventiladorOn;
    return;
  }

  bool dhtOK = verificarSensorDHTDuranteRiego();
  if (!dhtOK) {
    mostrarEstadoBloqueoVentilacion(); 
    return;
  }

  if (ahora - instanteCalculo >= EVALUACION_PID) {
    instanteCalculo = ahora;

    if (salidaPIDTemp < 0) {
      if (salidaPIDTemp > -5.0) {
        tiempoVentilacion = abs(salidaPIDTemp) * 1000;
      } else {
        tiempoVentilacion = EVALUACION_PID;
      }

      digitalWrite(FAN_CTRL_PIN, HIGH);
      if (!ventiladorPIDActivo) {
        gestionarEvento("notificacion", "Ventilador activado por PID");
        ventiladorPIDActivo = true;
      }
      servoMotor.write(150);
      servoMotor2.write(150);
      tapaAbierta = true;
      mostrarEstadoVentilando(); 
      instanteEncendido = ahora;
    } else {
      tiempoVentilacion = 0;
      digitalWrite(FAN_CTRL_PIN, LOW);

      if (ventiladorPIDActivo) {
        gestionarEvento("notificacion", "Ventilador apagado por PID ");
        ventiladorPIDActivo = false;
      }
      servoMotor.write(0);
      servoMotor2.write(0);
      tapaAbierta = false;
      mostrarEstadoNormal(); 
    }
  }

  if (tiempoVentilacion > 0 && (ahora - instanteEncendido >= tiempoVentilacion)) {
    digitalWrite(FAN_CTRL_PIN, LOW);
    gestionarEvento("notificacion", "Ventilador apagado tras ciclo PID");
    servoMotor.write(0);
    servoMotor2.write(0);
    tapaAbierta = false;
    tiempoVentilacion = 0;
    ventiladorPIDActivo = false;
    mostrarEstadoNormal();
  }
}

unsigned long calcularTiempoPID(float salidaPID, unsigned long periodoMaximo) {
  // salidaPID viene de -100..100; solo nos interesa la parte positiva
  salidaPID = constrain(salidaPID, 0.0f, 100.0f);

  unsigned long t = (unsigned long)((salidaPID / 100.0f) * (float)periodoMaximo);

  const unsigned long MIN_RIEGO_MS = 2000;
  const unsigned long MAX_RIEGO_MS = 8000;

 // if (t > 0 && t < MIN_RIEGO_MS) t = MIN_RIEGO_MS;
  if (t > MAX_RIEGO_MS) t = MAX_RIEGO_MS;
  return t;
}

void inicializarPID() {
  pidTemp.configurar(KpTemp, KiTemp, KdTemp);
  pidHum.configurar(KpHum, KiHum, KdHum);
}
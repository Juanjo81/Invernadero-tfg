#include <ESP32Servo.h>      
#include <PubSubClient.h>     
// ====== VARIABLES ======
bool bombaOn = false;
bool ventiladorOn = false;
bool ledsEncendidos = true;
bool tapaAbierta = false;
extern PubSubClient mqtt;
extern bool sistemaBloqueado;


bool modoManual = false;
bool modoManualVentilador = false;

int umbralEncender = 30;
int umbralApagar   = 35;
int umbralVentilarEncender = 40;
int umbralVentilarApagar   = 35;

unsigned long tiempoInicioRiego = 0;
const unsigned long MAX_TIEMPO_RIEGO = 30000;
bool alertaTiempoExcedido = false;

Servo servoMotor;
Servo servoMotor2;

// ====== FUNCIONES ======
void inicializarActuadores() {
  servoMotor.attach(SERVO_PIN);
  servoMotor.write(0);
  servoMotor2.attach(SERVO_PIN2);
  servoMotor2.write(0);

  pinMode(CH1_IN, OUTPUT);
  digitalWrite(CH1_IN, LOW);

  pinMode(FAN_CTRL_PIN, OUTPUT);
  digitalWrite(FAN_CTRL_PIN, LOW);

  ledcAttach(LED_R_PIN, 5000, 8);
  ledcAttach(LED_G_PIN, 5000, 8);
  ledcAttach(LED_B_PIN, 5000, 8);
  aplicarColor(0, 0, 0);
}

void bombaEncender() {
  if (sistemaBloqueado) {
    gestionarEvento("alerta", "Intento de riego manual bloqueado por sistema en estado de fallo");
    mqtt.publish("invernadero/debug/bloqueo", "Riego manual bloqueado: sistema en estado de fallo");
    mostrarEstadoBloqueo();
    return;
  }

  digitalWrite(CH1_IN, HIGH);
  gestionarEvento("notificacion", "Riego Manual Iniciado");

  bombaOn = true;
  modoManual = true;
  mostrarEstadoRiego();
}


void bombaApagar() {
  digitalWrite(CH1_IN, LOW);
  gestionarEvento("notificacion", "Riego Manual Finalizado");

  bombaOn = false;
  modoManual = false;
  if (!verificarSensoresDuranteRiego()){
    mostrarEstadoBloqueo();
  }else{
    mostrarEstadoNormal();
  }  
}
void ventiladorEncender() { digitalWrite(FAN_CTRL_PIN, HIGH); gestionarEvento("Notificacion", "Ventilacion Manual Iniciada");  ventiladorOn = true;modoManualVentilador=true;}
void ventiladorApagar()   { digitalWrite(FAN_CTRL_PIN, LOW); gestionarEvento("Notificacion", "Ventilacion Manual Detenida");  ventiladorOn = false;modoManualVentilador=false; }


/*CONTROL SIN PID
void controlarRiego(float sueloPct, float nivelPct) {
  if (!sensorSueloOK || !sensorNivelOK) {
    if (bombaOn) {
      bombaApagar();
      gestionarEvento("Alerta", "Riego detenido por fallo en sensor de nivel");
    }
    return;
  }

  // Nivel demasiado bajo
  if (nivelPct < 1.0) {
    if (bombaOn) {
      bombaApagar();
      gestionarEvento("Alerta", "Riego Detenido, nivel de depósito demasiado bajo");
    } else {      
      gestionarEvento("Alerta", "No se puede regar, nivel de depósito demasiado bajo");
    }
    return;
  }

  // Control automático
  if (!modoManual) {
    if (!bombaOn && sueloPct < umbralEncender) {
      bombaEncender();
      gestionarEvento("Notificacion", "Riego Automatico Iniciado");
      tiempoInicioRiego = millis();
      alertaTiempoExcedido = false;
    }

    if (bombaOn && sueloPct > umbralApagar) {
      bombaApagar();
      gestionarEvento("Notificacion", "Riego Automatico Detenido");
    }
  }

  // Control de tiempo máximo
  if (bombaOn && millis() - tiempoInicioRiego > MAX_TIEMPO_RIEGO && !alertaTiempoExcedido) {
    bombaApagar();
    gestionarEvento("Notificacion", "Riego Detenido por exceso de tiempo");
    alertaTiempoExcedido = true;
    tiempoInicioRiego = 0;
  }
}


void controlarVentiladores(float t) {
  if (!sensorTempOK) return;

  if (!modoManualVentilador) {
    if (!ventiladorOn && t > umbralVentilarEncender) {
      digitalWrite(FAN_CTRL_PIN, HIGH);
      ventiladorOn = true;
      servoMotor.write(150);
      servoMotor2.write(150);
      tapaAbierta = true;
      gestionarEvento("Notificacion", "Ventilacion Automatica Iniciada");
    }

    if (ventiladorOn && t < umbralVentilarApagar) {
      digitalWrite(FAN_CTRL_PIN, LOW);
      ventiladorOn = false;
      servoMotor.write(0);
      servoMotor2.write(0);
      tapaAbierta = false;
      gestionarEvento("Notificacion", "Ventilacion Automatica Detenida");
    }
  }
}*/
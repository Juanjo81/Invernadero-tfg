#include <ESP32Servo.h>      
#include <PubSubClient.h>     
// ====== VARIABLES ======
extern PubSubClient mqtt;
extern bool bombaOn;
extern bool ventiladorOn;
extern unsigned long tInicioRiegoGlobal;
bool modoManual = false;
bool modoManualVentilador = false;
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
  unsigned long ahora = millis();
  bool nivelOK = verificarSensorNivelDuranteRiego();
  bool sueloOK = verificarSensorSueloDuranteRiego();
  if (!nivelOK || !sueloOK) {
    gestionarEvento("alerta", "Intento de riego manual bloqueado por fallo en sensores críticos");
    return;
  }
  digitalWrite(CH1_IN, HIGH);
  gestionarEvento("notificacion", "Riego Manual Iniciado");
  bombaOn = true;
  modoManual = true;
  tInicioRiegoGlobal = ahora;
}

void bombaApagar() {
  digitalWrite(CH1_IN, LOW);
  gestionarEvento("notificacion", "Riego Manual Finalizado");
  bombaOn = false;
  modoManual = false;
}

void ventiladorEncender() {
  bool dhtOK = verificarSensorDHTDuranteRiego();
  if (!dhtOK) {
    gestionarEvento("alerta", "Intento de ventilación manual bloqueado por fallo en sensor DHT");
    return;
  }
  digitalWrite(FAN_CTRL_PIN, HIGH);
  gestionarEvento("notificacion", "Ventilación Manual Iniciada");
  ventiladorOn = true;
  modoManualVentilador = true;
}

void ventiladorApagar(){ 
  digitalWrite(FAN_CTRL_PIN, LOW); 
  gestionarEvento("notificacion", "Ventilacion Manual Detenida");  
  ventiladorOn = false;
  modoManualVentilador=false; 
}

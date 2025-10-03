#include <ESP32Servo.h>      
#include <PubSubClient.h>     
// ====== VARIABLES ======
bool bombaOn = false;
bool ventiladorOn = false;
bool ledsEncendidos = true;
bool tapaAbierta = false;
extern PubSubClient mqtt;

bool modoManual = false;
bool modoManualVentilador = false;

int umbralEncender = 30;
int umbralApagar   = 35;
int umbralVentilarEncender = 40;
int umbralVentilarApagar   = 35;

unsigned long tiempoInicioRiego = 0;
const unsigned long MAX_TIEMPO_RIEGO = 30000;
bool alertaTiempoExcedido = false;

int ultimoR = 255, ultimoG = 255, ultimoB = 255;

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
  aplicarColor(ultimoR, ultimoG, ultimoB);
}

void bombaEncender() { digitalWrite(CH1_IN, HIGH); gestionarEvento("Notificacion", "Riego Manual Iniciado"); bombaOn = true; modoManual=true; }
void bombaApagar()   { digitalWrite(CH1_IN, LOW); gestionarEvento("Notificacion", "Riego Manual Detenido, volviendo a modo Automatico"); bombaOn = false; modoManual=false; }
void ventiladorEncender() { digitalWrite(FAN_CTRL_PIN, HIGH); gestionarEvento("Notificacion", "Ventilacion Manual Iniciada");  ventiladorOn = true;modoManualVentilador=true;}
void ventiladorApagar()   { digitalWrite(FAN_CTRL_PIN, LOW); gestionarEvento("Notificacion", "Ventilacion Manual Detenida");  ventiladorOn = false;modoManualVentilador=false; }

void aplicarColor(int r, int g, int b) {
  ultimoR = r; ultimoG = g; ultimoB = b;
  if (ledsEncendidos) {
    ledcWrite(LED_R_PIN, r);
    ledcWrite(LED_G_PIN, g);
    ledcWrite(LED_B_PIN, b);
  } else {
    ledcWrite(LED_R_PIN, 0);
    ledcWrite(LED_G_PIN, 0);
    ledcWrite(LED_B_PIN, 0);
  }
}
bool parseHexColor(const String& s, int& r, int& g, int& b) {
  if (s.length()!=7 || s[0]!='#') return false;
  r = strtol(s.substring(1,3).c_str(), nullptr, 16);
  g = strtol(s.substring(3,5).c_str(), nullptr, 16);
  b = strtol(s.substring(5,7).c_str(), nullptr, 16);
  return true;
}
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
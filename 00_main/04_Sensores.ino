#include "DHT.h"

// ====== VARIABLES ======
bool sensorSueloOK = true;
bool sensorTempOK  = true;
bool sensorNivelOK = true;
extern PubSubClient mqtt;

unsigned long t_pub = 0;

DHT dht(DHTPIN, DHTTYPE);

void inicializarSensores() {
  pinMode(SUELO_PIN, INPUT);
  pinMode(ULTRASONIC_TRIG, OUTPUT);
  pinMode(ULTRASONIC_ECHO, INPUT);
  dht.begin();
}

float leerHumedadSuelo() {
  int raw = analogRead(SUELO_PIN);

  // Ignorar lecturas durante el primer segundo
  if (millis() < 1000) {
    return 0.0; 
  }

  if (raw < 100 || raw > 4095) {
    if (sensorSueloOK) {
      mqtt.publish("invernadero/alertas", "Fallo en sensor de humedad del suelo", true);
    }
    sensorSueloOK = false;
    return 0.0;
  }

  sensorSueloOK = true;
  float pct = 100.0 - ((raw / SUELO_SECO) * 100.0);
  return constrain(pct, 0.0, 100.0);
}


float leerTemperatura() {
  float t = dht.readTemperature();
  if (isnan(t)) {
    if (sensorTempOK) mqtt.publish("invernadero/alertas", "Fallo en sensor de temperatura", true);
    sensorTempOK = false;
    return 0.0;
  }
  sensorTempOK = true;
  return t;
}

float leerDistanciaCM() {
  digitalWrite(ULTRASONIC_TRIG, LOW);
  delayMicroseconds(2);
  digitalWrite(ULTRASONIC_TRIG, HIGH);
  delayMicroseconds(10);
  digitalWrite(ULTRASONIC_TRIG, LOW);
  long duracion = pulseIn(ULTRASONIC_ECHO, HIGH, 30000);
  return duracion * 0.034 / 2.0;
}
float calcularNivelTanque() {
  float distancia = leerDistanciaCM(); 
  if (isnan(distancia)) {
    if (sensorNivelOK) mqtt.publish("invernadero/alertas", "Fallo en sensor de Nivel del Deposito", true);
    sensorNivelOK = false;
    return 0.0;
  }
  sensorNivelOK = true;
  float nivel = 100.0 - ((distancia / ALTURA_TANQUE_CM) * 100.0);
  return constrain(nivel, 0.0, 100.0);
}
void publicarSensores(float sueloPct, float t, float h, float nivelPct) {
  if (millis() - t_pub > 5000) {
    mqtt.publish(T_SUELO_HUM, String(sueloPct, 2).c_str(), true);
    mqtt.publish(T_AIRE_TEMP, String(t, 1).c_str(), true);
    mqtt.publish(T_AIRE_HUM,  String(h, 1).c_str(), true);
    mqtt.publish(T_TANQUE_NIVEL, String(nivelPct, 1).c_str(), true);
    t_pub = millis();
  }
}
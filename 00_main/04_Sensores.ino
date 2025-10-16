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

  if (millis() < 1000) return 0.0;

  if (raw < 100 || raw > 4095) {
    if (sensorSueloOK) {
      mqtt.publish("invernadero/alertas", "Fallo en sensor de humedad del suelo", true);
    }
    sensorSueloOK = false;
    return 0.0;
  }

  sensorSueloOK = true;

  // Normalización entre mojado y seco
  float pct = ((SUELO_SECO - raw) / (SUELO_SECO - SUELO_MOJADO)) * 100.0;
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

  // Publicar la distancia cruda para diagnóstico
  //mqtt.publish("invernadero/debug/nivel/distancia_cm", String(distancia).c_str());

  if (isnan(distancia)) {
    if (sensorNivelOK) {
    //  mqtt.publish("invernadero/alertas", "Fallo en sensor de Nivel del Deposito", true);
    }
    sensorNivelOK = false;
    return 0.0;
  }

  sensorNivelOK = true;

  // Conversión a porcentaje
  float nivel = 100.0 - ((distancia / ALTURA_TANQUE_CM) * 100.0);
  nivel = constrain(nivel, 0.0, 100.0);

  // Publicar nivel calculado
  mqtt.publish("invernadero/debug/nivel_pct", String(nivel).c_str());

  return nivel;
}

float verificarSensorNivel() {
  // Activar el pulso ultrasónico
  digitalWrite(ULTRASONIC_TRIG, LOW);
  delayMicroseconds(2);
  digitalWrite(ULTRASONIC_TRIG, HIGH);
  delayMicroseconds(10);
  digitalWrite(ULTRASONIC_TRIG, LOW);

  // Medir duración del eco
  long duracion = pulseIn(ULTRASONIC_ECHO, HIGH, 30000);
  //mqtt.publish("invernadero/debug/nivel/duracion_us", String(duracion).c_str());

  // Convertir a distancia
  float distancia = duracion * 0.034 / 2.0;
  //mqtt.publish("invernadero/debug/nivel/distancia_cm", String(distancia).c_str());

  // Validar rango físico
  if (duracion == 0 || distancia < 2.0 || distancia > 100.0) {
    sensorNivelOK = false;
    mqtt.publish("invernadero/debug/nivel/estado_sensor", "FALLO");
    mqtt.publish("invernadero/alertas", "Sensor de nivel ultrasónico no responde o fuera de rango", true);
    return 0.0;
  }

  sensorNivelOK = true;
  //mqtt.publish("invernadero/debug/nivel/estado_sensor", "OK");

  // Calcular porcentaje de nivel
  const float DISTANCIA_MIN_CM = 3.0;   // tanque lleno
  const float DISTANCIA_MAX_CM = 28.0;  // tanque vacío

  float nivelPct = ((DISTANCIA_MAX_CM - distancia) / (DISTANCIA_MAX_CM - DISTANCIA_MIN_CM)) * 100.0;
  nivelPct = constrain(nivelPct, 0.0, 100.0);

  //mqtt.publish("invernadero/debug/nivel_pct", String(nivelPct).c_str());
  //mqtt.publish("invernadero/tanque/nivel", String(nivelPct).c_str());

  return nivelPct;
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

bool verificarSensoresDuranteRiego() {
  float nivel = verificarSensorNivel();
  float humedadSuelo = leerHumedadSuelo();
  float temp = leerTemperatura();
  float hum = dht.readHumidity();

  if (!sensorNivelOK || nivel < 1.0 || nivel == -1.0) {
    mqtt.publish("invernadero/debug/bloqueo", "Sensor de nivel no confiable o nivel crítico");
    gestionarEvento("alerta", "Riego interrumpido por fallo en sensor de nivel");
    return false;
  }

  if (humedadSuelo < 0.0 || humedadSuelo > 100.0) {
    mqtt.publish("invernadero/debug/bloqueo", "Sensor de humedad del suelo fuera de rango");
    gestionarEvento("alerta", "Riego interrumpido por fallo en sensor de humedad del suelo");
    return false;
  }

  if (temp < -10.0 || temp > 60.0 || hum < 0.0 || hum > 100.0) {
    mqtt.publish("invernadero/debug/bloqueo", "Sensor DHT fuera de rango");
    gestionarEvento("alerta", "Riego interrumpido por fallo en sensor de temperatura/humedad");
    return false;
  }

  return true;
}


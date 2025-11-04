#include "DHT.h"

// ====== VARIABLES ======
bool sensorSueloOK = true;
bool sensorTempOK  = true;
bool sensorNivelOK = true;

extern PubSubClient mqtt;
extern float nivelPct;
extern float humedadActual;
extern float TemperaturaActual;
extern bool modoManual;
extern bool regandoPID;
extern bool bombaOn;


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
  //mqtt.publish("invernadero/alertas", (String("valor de raw: ") + raw).c_str());


  if (raw < 100 || raw > 4094) {
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


float leerNivel() {
  // Activar el pulso ultrasónico
  digitalWrite(ULTRASONIC_TRIG, LOW);
  delayMicroseconds(2);
  digitalWrite(ULTRASONIC_TRIG, HIGH);
  delayMicroseconds(10);
  digitalWrite(ULTRASONIC_TRIG, LOW);

  // Medir duración del eco
  long duracion = pulseIn(ULTRASONIC_ECHO, HIGH, 30000);

  // Convertir a distancia
  float distancia = duracion * 0.034 / 2.0;

  // Validar rango físico
  if (duracion == 0 || distancia < 2.0 || distancia > 100.0) {
    sensorNivelOK = false;
    mqtt.publish("invernadero/debug/nivel/estado_sensor", "FALLO");
    mqtt.publish("invernadero/alertas", "Sensor de nivel ultrasónico no responde o fuera de rango", true);
    return 0.0;
  }

  // Validar fluctuación brusca
  static float distanciaAnterior = 0.0;
  if (distanciaAnterior > 0.0 && abs(distancia - distanciaAnterior) > 10.0) {
    sensorNivelOK = false;
    mqtt.publish("invernadero/debug/nivel/estado_sensor", "FLUCTUACIÓN");
    mqtt.publish("invernadero/alertas", "Lectura de nivel ultrasónico inestable", true);
    return distanciaAnterior;  // mantener valor anterior
  }

  sensorNivelOK = true;
  //mqtt.publish("invernadero/debug/nivel/estado_sensor", "OK");

  // Calcular porcentaje de nivel

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

  if (!verificarSensorNivel()) {
    if (nivelPct <= DISTANCIA_MIN_CM){
      gestionarEvento("alerta", "Riego interrumpido por nivel demasiado bajo");
    }else{
      gestionarEvento("alerta", "Riego interrumpido por fallo en sensor de nivel");
    }
    return false;
  }

  if (!verificarSensorSuelo()) {
    mqtt.publish("invernadero/debug/bloqueo", "Sensor de humedad del suelo fuera de rango");
    gestionarEvento("alerta", "Riego interrumpido por fallo en sensor de humedad del suelo");
    return false;
  }

  if (!verificarSensorDHT()) {
    mqtt.publish("invernadero/debug/bloqueo", "Sensor DHT fuera de rango");
    gestionarEvento("alerta", "Riego interrumpido por fallo en sensor de temperatura/humedad");
    return false;
  }

  return true;
}

bool verificarSensorNivel() {
  return sensorNivelOK && nivelPct >= DISTANCIA_MIN_CM && nivelPct != -1.0;
}

bool verificarSensorSuelo() {
  return sensorSueloOK && humedadActual >= 1.0 && humedadActual <= 100.0;
}

bool verificarSensorDHT() {
  return sensorTempOK && temperaturaActual > -10.0 && temperaturaActual < 60.0 ;
}

// === Control de riego activo ===
void controlarRiegoActivo() {

  unsigned long ahora = millis();

  if ((modoManual || regandoPID) && bombaOn) {
    // Apagado por tiempo máximo en modo manual
    if (modoManual && ahora - tInicioRiegoGlobal > TIEMPO_MAX_RIEGO) {
      mqtt.publish("invernadero/debug/bloqueo", "Riego manual apagado por tiempo máximo");
      gestionarEvento("alerta", "Riego manual apagado por seguridad (tiempo máximo)");
      bombaApagar();
      mostrarEstadoNormal();
      return;
    }

    // Supervisión de sensores
    if (!verificarSensoresDuranteRiego()) {
      mqtt.publish("invernadero/debug/bloqueo", "Fallo de sensor durante riego activo");
      gestionarEvento("alerta", "Riego interrumpido por fallo de sensor");
      bombaApagar();
      mostrarEstadoBloqueo();
    } else {
      // Recuperación visual si sensores están bien
    mostrarEstadoNormal();
    }
  }
}





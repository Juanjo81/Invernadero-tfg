#include "DHT.h"

// ====== VARIABLES ======
bool sensorSueloOK = true;
bool sensorTempOK  = true;
bool sensorNivelOK = true;
bool falloSensorNivel = false;
bool falloSensorSuelo = false;
bool falloSensorDHT = false;
extern PubSubClient mqtt;
extern float nivelPct;
extern float temperaturaActual;
extern float sueloPct;
extern bool modoManual;
extern bool regandoPID;
extern bool bombaOn;
extern bool ledsManual;
extern unsigned long tInicioRiegoGlobal;
const float ALPHA = 0.1;           
const float CAMBIO_MINIMO = 1.0;    
float humedadSuavizada1 = 0.0;
unsigned long t_pub = 0;
float distanciaCm = -1.0;
DHT dht(DHTPIN, DHTTYPE);

void inicializarSensores() {
  pinMode(SUELO_PIN, INPUT);  
  pinMode(SUELO_PIN2, INPUT);
  pinMode(ULTRASONIC_TRIG, OUTPUT);
  pinMode(ULTRASONIC_ECHO, INPUT);
  dht.begin();
}
int leerHumedadMediana7() {
  int v[7];

  for (int i = 0; i < 7; i++) {
    v[i] = analogRead(SUELO_PIN);
    delay(10);
  }

  ordenar(v, 7);
  return v[3];
}

void ordenar(int *v, int n) {
  for (int i = 0; i < n - 1; i++) {
    for (int j = i + 1; j < n; j++) {
      if (v[j] < v[i]) {
        int tmp = v[i];
        v[i] = v[j];
        v[j] = tmp;
      }
    }
  }
}


float leerHumedadSuelo() {
  int raw1 = leerHumedadMediana7();

  if (raw1 < 100 || raw1 > 4094) {
    sensorSueloOK = false;
    return 0.0; 
  }
  static int lecturasValidas = 0;
  lecturasValidas++;
  if (lecturasValidas >= 3) { 
    sensorSueloOK = true;
    lecturasValidas = 0; 
  }

  float pct1 = ((SUELO_SECO - raw1) / (SUELO_SECO - SUELO_MOJADO)) * 100.0;
  pct1 = constrain(pct1, 0.0, 100.0);
  humedadSuavizada1 = ALPHA * pct1 + (1.0 - ALPHA) * humedadSuavizada1;

  static float anterior1 = 0.0;
  if (abs(humedadSuavizada1 - anterior1) >= CAMBIO_MINIMO) {
    anterior1 = humedadSuavizada1;
  }
  return humedadSuavizada1;
}

float leerTemperatura() {
  float t = dht.readTemperature();
    if (isnan(t)) {
    sensorTempOK = false;
    return 0.0;
  }
  sensorTempOK = true;
  return t;
}

float leerNivel() {
  digitalWrite(ULTRASONIC_TRIG, LOW);
  delayMicroseconds(2);
  digitalWrite(ULTRASONIC_TRIG, HIGH);
  delayMicroseconds(10);
  digitalWrite(ULTRASONIC_TRIG, LOW);

  long duracion = pulseIn(ULTRASONIC_ECHO, HIGH, 30000);
  float distancia = duracion * 0.034 / 2.0;

  if (duracion == 0 || distancia < 2.0 || distancia > 100.0) {
    sensorNivelOK = false;
    distanciaCm = -1.0;
    return 0.0; 
  }
  sensorNivelOK=true;
  distanciaCm=distancia;

  nivelPct = ((DISTANCIA_MAX_CM - distanciaCm) / (DISTANCIA_MAX_CM - DISTANCIA_MIN_CM)) * 100.0;
  nivelPct = constrain(nivelPct, 0.0, 100.0);

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
bool verificarSensorNivelDuranteRiego() {
    bool sensorNivel = verificarSensorNivel();
    if (!sensorNivel) {
        if (!falloSensorNivel) {
            falloSensorNivel = true;
            if (distanciaCm >= DISTANCIA_MAX_CM - 0.5) { 
              gestionarEvento("alerta", "Riego interrumpido: depósito casi vacío");
            } else {
             gestionarEvento("alerta", "Riego interrumpido por fallo en sensor de nivel");
            }
        }
    } else {
        if (falloSensorNivel) {
            falloSensorNivel = false;
            gestionarEvento("notificacion", "Sensor de Nivel recuperado");
        }
    }
    return sensorNivel;
}

bool verificarSensorSueloDuranteRiego() {
    bool sensorSuelo = verificarSensorSuelo();
    if (!sensorSuelo) {
        if (!falloSensorSuelo) {
            falloSensorSuelo = true;
            gestionarEvento("alerta", "Riego interrumpido por fallo en sensor de humedad del suelo");
        }
    } else {
        if (falloSensorSuelo) {
            falloSensorSuelo = false;
            gestionarEvento("notificacion", "Sensor Humedad del Suelo recuperado");
        }
    }
    return sensorSuelo;
}

bool verificarSensorDHTDuranteRiego() {
    bool sensorDHT = verificarSensorDHT();
    if (!sensorDHT) {
        if (!falloSensorDHT) {
            falloSensorDHT = true;
            gestionarEvento("alerta", "Riego interrumpido por fallo en sensor de temperatura/humedad");
        }
    } else {
        if (falloSensorDHT) {
            falloSensorDHT = false;
            gestionarEvento("notificacion", "Sensor DHT recuperado");
        }
    }
    return sensorDHT;
}
bool verificarSensoresDuranteRiego() {
    bool nivelOK = verificarSensorNivelDuranteRiego();
    bool sueloOK = verificarSensorSueloDuranteRiego();
    bool dhtOK   = verificarSensorDHTDuranteRiego();
    return nivelOK && sueloOK && dhtOK;
}
bool verificarSensorNivel() {
  return sensorNivelOK && distanciaCm > 0.0 && distanciaCm <= DISTANCIA_MAX_CM;
}
bool verificarSensorSuelo() {
  return sensorSueloOK && sueloPct >= 1.0 && sueloPct <= 100.0;
}
bool verificarSensorDHT() {
  return sensorTempOK && temperaturaActual > -10.0 && temperaturaActual < 60.0 ;
}

void controlarRiegoActivo() {
  unsigned long ahora = millis();
  if ((modoManual || regandoPID) && bombaOn) {
    if (modoManual && ahora - tInicioRiegoGlobal > tiempoMaxRiego) {
      mqtt.publish("invernadero/debug/bloqueo", "Riego manual apagado por tiempo máximo");
      gestionarEvento("alerta", "Riego manual apagado por seguridad (tiempo máximo)");
      bombaApagar();
      return;
    }
  }
}
void actualizarEstadoVisual() {
  bool nivelOK = verificarSensorNivelDuranteRiego();
  bool sueloOK = verificarSensorSueloDuranteRiego();
  bool dhtOK   = verificarSensorDHTDuranteRiego();

  if (!nivelOK || !sueloOK) {
    if (!dhtOK) {
      mostrarEstadoBloqueoTotal();       
    } else {
      mostrarEstadoBloqueoRiego();       
    }
    return;
  }
  if (!dhtOK) {
    if (bombaOn) {
      aplicarColor(0, 0, 255); 
      mqtt.publish("invernadero/estado", "RIEGO+BLOQUEO_VENTILACION", true);
      estadoActual = ESTADO_RIEGO_BLOQUEO_VENTILACION;
    } else {
      mostrarEstadoBloqueoVentilacion(); 
    }
    return;
  }
  if (ledsManual) return;
  if (bombaOn && ventiladorOn) {
    mostrarEstadoRiegoYVentilando();      
  } else if (bombaOn) {
    mostrarEstadoRiego();                 
  } else if (ventiladorOn) {
    mostrarEstadoVentilando();            
  } else {
    mostrarEstadoNormal();                
  }
}

int leerADCMediana5(int pin) {
  int v[5];
  for (int i = 0; i < 5; i++) {
    v[i] = analogRead(pin);
    delay(2);
  }
  // ordenar 5 valores (burbuja simple)
  for (int i = 0; i < 4; i++) {
    for (int j = i + 1; j < 5; j++) {
      if (v[j] < v[i]) { int t = v[i]; v[i] = v[j]; v[j] = t; }
    }
  }
  return v[2]; // mediana
}










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
extern float humedadActual;
extern float TemperaturaActual;
extern float sueloPct;
extern bool modoManual;
extern bool regandoPID;
extern bool bombaOn;
extern bool ledsEncendidos;
// Parámetros de suavizado
const float ALPHA = 0.1;            // Factor de suavizado (0.1 = muy suave)
const float CAMBIO_MINIMO = 1.0;    // Umbral mínimo para publicar

// Estado interno
float humedadSuavizada1 = 0.0;
float humedadSuavizada2 = 0.0;



unsigned long t_pub = 0;

DHT dht(DHTPIN, DHTTYPE);

void inicializarSensores() {
  pinMode(SUELO_PIN, INPUT);  
  pinMode(SUELO_PIN2, INPUT);
  pinMode(ULTRASONIC_TRIG, OUTPUT);
  pinMode(ULTRASONIC_ECHO, INPUT);
  dht.begin();
}

float leerHumedadSuelo() {
  // --- 1. Lecturas crudas de los dos sensores ---
  int raw1 = analogRead(SUELO_PIN);
  int raw2 = analogRead(SUELO_PIN2);

  // Publicamos trazabilidad cruda para depuración
  mqtt.publish("invernadero/debug", (String("raw1: ") + raw1).c_str());
  mqtt.publish("invernadero/debug", (String("raw2: ") + raw2).c_str());

  // --- 2. Verificación de rango válido (salud del sensor principal) ---
  // Si el valor está fuera de rango físico, marcamos fallo inmediato
  if (raw1 < 100 || raw1 > 4094) {
    sensorSueloOK = false;
    return 0.0; // devolvemos 0 para indicar fallo
  }

  // Si el valor está dentro de rango, no recuperamos el OK de golpe:
  // usamos un contador de lecturas válidas consecutivas para evitar falsos positivos
  static int lecturasValidas = 0;
  lecturasValidas++;
  if (lecturasValidas >= 3) { // por ejemplo, 3 lecturas seguidas correctas
    sensorSueloOK = true;
    lecturasValidas = 0; // reiniciamos el contador
  }

  // --- 3. Normalización individual ---
  // Convertimos las lecturas crudas a porcentaje de humedad
  float pct1 = ((SUELO_SECO - raw1) / (SUELO_SECO - SUELO_MOJADO)) * 100.0;
  float pct2 = ((SUELO_SECO2 - raw2) / (SUELO_SECO2 - SUELO_MOJADO2)) * 100.0;

  // Limitamos los valores al rango 0–100 %
  pct1 = constrain(pct1, 0.0, 100.0);
  pct2 = constrain(pct2, 0.0, 100.0);

  // --- 4. Suavizado exponencial ---
  // Aplicamos un filtro exponencial para estabilizar las lecturas
  humedadSuavizada1 = ALPHA * pct1 + (1.0 - ALPHA) * humedadSuavizada1;
  humedadSuavizada2 = ALPHA * pct2 + (1.0 - ALPHA) * humedadSuavizada2;

  // --- 5. Publicación solo si hay cambio significativo ---
  static float anterior1 = 0.0;
  static float anterior2 = 0.0;

  if (abs(humedadSuavizada1 - anterior1) >= CAMBIO_MINIMO) {
    anterior1 = humedadSuavizada1;
    mqtt.publish("invernadero/humedadSuelo1", String(humedadSuavizada1, 1).c_str());
  }

  if (abs(humedadSuavizada2 - anterior2) >= CAMBIO_MINIMO) {
    anterior2 = humedadSuavizada2;
    mqtt.publish("invernadero/humedadSuelo2", String(humedadSuavizada2, 1).c_str());
  }

  // --- 6. Trazabilidad de humedad suavizada ---
  mqtt.publish("invernadero/debug", (String("HUMEDAD1 suavizada: ") + humedadSuavizada1).c_str());
  mqtt.publish("invernadero/debug", (String("HUMEDAD2 suavizada: ") + humedadSuavizada2).c_str());

  // --- 7. Devolvemos la humedad suavizada del sensor principal ---
  return humedadSuavizada1;
}





float leerTemperatura() {
  float t = dht.readTemperature();
  
  if (isnan(t)) {
    //if (sensorTempOK) mqtt.publish("invernadero/alertas", "Fallo en sensor de temperatura", true);
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
    if (sensorNivelOK){
      /*mqtt.publish("invernadero/debug/nivel/estado_sensor", "FALLO");
      mqtt.publish("invernadero/alertas", "Sensor de nivel ultrasónico no responde o fuera de rango", true);*/
     sensorNivelOK = false;
      
    }
    return 0.0;
  }
  sensorNivelOK=true;

  // Calcular porcentaje de nivel
  float nivelPct = ((DISTANCIA_MAX_CM - distancia) / (DISTANCIA_MAX_CM - DISTANCIA_MIN_CM)) * 100.0;
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
            if (nivelPct <= DISTANCIA_MIN_CM) {
                gestionarEvento("alerta", "Riego interrumpido por nivel demasiado bajo");
            } else {
                gestionarEvento("alerta", "Riego interrumpido por fallo en sensor de nivel");
            }
            mqtt.publish("invernadero/debug/bloqueo", "Sensor de nivel fuera de rango");
        }
    } else {
        if (falloSensorNivel) {
            falloSensorNivel = false;
            
            gestionarEvento("notificacion", "Sensor de Nivel recuperado");
            mqtt.publish("invernadero/debug/recuperado", "Sensor de nivel recuperado");
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
            mqtt.publish("invernadero/debug/bloqueo", "Sensor de humedad del suelo fuera de rango");
        }
    } else {
        if (falloSensorSuelo) {
            falloSensorSuelo = false;
            
            gestionarEvento("notificacion", "Sensor Humedad del Suelo recuperado");
            mqtt.publish("invernadero/debug/recuperado", "Sensor de humedad del suelo recuperado");
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
            mqtt.publish("invernadero/debug/bloqueo", "Sensor DHT fuera de rango");
        }
    } else {
        if (falloSensorDHT) {
            falloSensorDHT = false;
            gestionarEvento("notificacion", "Sensor DHT recuperado");
            mqtt.publish("invernadero/debug/recuperado", "Sensor DHT recuperado");
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
  return sensorNivelOK && nivelPct >= DISTANCIA_MIN_CM && nivelPct != -1.0;
}

bool verificarSensorSuelo() {
  return sensorSueloOK && sueloPct >= 1.0 && sueloPct <= 100.0;
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
     // mostrarEstadoNormal();
      return;
    }

    // Supervisión de sensores
    /*if (!verificarSensoresDuranteRiego()) {
      mqtt.publish("invernadero/debug/bloqueo", "Fallo de sensor durante riego activo");
      gestionarEvento("alerta", "Riego interrumpido por fallo de sensor");
      bombaApagar();
    //  mostrarEstadoBloqueo();
    } else {
      // Recuperación visual si sensores están bien
    //mostrarEstadoNormal();
    }*/
  }
    // Chequeo de recuperación visual cuando no hay riego
  /*if (!bombaOn && verificarSensoresDuranteRiego()) {
    mostrarEstadoNormal();  // se pone verde si todo está OK
  }*/

}

/*void actualizarEstadoVisual() {
  // Prioridad absoluta al fallo de sensores
  if (!verificarSensoresDuranteRiego()) {
    mostrarEstadoBloqueo();  // rojo
    return;
  }

  //  Si no hay fallo, respetamos el modo usuario
  if (ledsManual) return;

  //  Estado automático si no hay modo usuario
  if (bombaOn) {
    mostrarEstadoRiego();    // azul
  } else {
    mostrarEstadoNormal();   // verde
  }
}*/
void actualizarEstadoVisual() {
  bool nivelOK = verificarSensorNivelDuranteRiego();
  bool sueloOK = verificarSensorSueloDuranteRiego();
  bool dhtOK   = verificarSensorDHTDuranteRiego();
  // 1. Bloqueos de riego
  if (!nivelOK || !sueloOK) {
    if (!dhtOK) {
      mostrarEstadoBloqueoTotal();        // Magenta
    } else {
      mostrarEstadoBloqueoRiego();        // Rojo
    }
    return;
  }
  // 2. Bloqueo de ventilación (pero riego OK)
  if (!dhtOK) {
    if (bombaOn) {
      // Estado mixto: regando pero ventilación bloqueada
      aplicarColor(0, 0, 255); // Azul
      mqtt.publish("invernadero/estado", "RIEGO+BLOQUEO_VENTILACION", true);
      estadoActual = ESTADO_RIEGO_BLOQUEO_VENTILACION;
    } else {
      mostrarEstadoBloqueoVentilacion();  // Naranja
    }
    return;
  }

  // 3. Si no hay fallo, respetamos modo manual
  if (ledsManual) return;

  // 4. Estado automático
  if (bombaOn && ventiladorOn) {
    mostrarEstadoRiegoYVentilando();      // Cian
  } else if (bombaOn) {
    mostrarEstadoRiego();                 // Azul
  } else if (ventiladorOn) {
    mostrarEstadoVentilando();            // Amarillo
  } else {
    mostrarEstadoNormal();                // Verde
  }
}











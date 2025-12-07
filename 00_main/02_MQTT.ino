#include <WiFi.h>
#include <PubSubClient.h>
extern bool modoManual;
extern bool modoManualVentilador;
extern int umbralEncender;
extern int umbralApagar;
bool ledsEncendidos=true;
bool ledsManual=false;
extern int ultimoR, ultimoG, ultimoB;
extern bool tapaAbierta;
extern int umbralVentilarEncender;
extern int umbralVentilarApagar;


// ====== WIFI ======
const char* WIFI_SSID = "Tenda_1F2560";
const char* WIFI_SSID2 = "MOVISTAR_CE20";
const char* WIFI_PASS = "77777777";

// ====== MQTT ======
const char* MQTT_HOST = "192.168.1.20";
const uint16_t MQTT_PORT = 1883;
const char* MQTT_USER = "";
const char* MQTT_PASS = "";

WiFiClient espClient;
PubSubClient mqtt(espClient); 

void inicializarRed() {
  connectWiFi();
  connectMQTT();
}

void connectWiFi(){
  WiFi.mode(WIFI_STA);
  WiFi.begin(WIFI_SSID, WIFI_PASS);
  while (WiFi.status()!=WL_CONNECTED) { delay(300); }
}

void mantenerConexiones() {
    if (WiFi.status() != WL_CONNECTED) connectWiFi();
  if (!mqtt.connected()) connectMQTT();

  Serial.println("\nConectado a: " + WiFi.SSID());
  Serial.print("IP: ");
  Serial.println(WiFi.localIP());

}
void inicializarTopics(){
    //Publicar limpieza de tópicos al iniciar
  mqtt.publish("invernadero/alertas", "", true);           // Limpia alerta anterior
  mqtt.publish("invernadero/notificaciones", "", true);    // Limpia notificación anterior
  mqtt.publish("invernadero/suelo/humedad", "", true);     // Limpia lectura antigua
  mqtt.publish("invernadero/tanque/nivel", "", true);      // Limpia nivel anterior
  mqtt.publish("invernadero/bomba/state", "", true);       // Limpia estado bomba
  mqtt.publish("invernadero/led/power", "", true); 
  mqtt.publish("invernadero/debug/bloqueo", "", true); 
  mqtt.publish("invernadero/notificaciones", "", true); 
  mqtt.publish("invernadero/estado", "", true);

}

void onMqtt(char* topic, byte* payload, unsigned int len) {
  Serial.print("Topic recibido: '");
Serial.print(topic);
Serial.println("'");

  String msg; msg.reserve(len);
  for (unsigned int i = 0; i < len; i++) msg += (char)payload[i];
  msg.trim();

  if (String(topic) == T_BOMBA_CMD) {
    modoManual = true;
    if (msg.equalsIgnoreCase("ON"))  { bombaEncender(); }
    if (msg.equalsIgnoreCase("OFF")) { bombaApagar(); }
  }
  else if (String(topic) == T_FAN_CMD) {
    modoManualVentilador = true;
    if (msg.equalsIgnoreCase("ON"))  { ventiladorEncender(); }
    if (msg.equalsIgnoreCase("OFF")) { ventiladorApagar(); }
  }
  else if (String(topic) == T_OPTIMO_HUM) {
    modoManual = false;
    int v = msg.toInt();
    v = constrain(v, 0, 100);
    humedadObjetivo = v;
  }
  else if (String(topic) == T_LED_CMD) {
    int r, g, b;
    if (parseHexColor(msg, r, g, b) || (sscanf(msg.c_str(), "%d,%d,%d", &r, &g, &b) == 3)) {
      ledsEncendidos = true;
      ledsManual = true;
      aplicarColor(r, g, b);
    }
  }
  else if (String(topic) == T_LED_POWER) {
    if (msg.equalsIgnoreCase("OFF")) {
      ledsEncendidos = false;
      ledsManual = false;
      aplicarColor(ultimoR, ultimoG, ultimoB);
    }
    else if (msg.equalsIgnoreCase("ON")) {
      ledsEncendidos = true;
      ledsManual = false;
      aplicarColor(ultimoR, ultimoG, ultimoB);
    }
  }
  else if (String(topic) == T_LED_MODO) {
    if (msg.equalsIgnoreCase("USER")) { ledsManual = true; }
    else if (msg.equalsIgnoreCase("AUTO")) { ledsManual = false;  actualizarEstadoVisual(); }
  }
  else if (String(topic) == T_BOMBA_MAXIMO) {
    unsigned long tiempo = msg.toInt();
      tiempoMaxRiego = tiempo;
      Serial.print("⏱ Tiempo máximo de riego actualizado: ");
      Serial.println(tiempoMaxRiego);
    
  }
  else if (String(topic) == T_OPTIMO_TEMP) {
    modoManualVentilador = false;
    int v = msg.toInt();
    v = constrain(v, 10, 100);
    temperaturaObjetivo = v;
  }
  else if (String(topic) == T_SERVO_CMD) {
    if (msg.equalsIgnoreCase("OPEN")) {
      servoMotor.write(120);
      servoMotor2.write(120);
      tapaAbierta = true;
      Serial.println("Tapa abierta");
    }
    else if (msg.equalsIgnoreCase("CLOSE")) {
      servoMotor.write(0);
      servoMotor2.write(0);
      tapaAbierta = false;
      Serial.println("Tapa cerrada");
    }
  }
}

void connectMQTT() {
  mqtt.setServer(MQTT_HOST, MQTT_PORT);
  mqtt.setCallback(onMqtt);

  while (!mqtt.connected()) {
    if (mqtt.connect("esp32-invernadero", MQTT_USER, MQTT_PASS)) {
      // Suscripciones necesarias
      mqtt.subscribe(T_BOMBA_CMD, 1);
      mqtt.subscribe(T_BOMBA_MAXIMO, 1);      // ← añade esta
      mqtt.subscribe(T_OPTIMO_HUM, 1);
      mqtt.subscribe(T_OPTIMO_TEMP, 1);
      mqtt.subscribe(T_FAN_CMD, 1);
      mqtt.subscribe(T_LED_CMD, 1);
      mqtt.subscribe(T_LED_POWER, 1);
      mqtt.subscribe(T_LED_MODO, 1);          // ← añade esta si quieres controlar modo LED
      mqtt.subscribe(T_SERVO_CMD, 1);
    } else {
      delay(500);
    }
  }
}






#include <ESP32Servo.h>
#include <PubSubClient.h>
#include <Adafruit_SSD1306.h>
#include <DHT.h>
#include "PIDControl.h"

void inicializarActuadores();
void inicializarSensores();
void inicializarPantalla();
void inicializarRed();
void inicializarPID();
void activarBombaporPID();
void activarVentiladorporPID();
float verificarSensorNivel();
bool verificarSensoresDuranteRiego();


extern Servo servoMotor;
extern Servo servoMotor2;
extern PubSubClient mqtt;
extern Adafruit_SSD1306 display;
extern const unsigned long INTERVALO_SENSORES;
extern PubSubClient mqtt;

extern PIDControl pidTemp;
extern PIDControl pidHum;

unsigned long t_sensores = 0;
extern DHT dht;

extern float nivelPct;
extern float temperaturaActual;
extern float humedadActual;
extern float temperaturaObjetivo;
extern float humedadObjetivo;
extern bool modoManual;
extern bool bombaOn;


void setup() {
  Serial.begin(115200);

  inicializarActuadores();       // Servos, bomba, ventilador, LED RGB
  inicializarSensores();         // Pines de suelo y ultrasonido
  inicializarPantalla();         // OLED
  inicializarRed();              // WiFi + MQTT
  inicializarPID(); 
  configurarOTA("InvernaderoESP32");

}


void loop() {
  mantenerConexiones();
  mqtt.loop();
  unsigned long ahora = millis();
  gestionarOTA();

  if (ahora - t_sensores > INTERVALO_SENSORES) {
    float sueloPct = leerHumedadSuelo();
    float t = leerTemperatura();
    float h = dht.readHumidity();
    float nivelPct = verificarSensorNivel();

    temperaturaActual = t;
    humedadActual = sueloPct;

    publicarSensores(sueloPct, t, h, nivelPct);
    pintarOLED(sueloPct, t, h, nivelPct);

    // Actualizar PID
    pidTemp.actualizar(temperaturaActual, temperaturaObjetivo);
    pidHum.actualizar(humedadActual, humedadObjetivo);

    // Control proporcional por tiempo
    activarBombaPorPID(pidHum.output);
    activarVentiladorPorPID(pidTemp.output); 
/*mqtt.publish("invernadero/debug/pid/hum/output", String(pidHum.output).c_str());
mqtt.publish("invernadero/debug/hum/actual", String(humedadActual).c_str());
mqtt.publish("invernadero/debug/hum/objetivo", String(humedadObjetivo).c_str());*/
    //Control de errores para riego manual 
    if (modoManual && bombaOn) {
        if (!verificarSensoresDuranteRiego()) {
        bombaApagar();
        mqtt.publish("invernadero/debug/bloqueo", "Riego manual interrumpido por fallo de sensor");
        gestionarEvento("alerta", "Riego manual interrumpido por fallo de sensor");
      }
    }


    t_sensores = ahora;
  }
}


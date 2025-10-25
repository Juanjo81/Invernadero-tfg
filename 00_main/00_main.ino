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
float leerNivel();
bool verificarSensoresDuranteRiego();
void compruebaVersion();

extern Servo servoMotor;
extern Servo servoMotor2;
extern PubSubClient mqtt;
extern Adafruit_SSD1306 display;
extern const unsigned long INTERVALO_SENSORES;
extern PubSubClient mqtt;
extern bool regandoPID;

extern PIDControl pidTemp;
extern PIDControl pidHum;

unsigned long t_sensores = 0;
extern DHT dht;

extern float nivelPct;
extern float sueloPct;
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
  gestionarOTA();
  compruebaVersion(millis());

  unsigned long ahora = millis();

  // Control periódico de sensores
  if (ahora - t_sensores > INTERVALO_SENSORES) {
     
      sueloPct = leerHumedadSuelo();
      temperaturaActual = leerTemperatura();
      humedadActual = dht.readHumidity();
      nivelPct = leerNivel();

      // Verificación preventiva de sensores
      sistemaOK();

      publicarSensores(sueloPct, temperaturaActual, humedadActual, nivelPct);
      pintarOLED(sueloPct, temperaturaActual, humedadActual, nivelPct);

      // Actualizar PID
      pidTemp.actualizar(temperaturaActual, temperaturaObjetivo);
      pidHum.actualizar(sueloPct, humedadObjetivo);

      // Control proporcional por tiempo
      activarBombaPorPID(pidHum.output);
      activarVentiladorPorPID(pidTemp.output);

      // Control de errores durante riego manual o PID
      controlarRiegoActivo();

      t_sensores = ahora;
    }
}


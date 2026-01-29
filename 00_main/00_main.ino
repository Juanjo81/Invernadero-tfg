#include <ESP32Servo.h>
#include <PubSubClient.h>
#include <Adafruit_SSD1306.h>
#include <DHT.h>
#include "PIDControl.h"

void inicializarTopics();
void inicializarActuadores();
void inicializarSensores();
void inicializarPantalla();
void inicializarRed();
void inicializarPID();
void controlarRiegoActivo();
void activarBombaPorPID(float f);
void activarVentiladorPorPID(float f);
float leerNivel();
void compruebaVersion(unsigned long tiempoActual);
void gestionarOTA();
void configurarOTA(const char* nombreDispositivo);
void actualizarEstadoVisual();

extern PubSubClient mqtt;
extern const unsigned long INTERVALO_SENSORES;
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


void setup() {
  Serial.begin(115200);
  inicializarActuadores();     
  inicializarSensores();         
  inicializarPantalla();         
  inicializarRed();              
  inicializarTopics();
  inicializarPID(); 
  configurarOTA("InvernaderoESP32");

}


void loop() {
  mantenerConexiones();
  mqtt.loop();
  gestionarOTA();
  compruebaVersion(millis());

  unsigned long ahora = millis();
  // Control de errores durante riego manual o PID
  controlarRiegoActivo();
  actualizarEstadoVisual();
  // Control periódico de sensores
  if (ahora - t_sensores > INTERVALO_SENSORES) {
     
      sueloPct = leerHumedadSuelo();
      temperaturaActual = leerTemperatura();
      humedadActual = dht.readHumidity();
      nivelPct = leerNivel();

      publicarSensores(sueloPct, temperaturaActual, humedadActual, nivelPct);
      pintarOLED(sueloPct, temperaturaActual, humedadActual, nivelPct);

      // Actualizar PID
      pidTemp.actualizar(temperaturaActual, temperaturaObjetivo);
      pidHum.actualizar(sueloPct, humedadObjetivo);

      // Control proporcional por tiempo
      activarBombaPorPID(pidHum.output);
      activarVentiladorPorPID(pidTemp.output);

      t_sensores = ahora;
    }
}


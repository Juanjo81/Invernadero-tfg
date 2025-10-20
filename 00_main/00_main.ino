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




  unsigned long ahora = millis();

  // ⏱ Control periódico de sensores
  if (ahora - t_sensores > INTERVALO_SENSORES) {
      // ⛑ Verificación preventiva de sensores
  if (!sistemaOK()) mostrarEstadoBloqueo();
    float sueloPct = leerHumedadSuelo();
    float t = leerTemperatura();
    float h = dht.readHumidity();
    float nivelPct = leerNivel();

    temperaturaActual = t;
    humedadActual = sueloPct;

    publicarSensores(sueloPct, t, h, nivelPct);
    pintarOLED(sueloPct, t, h, nivelPct);

    // 🔁 Actualizar PID
    pidTemp.actualizar(temperaturaActual, temperaturaObjetivo);
    pidHum.actualizar(humedadActual, humedadObjetivo);

    // ⚙️ Control proporcional por tiempo
    activarBombaPorPID(pidHum.output);
    activarVentiladorPorPID(pidTemp.output);

    // 🛑 Control de errores durante riego manual o PID
    if ((modoManual || regandoPID) && bombaOn) {
      if (!verificarSensoresDuranteRiego()) {
        mqtt.publish("invernadero/debug/bloqueo", "Fallo de sensor durante riego activo");
        bombaApagar();
        gestionarEvento("alerta", "Riego interrumpido por fallo de sensor");
        mostrarEstadoBloqueo();
      }
    }

    // 🧠 Lógica de parada por objetivo o fallo
    if (regandoPID) {
      if (humedadActual >= humedadObjetivo || !verificarSensoresDuranteRiego()) {
        bombaApagar();
        gestionarEvento("notificacion", "Riego PID detenido por objetivo o fallo");
        mqtt.publish("invernadero/debug/parada_pid", "Riego detenido: humedad alcanzada o fallo");

        if (!verificarSensoresDuranteRiego()) {
          mostrarEstadoBloqueo();
        } else {
          mostrarEstadoNormal();
        }
      }
    }

    t_sensores = ahora;
  }
}


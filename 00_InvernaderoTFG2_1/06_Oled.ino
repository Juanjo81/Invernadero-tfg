#include <Adafruit_SSD1306.h>
#include <Adafruit_GFX.h>

// ====== VARIABLES ======
#define SCREEN_WIDTH 128
#define SCREEN_HEIGHT 64
extern PubSubClient mqtt;

Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, -1);

// ====== OLED ======
void inicializarPantalla() {
  display.begin(SSD1306_SWITCHCAPVCC, 0x3C);
}

void pintarOLED(float humSueloPct, float t, float h,float nivelPct) {

  static unsigned long t_oled = 0;
  const unsigned long INTERVALO_OLED = 1000;

  if (millis() - t_oled < INTERVALO_OLED) return;
  t_oled = millis();

  // Solo refresca si hay conexión
  if (!WiFi.isConnected() && !mqtt.connected()) return;

  display.clearDisplay();
  display.setTextSize(1);
  display.setTextColor(SSD1306_WHITE);

  display.setCursor(0,0);
  display.print("WiFi: "); display.print(WiFi.isConnected()?"OK":"X");
  display.print("  MQTT: "); display.print(mqtt.connected()?"OK":"X");

  display.setCursor(0,12);
  display.print("Suelo: "); display.print(humSueloPct,2); display.println("%");

  display.setCursor(0,24);
  display.print("Nivel Deposito: "); display.print(nivelPct); display.print("%");

  display.setCursor(0,36);
  display.print("Bomba: "); display.print(bombaOn ? "ON" : "OFF");

  display.setCursor(0,48);
  display.print("Aire: ");
  display.print(t,1); display.print("C ");
  display.print(h,1); display.print("%");

  display.display();
}
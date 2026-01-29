unsigned long t_alerta = 0;
extern PubSubClient mqtt;
extern Adafruit_SSD1306 display;

void gestionarEvento(String tipo, String mensaje) {
  if (tipo == "alerta") {
    t_alerta = millis();
    mqtt.publish(T_ALERTAS, mensaje.c_str(), true);
    display.clearDisplay();
    display.setCursor(0, 0);
    display.print("⚠ ALERTA ⚠");
    display.setCursor(0, 12);
    display.print(mensaje);
    display.display();
  }

  else if (tipo == "notificacion") {
    mqtt.publish(T_NOTIFICACIONES, mensaje.c_str(), true);
    display.clearDisplay();
    display.setCursor(0, 0);
    display.print("ℹ️ Notificación");
    display.setCursor(0, 12);
    display.print(mensaje);
    display.display();
  }

}

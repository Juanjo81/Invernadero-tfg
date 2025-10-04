String mensajeEvento = "";
bool alertaActiva = false;
int parpadeosRestantes = 0;
unsigned long t_alerta = 0;
const unsigned long INTERVALO_PARPADEO = 300;
bool estadoLedAlerta = false;
extern PubSubClient mqtt;

void gestionarEvento(String tipo, String mensaje) {
  mensajeEvento = mensaje;

  if (tipo == "Alerta") {
    alertaActiva = true;
    parpadeosRestantes = 10;
    t_alerta = millis();

    mqtt.publish(T_ALERTAS, mensaje.c_str(), true);

    display.clearDisplay();
    display.setCursor(0, 0);
    display.print("⚠ ALERTA ⚠");
    display.setCursor(0, 12);
    display.print(mensaje);
    display.display();
  }

  else if (tipo == "Notificacion") {
    mqtt.publish(T_NOTIFICACIONES, mensaje.c_str(), true);

    display.clearDisplay();
    display.setCursor(0, 0);
    display.print("ℹ️ Notificación");
    display.setCursor(0, 12);
    display.print(mensaje);
    display.display();
  }
}
  void actualizarEventos() {
  if (alertaActiva && parpadeosRestantes > 0) {
    unsigned long ahora = millis();
    if (ahora - t_alerta >= INTERVALO_PARPADEO) {
      t_alerta = ahora;
      estadoLedAlerta = !estadoLedAlerta;
      digitalWrite(LED_R_PIN, estadoLedAlerta ? HIGH : LOW);
      parpadeosRestantes--;
    }
    if (parpadeosRestantes == 0) {
      alertaActiva = false;
      digitalWrite(LED_R_PIN, LOW);
    }
  }
}

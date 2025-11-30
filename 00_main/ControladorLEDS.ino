// ====== VARIABLES ======
int ultimoR = 255, ultimoG = 255, ultimoB = 255;


// ====== FUNCIONES ======
void inicializarLEDs() {
  ledcAttach(LED_R_PIN, 5000, 8);
  ledcAttach(LED_G_PIN, 5000, 8);
  ledcAttach(LED_B_PIN, 5000, 8);
  aplicarColor(0, 0, 0);
}
void mostrarEstadoRiego() {
  if (estadoActual != ESTADO_RIEGO) {
    aplicarColor(0, 0, 255); // Azul
    mqtt.publish("invernadero/estado", "RIEGO");
    estadoActual = ESTADO_RIEGO;
  }
}

void mostrarEstadoBloqueo() {
  if (estadoActual != ESTADO_BLOQUEO) {
    aplicarColor(255, 0, 0); // Rojo
    mqtt.publish("invernadero/estado", "BLOQUEO");
    estadoActual = ESTADO_BLOQUEO;
  }
}

void mostrarEstadoNormal() {
  if (estadoActual != ESTADO_OK) {
    aplicarColor(0, 255, 0); // Verde
    mqtt.publish("invernadero/estado", "OK");
    estadoActual = ESTADO_OK;
  }
}

void aplicarColor(int r, int g, int b) {
  ultimoR = r; ultimoG = g; ultimoB = b;
  if (ledsEncendidos) {
    ledcWrite(LED_R_PIN, r);
    ledcWrite(LED_G_PIN, g);
    ledcWrite(LED_B_PIN, b);
  } else {
    ledcWrite(LED_R_PIN, 0);
    ledcWrite(LED_G_PIN, 0);
    ledcWrite(LED_B_PIN, 0);
  }
}
bool parseHexColor(const String& s, int& r, int& g, int& b) {
  if (s.length()!=7 || s[0]!='#') return false;
  r = strtol(s.substring(1,3).c_str(), nullptr, 16);
  g = strtol(s.substring(3,5).c_str(), nullptr, 16);
  b = strtol(s.substring(5,7).c_str(), nullptr, 16);
  return true;
}
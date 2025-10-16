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
  aplicarColor(0, 0, 255); // Azul
}

void mostrarEstadoBloqueo() {
  aplicarColor(255, 0, 0); // Rojo
}

void mostrarEstadoNormal() {
  aplicarColor(0, 255, 0); // Verde
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
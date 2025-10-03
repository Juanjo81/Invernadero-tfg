#include <ArduinoOTA.h>

void configurarOTA(const char* nombreDispositivo) {
  ArduinoOTA.setHostname(nombreDispositivo);

  ArduinoOTA.onStart([]() {
    Serial.println("[OTA] Inicio de actualización");
  });

  ArduinoOTA.onEnd([]() {
    Serial.println("[OTA] Actualización completada");
  });

  ArduinoOTA.onProgress([](unsigned int progress, unsigned int total) {
    Serial.printf("[OTA] Progreso: %u%%\r\n", (progress * 100) / total);
  });

  ArduinoOTA.onError([](ota_error_t error) {
    Serial.printf("[OTA] Error [%u]: ", error);
    switch (error) {
      case OTA_AUTH_ERROR: Serial.println("Fallo de autenticación"); break;
      case OTA_BEGIN_ERROR: Serial.println("Fallo al comenzar"); break;
      case OTA_CONNECT_ERROR: Serial.println("Fallo de conexión"); break;
      case OTA_RECEIVE_ERROR: Serial.println("Fallo al recibir"); break;
      case OTA_END_ERROR: Serial.println("Fallo al finalizar"); break;
    }
  });

  ArduinoOTA.begin();
}

void gestionarOTA() {
  ArduinoOTA.handle();
}
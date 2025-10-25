#include <ArduinoOTA.h>

#include <HTTPClient.h>
#include <HTTPUpdate.h>  


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

void compruebaVersion() {
  HTTPClient http;
  http.begin("http://192.168.1.30/index.php/s/ic2k8aePcapxdAn/download/Version.txt");
  int httpCode = http.GET();

  if (httpCode == 200) {
    String versionRemota = http.getString();
    versionRemota.trim();

    if (esVersionSuperior(versionRemota, VERSION_FIRMWARE)) {
      mqtt.publish("invernadero/estado/firmware", "Nueva versión disponible: " + versionRemota);
      WiFiClient client;
      httpUpdate.update(client, "http://192.168.1.30/index.php/s/ic2k8aePcapxdAn/download/firmware.bin");
    } else {
      mqtt.publish("invernadero/estado/firmware", "Firmware actualizado: " + VERSION_FIRMWARE);
    }
  } else {
    mqtt.publish("invernadero/estado/firmware", "Error al comprobar versión remota");
  }

  http.end();
}



bool esVersionSuperior(String remota, String local) {
  int rMayor, rMenor, rPatch;
  int lMayor, lMenor, lPatch;

  sscanf(remota.c_str(), "%d.%d.%d", &rMayor, &rMenor, &rPatch);
  sscanf(local.c_str(), "%d.%d.%d", &lMayor, &lMenor, &lPatch);

  if (rMayor > lMayor) return true;
  if (rMayor == lMayor && rMenor > lMenor) return true;
  if (rMayor == lMayor && rMenor == lMenor && rPatch > lPatch) return true;

  return false;
}



#include <ArduinoOTA.h>
#include <WiFiClientSecure.h>

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

void compruebaVersion(unsigned long ahora) {
  static unsigned long t_version = 0;
  const unsigned long INTERVALO_VERSION = 86400000; // 24 horas

  // Ejecutar la primera vez o si ha pasado el intervalo
  if (t_version == 0 || ahora - t_version >= INTERVALO_VERSION) {
    t_version = ahora;

    if (WiFi.status() != WL_CONNECTED) {
      mqtt.publish("invernadero/estado/firmware", "WiFi no conectado");
      return;
    }

    WiFiClientSecure client;
    client.setInsecure();  

    HTTPClient http;
    http.setFollowRedirects(HTTPC_FORCE_FOLLOW_REDIRECTS);
    http.begin(client, "https://192.168.1.30/index.php/s/ic2k8aePcapxdAn/download/Version.txt");

    int httpCode = http.GET();
    mqtt.publish("invernadero/debug/http", ("Código HTTP: " + String(httpCode)).c_str());

    if (httpCode == 200) {
      String versionRemota = http.getString();
      versionRemota.trim();

      if (esVersionSuperior(versionRemota, VERSION_FIRMWARE)) {
        String mensaje = "Nueva versión disponible: " + versionRemota + " | Actual: " + VERSION_FIRMWARE;
        mqtt.publish("invernadero/estado/firmware", mensaje.c_str());

        WiFiClientSecure updateClient;
        updateClient.setInsecure();

        t_httpUpdate_return resultado = httpUpdate.update(updateClient, "https://192.168.1.30/index.php/s/ppKr4QSsabjcYam/download/firmware.bin");

        if (resultado == HTTP_UPDATE_OK) {
          mqtt.publish("invernadero/estado/firmware", "Actualización OTA completada con éxito. Reiniciando...");
        } else {
          String errorOTA = "Error en la actualización OTA. Código: " + String(resultado);
          mqtt.publish("invernadero/estado/firmware", errorOTA.c_str());
        }

      } else {
        String mensaje = "Versión remota: " + versionRemota + " | Actual: " + VERSION_FIRMWARE + " → No se actualiza";
        mqtt.publish("invernadero/estado/firmware", mensaje.c_str());
      }

    } else {
      String errorHTTP = "Error al comprobar versión remota. Código HTTP: " + String(httpCode);
      mqtt.publish("invernadero/estado/firmware", errorHTTP.c_str());
    }

    http.end();
  }
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



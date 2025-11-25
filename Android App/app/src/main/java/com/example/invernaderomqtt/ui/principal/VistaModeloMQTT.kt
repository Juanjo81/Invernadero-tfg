package com.example.invernaderomqtt.ui.principal

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.example.invernaderomqtt.data.eventos.EventoBD
import com.example.invernaderomqtt.data.eventos.RepositorioEventos
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.MqttGlobalPublishFilter
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mostrarNotificacion
import java.nio.charset.StandardCharsets

class VistaModeloMQTT : ViewModel() {

    private lateinit var clienteMQTT: Mqtt3AsyncClient

    private val _conectadoMQTT = MutableStateFlow(false)
    val conectadoMQTT: StateFlow<Boolean> = _conectadoMQTT

    private val _direccionIP = MutableStateFlow("invernaderotfg2.duckdns.org")
    val direccionIP: StateFlow<String> = _direccionIP

    private val _temperaturaAire = MutableStateFlow("0.0")
    val temperaturaAire: StateFlow<String> = _temperaturaAire

    private val _humedadAire = MutableStateFlow("0.0")
    val humedadAire: StateFlow<String> = _humedadAire

    private val _humedadSuelo = MutableStateFlow("0.0")
    val humedadSuelo: StateFlow<String> = _humedadSuelo
    private val _temperaturaObjetivo = MutableStateFlow(25f)
    val temperaturaObjetivo: StateFlow<Float> = _temperaturaObjetivo

    private val _humedadObjetivo = MutableStateFlow(60f)
    val humedadObjetivo: StateFlow<Float> = _humedadObjetivo

    private val _nivelTanque = MutableStateFlow("0.0")
    val nivelTanque: StateFlow<String> = _nivelTanque

    private val _riegoEncendido = MutableStateFlow(false)
    val riegoEncendido: StateFlow<Boolean> = _riegoEncendido

    private val _ventilacionEncendida = MutableStateFlow(false)
    val ventilacionEncendida: StateFlow<Boolean> = _ventilacionEncendida

    private val _puertaAbierta = MutableStateFlow(false)
    val puertaAbierta: StateFlow<Boolean> = _puertaAbierta

    private val _bombillaEncendida = MutableStateFlow(false)
    val bombillaEncendida: StateFlow<Boolean> = _bombillaEncendida

    private val _colorBombilla = MutableStateFlow(Color.White)
    val colorBombilla: StateFlow<Color> = _colorBombilla

    fun inicializarMQTT(context: Context) {
        clienteMQTT = MqttClient.builder()
            .useMqttVersion3()
            .serverHost(_direccionIP.value)
            .serverPort(1883)
            .identifier("invernaderoApp")
            .buildAsync()

        clienteMQTT.connect().whenComplete { _, error ->
            if (error == null) {
                _conectadoMQTT.value = true
                suscribirseATopics(context)
            } else {
                Log.e("MQTT", "Error al conectar", error)
                _conectadoMQTT.value = false
            }
        }
    }

    private fun suscribirseATopics(context: Context) {
        val topics = listOf(
            "invernadero/aire/temperatura",
            "invernadero/aire/humedad",
            "invernadero/suelo/humedad",
            "invernadero/bomba/state",
            "invernadero/tanque/nivel",
            "invernadero/led/power",
            "invernadero/alertas",
            "invernadero/notificaciones",
            "invernadero/objetivos/temperatura",
            "invernadero/objetivos/humedad"
        )

        clienteMQTT.publishes(MqttGlobalPublishFilter.ALL) { mensaje ->
            val topic = mensaje.topic.toString()
            val payload = mensaje.payload.map { buffer ->
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)
                String(bytes, StandardCharsets.UTF_8)
            }.orElse(null) ?: return@publishes

            Log.d("MQTT", "Mensaje recibido en $topic: $payload")

            when (topic) {
                "invernadero/aire/temperatura" -> _temperaturaAire.value = payload
                "invernadero/aire/humedad" -> _humedadAire.value = payload
                "invernadero/suelo/humedad" -> _humedadSuelo.value = payload
                "invernadero/tanque/nivel" -> _nivelTanque.value = payload
                "invernadero/bomba/state" -> _riegoEncendido.value = payload == "ON"
                "invernadero/led/power" -> _bombillaEncendida.value = payload == "ON"
                "invernadero/objetivos/temperatura" -> _temperaturaObjetivo.value =
                    payload.toFloatOrNull() ?: _temperaturaObjetivo.value

                "invernadero/objetivos/humedad" -> _humedadObjetivo.value =
                    payload.toFloatOrNull() ?: _humedadObjetivo.value

                "invernadero/alertas" -> {
                    mostrarNotificacion(context, "Alerta del invernadero", payload)

                    CoroutineScope(Dispatchers.IO).launch {
                        val dao = EventoBD.obtener(context).eventoDao()
                        val repo = RepositorioEventos(dao)
                        repo.registrarEvento("alerta", payload, "invernadero/alertas")
                    }
                }


                "invernadero/notificaciones" -> {
                    mostrarNotificacion(context, "Notificación", payload)

                    CoroutineScope(Dispatchers.IO).launch {
                        val dao = EventoBD.obtener(context).eventoDao()
                        val repo = RepositorioEventos(dao)
                        repo.registrarEvento("info", payload, "invernadero/notificaciones")
                    }
                }
            }
        }

        topics.forEach { topic ->
            clienteMQTT.subscribeWith()
                .topicFilter(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .send()
        }
    }


    private fun publicar(topic: String, mensaje: String) {
        clienteMQTT.publishWith()
            .topic(topic)
            .payload(mensaje.toByteArray(StandardCharsets.UTF_8))
            .send()
        Log.d("MQTT", "Publicado en $topic: $mensaje")
    }
    fun setTemperaturaObjetivo(valor: Float) {
        _temperaturaObjetivo.value = valor
        publicar("invernadero/optimo/temperatura", valor.toString())
    }

    fun setHumedadObjetivo(valor: Float) {
        _humedadObjetivo.value = valor
        publicar("invernadero/optimo/humedad", valor.toString())
    }

    fun alternarRiego() {
        val nuevoEstado = !_riegoEncendido.value
        _riegoEncendido.value = nuevoEstado
        publicar("invernadero/bomba/cmd", if (nuevoEstado) "ON" else "OFF")
    }

    fun alternarVentilacion() {
        val nuevoEstado = !_ventilacionEncendida.value
        _ventilacionEncendida.value = nuevoEstado
        publicar("invernadero/ventiladores/cmd", if (nuevoEstado) "ON" else "OFF")
    }

    fun alternarPuerta() {
        val nuevoEstado = !_puertaAbierta.value
        _puertaAbierta.value = nuevoEstado
        publicar("invernadero/servomotor1/cmd", if (nuevoEstado) "OPEN" else "CLOSE")
    }

    fun alternarLuz() {
        val nuevoEstado = !_bombillaEncendida.value
        _bombillaEncendida.value = nuevoEstado
        publicar("invernadero/led/power", if (nuevoEstado) "ON" else "OFF")
    }

    fun publicarColorBombilla(color: Color) {
        _colorBombilla.value = color
        publicar("invernadero/led/cmd", color.aHex())
    }

}

fun Color.aHex(): String {
    val r = (red * 255).toInt()
    val g = (green * 255).toInt()
    val b = (blue * 255).toInt()
    return String.format("#%02X%02X%02X", r, g, b)
}
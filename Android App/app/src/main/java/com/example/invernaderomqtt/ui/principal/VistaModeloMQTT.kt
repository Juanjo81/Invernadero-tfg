package com.example.invernaderomqtt.ui.principal

import android.content.Context
import android.util.Log
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
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import mostrarNotificacion
import java.nio.charset.StandardCharsets

class VistaModeloMQTT : ViewModel() {

    private lateinit var clienteMQTT: Mqtt3AsyncClient

    // Estado de conexión
    private val _conectadoMQTT = MutableStateFlow(false)
    val conectadoMQTT: StateFlow<Boolean> = _conectadoMQTT

    // Último mensaje recibido (para watchdog)
    private var ultimoMensajeRecibido = System.currentTimeMillis()

    // Variables de estado
    private val _direccionIP = MutableStateFlow("invernaderotfg2.duckdns.org")
    val direccionIP: StateFlow<String> = _direccionIP

    private val _temperaturaAire = MutableStateFlow("0.0")
    val temperaturaAire: StateFlow<String> = _temperaturaAire

    private val _humedadAire = MutableStateFlow("0.0")
    val humedadAire: StateFlow<String> = _humedadAire

    private val _humedadSuelo = MutableStateFlow("0.0")
    val humedadSuelo: StateFlow<String> = _humedadSuelo

    private val _temperaturaObjetivo = MutableStateFlow(60f)
    val temperaturaObjetivo: StateFlow<Float> = _temperaturaObjetivo

    private val _humedadObjetivo = MutableStateFlow(0f)
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


    private val _tiempoMaxRiego = MutableStateFlow(5f)
    val tiempoMaxRiego: StateFlow<Float> = _tiempoMaxRiego


    private val _estado = MutableStateFlow("OK")
    val estadoVisual: StateFlow<String> = _estado

    // --- Inicialización del cliente MQTT ---
    fun inicializarMQTT(context: Context) {
        clienteMQTT = MqttClient.builder()
            .useMqttVersion3()
            .serverHost(_direccionIP.value)
            .serverPort(1883)
            .identifier("invernaderoApp")
            .buildAsync()

        clienteMQTT.connectWith()
            .keepAlive(30)
            .cleanSession(false)
            .send()
            .whenComplete { _, error ->
                if (error == null) {
                    _conectadoMQTT.value = true
                    suscribirseATopics(context)
                    Log.d("MQTT", "Conectado correctamente a ${_direccionIP.value}")
                } else {
                    Log.e("MQTT", "Error al conectar a ${_direccionIP.value}", error)
                    _conectadoMQTT.value = false
                }
            }
    }



    // --- Suscripción a topics ---
    private fun suscribirseATopics(context: Context) {
        val topics = listOf(
            "invernadero/aire/temperatura",
            "invernadero/aire/humedad",
            "invernadero/suelo/humedad",
            "invernadero/bomba/estado",
            "invernadero/bomba/max",
            "invernadero/tanque/nivel",
            "invernadero/led/power",
            "invernadero/led/cmd",
            "invernadero/led/mode",
            "invernadero/alertas",
            "invernadero/notificaciones",
            "invernadero/optimo/temperatura",
            "invernadero/optimo/humedad",
        )

        clienteMQTT.publishes(MqttGlobalPublishFilter.ALL) { mensaje ->
            ultimoMensajeRecibido = System.currentTimeMillis() // watchdog actualizado

            val topic = mensaje.topic.toString()
            val mensajeV3 = mensaje as? Mqtt3Publish
            val retained = mensajeV3?.isRetain ?: false

            val payload = mensaje.payload.map { buffer ->
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)
                String(bytes, StandardCharsets.UTF_8)
            }.orElse(null) ?: return@publishes

            Log.d("MQTT", "Mensaje recibido en $topic: $payload (retained=$retained)")

            // Ignorar mensajes retained en eventos
            if ((topic == "invernadero/notificaciones" || topic == "invernadero/alertas") && retained) {
                Log.d("MQTT", "Ignorado mensaje retained en $topic: $payload")
                return@publishes
            }

            when (topic) {
                "invernadero/aire/temperatura" -> _temperaturaAire.value = payload.toFloatOrNull()?.toString() ?: "0.0"
                "invernadero/aire/humedad" -> _humedadAire.value = payload.toFloatOrNull()?.toString() ?: "0.0"
                "invernadero/suelo/humedad" -> _humedadSuelo.value = payload.toFloatOrNull()?.toString() ?: "0.0"
                "invernadero/tanque/nivel" -> _nivelTanque.value = payload.toFloatOrNull()?.toString() ?: "0.0"
                "invernadero/bomba/estado" -> _riegoEncendido.value = payload == "ON"
                "invernadero/bomba/max" -> _tiempoMaxRiego.value = payload.toFloatOrNull() ?: _tiempoMaxRiego.value
                "invernadero/led/power" -> _bombillaEncendida.value = payload == "ON"
                "invernadero/led/cmd" -> _colorBombilla.value = Color(android.graphics.Color.parseColor(payload))
                "invernadero/optimo/temperatura" -> _temperaturaObjetivo.value = payload.toFloatOrNull() ?: _temperaturaObjetivo.value
                "invernadero/optimo/humedad" -> _humedadObjetivo.value = payload.toFloatOrNull() ?: _humedadObjetivo.value

                "invernadero/alertas" -> {
                    mostrarNotificacion(context, "Alerta del invernadero", payload)
                    CoroutineScope(Dispatchers.IO).launch {
                        val dao = EventoBD.obtener(context).eventoDao()
                        val repo = RepositorioEventos(dao)
                        repo.registrarEvento("alerta", payload, topic)
                    }
                }

                "invernadero/notificaciones" -> {
                    mostrarNotificacion(context, "Notificación", payload)
                    CoroutineScope(Dispatchers.IO).launch {
                        val dao = EventoBD.obtener(context).eventoDao()
                        val repo = RepositorioEventos(dao)
                        repo.registrarEvento("info", payload, topic)
                    }
                }


            }
        }



    topics.forEach { topic ->
            clienteMQTT.subscribeWith()
                .topicFilter(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .send()
                .whenComplete { _, error ->
                    if (error != null) {
                        Log.e("MQTT", "Error al suscribirse a $topic", error)
                        _conectadoMQTT.value = false
                    }
                }
        }
    }

    // --- Publicación robusta con verificación ---
    private fun publicar(topic: String, mensaje: String) {
        clienteMQTT.publishWith()
            .topic(topic)
            .payload(mensaje.toByteArray(StandardCharsets.UTF_8))
            .send()
            .whenComplete { _, error ->
                if (error != null) {
                    Log.e("MQTT", "Error al publicar en $topic", error)
                } else {
                    Log.d("MQTT", "Publicado en $topic: $mensaje")
                }
            }
    }


    // --- Métodos de control ---
    fun setTemperaturaObjetivo(valor: Float) {
        _temperaturaObjetivo.value = valor
        publicar("invernadero/optimo/temperatura", valor.toString())
    }

    fun setHumedadObjetivo(valor: Float) {
        _humedadObjetivo.value = valor
        publicar("invernadero/optimo/humedad", valor.toString())
    }
    fun alternarRiego() {
        // Invertimos el estado actual de la bomba
        val nuevoEstado = !_riegoEncendido.value
        _riegoEncendido.value = nuevoEstado

        // Publicamos el comando al topic correspondiente
        publicar("invernadero/bomba/cmd", if (nuevoEstado) "ON" else "OFF")

        Log.d("MQTT", "Comando enviado a bomba: ${if (nuevoEstado) "ON" else "OFF"}")
    }

    fun alternarVentilacion() {
        // Invertimos el estado actual de la ventilación
        val nuevoEstado = !_ventilacionEncendida.value
        _ventilacionEncendida.value = nuevoEstado

        // Publicamos el comando al topic correspondiente
        publicar("invernadero/ventiladores/cmd", if (nuevoEstado) "ON" else "OFF")

        Log.d("MQTT", "Comando enviado a ventiladores: ${if (nuevoEstado) "ON" else "OFF"}")
    }

    fun alternarPuerta() {
        // Invertimos el estado actual de la puerta
        val nuevoEstado = !_puertaAbierta.value
        _puertaAbierta.value = nuevoEstado

        // Publicamos el comando al topic correspondiente
        publicar("invernadero/servomotor1/cmd", if (nuevoEstado) "OPEN" else "CLOSE")

        Log.d("MQTT", "Comando enviado a puerta: ${if (nuevoEstado) "OPEN" else "CLOSE"}")
    }

    fun alternarLuz() {
        // Invertimos el estado actual de la luz
        val nuevoEstado = !_bombillaEncendida.value
        _bombillaEncendida.value = nuevoEstado

        // Publicamos el comando al topic correspondiente
        publicar("invernadero/led/power", if (nuevoEstado) "ON" else "OFF")

        Log.d("MQTT", "Comando enviado a luz: ${if (nuevoEstado) "ON" else "OFF"}")
    }

    // 🔧 Color LED en modo usuario (usa T_LED_CMD)

    fun setModoUsuarioLed(color: Color) {
        _colorBombilla.value = color
        val hex = String.format("#%02X%02X%02X",
            (color.red * 255).toInt(),
            (color.green * 255).toInt(),
            (color.blue * 255).toInt()
        )
        publicar("invernadero/led/cmd", hex)
        publicar("invernadero/led/mode", "USER") // activa modo usuario
    }

    // 🔧 Volver a modo automático de LEDs
    fun setModoAutomaticoLed() {
        _colorBombilla.value = Color.Green
        publicar("invernadero/led/cmd", "#00FF00") // 👈 fuerza verde en el LED físico
        publicar("invernadero/led/mode", "AUTO")

    }


    fun setDireccionIP(nueva: String) {
        // Permite cambiar la dirección IP del broker MQTT
        _direccionIP.value = nueva
    }

    // 🔧 Tiempo máximo de riego manual (segundos)
    fun setTiempoMaxRiego(valorSegundos: Float) {
        _tiempoMaxRiego.value = valorSegundos
        val milis = valorSegundos * 1000
        publicar("invernadero/bomba/max", milis.toString())
    }


    // --- Watchdog de recepción ---
    fun iniciarWatchdog() {
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(10000)
                val ahora = System.currentTimeMillis()
                if (ahora - ultimoMensajeRecibido > 30000) {
                    Log.w("MQTT", "Watchdog detecta inactividad")
                    _conectadoMQTT.value = false

                }
            }
        }
    }

}

// --- Extensión para convertir Color a HEX ---
fun Color.aHex(): String {
    val r = (red * 255).toInt()
    val g = (green * 255).toInt()
    val b = (blue * 255).toInt()
    return String.format("#%02X%02X%02X", r, g, b)
}
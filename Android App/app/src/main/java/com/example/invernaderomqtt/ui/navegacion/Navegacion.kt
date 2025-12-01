package com.example.invernaderomqtt.navigation

import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.invernaderomqtt.ui.PantallaPrincipal
import com.example.invernaderomqtt.ui.BotonControl
import com.example.invernaderomqtt.ui.about.PantallaAbout
import com.example.invernaderomqtt.ui.configuracion.ConfiguracionScreen
import com.example.invernaderomqtt.ui.configuracion.ConfiguracionServidorScreen
import com.example.invernaderomqtt.ui.historial.PantallaHistorial
import com.example.invernaderomqtt.ui.principal.VistaModeloMQTT
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.unit.sp

@Composable
fun NavegacionApp(controlNavegacion: NavHostController, vistaModelo: VistaModeloMQTT) {
    val riegoActivo = vistaModelo.riegoEncendido.collectAsState().value
    val ventilacionActiva = vistaModelo.ventilacionEncendida.collectAsState().value
    val puertaAbierta = vistaModelo.puertaAbierta.collectAsState().value
    val bombillaEncendida = vistaModelo.bombillaEncendida.collectAsState().value
    val colorTarjeta = Color(0xFF1A1A1A)

    // Nuevo: recogemos el estado visual del ViewModel
    val estadoVisual = vistaModelo.estadoVisual.collectAsState().value

    Scaffold(
        bottomBar = {
            MenuActuadores(
                vistaModelo = vistaModelo,
                riegoActivo = riegoActivo,
                ventilacionActiva = ventilacionActiva,
                puertaAbierta = puertaAbierta,
                bombillaEncendida = bombillaEncendida,
                colorTarjeta = colorTarjeta,
                estado = estadoVisual // ← aquí pasamos el estado
            )
        },
        containerColor = Color.Black
    ) { padding ->
        NavHost(
            navController = controlNavegacion,
            startDestination = "principal",
            modifier = Modifier.padding(padding)
        ) {
            composable("principal") {
                PantallaPrincipal(navController = controlNavegacion, vistaModelo = vistaModelo)
            }
            composable("configuracion_pid") { ConfiguracionScreen() }
            composable("camara") { CamaraStreamScreen(vistaModelo = vistaModelo) }
            composable("historial_eventos") { PantallaHistorial() }
            composable("configuracion_servidor") { ConfiguracionServidorScreen() }
            composable("about") { PantallaAbout() }
        }
    }
}

@Composable
fun CamaraStreamScreen(vistaModelo: VistaModeloMQTT? = null) {
    val context = LocalContext.current
    val libVLC = remember { LibVLC(context) }
    val mediaPlayer = remember { MediaPlayer(libVLC) }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.stop()
            mediaPlayer.release()
            libVLC.release()
        }
    }

    AndroidView(
        factory = {
            val videoLayout = VLCVideoLayout(it)
            mediaPlayer.attachViews(videoLayout, null, false, false)

            val media = Media(
                libVLC,
                Uri.parse("rtsp://Invernadero:1981c4m4r41981@juanjomalaga.duckdns.org:554/stream1")
            )
            media.setHWDecoderEnabled(true, false)
            mediaPlayer.media = media
            mediaPlayer.play()

            videoLayout
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun MenuActuadores(
    vistaModelo: VistaModeloMQTT,
    riegoActivo: Boolean,
    ventilacionActiva: Boolean,
    puertaAbierta: Boolean,
    bombillaEncendida: Boolean,
    colorTarjeta: Color,
    estado: String
) {
    val colorBorde = when (estado) {
        "BLOQUEADO" -> Color.Red
        "RIEGO" -> Color.Blue
        "OK", "NORMAL" -> Color(0xFF81C784) // verde
        else -> Color.White
    }

    val textoEstado = when (estado) {
        "BLOQUEADO" -> "⚠️ Sistema Bloqueado"
        "RIEGO" -> "💧 Regando"
        "OK", "NORMAL" -> "✅ OK"
        else -> estado
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .navigationBarsPadding()
            .padding(bottom = 8.dp)
            .border(
                width = 2.dp,
                color = colorBorde,
                shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
            ),
        elevation = 12.dp,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        backgroundColor = colorTarjeta
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = textoEstado,
                color = colorBorde,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 🔧 Botón de bomba: deshabilitado si bloqueado
                BotonControl(
                    icono = Icons.Default.Opacity,
                    etiqueta = "Bomba",
                    activo = riegoActivo,
                    enabled = estado != "BLOQUEADO"
                ) {
                    vistaModelo.alternarRiego()
                }

                BotonControl(
                    icono = Icons.Default.Cloud,
                    etiqueta = "Ventilador",
                    activo = ventilacionActiva
                ) {
                    vistaModelo.alternarVentilacion()
                }

                BotonControl(
                    icono = Icons.Default.MeetingRoom,
                    etiqueta = "Puerta",
                    activo = puertaAbierta
                ) {
                    vistaModelo.alternarPuerta()
                }

                BotonControl(
                    icono = Icons.Default.Lightbulb,
                    etiqueta = "Luz",
                    activo = bombillaEncendida
                ) {
                    vistaModelo.alternarLuz()
                }
            }
        }
    }
}


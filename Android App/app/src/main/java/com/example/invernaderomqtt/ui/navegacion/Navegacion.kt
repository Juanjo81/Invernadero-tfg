package com.example.invernaderomqtt.navigation

import android.net.Uri
import android.view.Surface
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Lightbulb

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.invernaderomqtt.ui.PantallaPrincipal
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.invernaderomqtt.ui.principal.VistaModeloMQTT
import com.example.invernaderomqtt.ui.BotonControl
import com.example.invernaderomqtt.ui.about.PantallaAbout
import com.example.invernaderomqtt.ui.configuracion.ConfiguracionScreen
import com.example.invernaderomqtt.ui.configuracion.ConfiguracionServidorScreen
import com.example.invernaderomqtt.ui.historial.PantallaHistorial

@Composable
fun NavegacionApp(controlNavegacion: NavHostController, vistaModelo: VistaModeloMQTT) {
    val riegoActivo = vistaModelo.riegoEncendido.collectAsState().value
    val ventilacionActiva = vistaModelo.ventilacionEncendida.collectAsState().value
    val puertaAbierta = vistaModelo.puertaAbierta.collectAsState().value
    val bombillaEncendida = vistaModelo.bombillaEncendida.collectAsState().value
    val colorTarjeta = Color(0xFF1A1A1A) // o el color que uses

    Scaffold(
        bottomBar = {
            MenuActuadores(
                vistaModelo = vistaModelo,
                riegoActivo = riegoActivo,
                ventilacionActiva = ventilacionActiva,
                puertaAbierta = puertaAbierta,
                bombillaEncendida = bombillaEncendida,
                colorTarjeta = colorTarjeta
            )
        },
        containerColor = Color.Black // ← fondo oscuro para toda la pantalla
    ) { padding ->
        NavHost(
            navController = controlNavegacion,
            startDestination = "principal",
            modifier = Modifier.padding(padding)
        ) {
            composable("principal") {
                PantallaPrincipal(navController = controlNavegacion, vistaModelo = vistaModelo)
            }

            composable("configuracion_pid") {
                ConfiguracionScreen()
            }




            composable("camara") {
                CamaraStreamScreen(vistaModelo = vistaModelo)
            }

            composable("historial_eventos") {
                PantallaHistorial()
            }

            composable("configuracion_servidor") {
                ConfiguracionServidorScreen()
            }

            composable("about") {
                PantallaAbout()
            }

            composable("configuracion_pid") {
                ConfiguracionScreen()
            }
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
    colorTarjeta: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .navigationBarsPadding()
            .padding(bottom = 8.dp)
            .border(
                width = 2.dp,
                color = Color.White,
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
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BotonControl(Icons.Default.Opacity, "Bomba", riegoActivo) {
                    vistaModelo.alternarRiego()
                }
                BotonControl(Icons.Default.Cloud, "Ventilador", ventilacionActiva) {
                    vistaModelo.alternarVentilacion()
                }
                BotonControl(Icons.Default.MeetingRoom, "Puerta", puertaAbierta) {
                    vistaModelo.alternarPuerta()
                }
                BotonControl(Icons.Default.Lightbulb, "Luz", bombillaEncendida) {
                    vistaModelo.alternarLuz()
                }

            }
        }
    }
}








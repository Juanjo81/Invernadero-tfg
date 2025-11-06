package com.example.invernaderomqtt.navigation

import android.net.Uri
import android.view.SurfaceView
import android.widget.Button
import android.widget.VideoView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.invernaderomqtt.Configuracion
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
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.invernaderomqtt.R
import com.example.invernaderomqtt.VistaModeloMQTT
import com.example.invernaderomqtt.ui.BotonControl

@Composable
fun NavegacionApp(controlNavegacion: NavHostController, vistaModelo: VistaModeloMQTT) {
    val riegoActivo = vistaModelo.riegoEncendido.collectAsState().value
    val ventilacionActiva = vistaModelo.ventilacionEncendida.collectAsState().value
    val puertaAbierta = vistaModelo.puertaAbierta.collectAsState().value
    val bombillaEncendida = vistaModelo.bombillaEncendida.collectAsState().value
    val colorTarjeta = Color(0xFF2E7D32) // o el color que uses

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
        }
    ) { padding ->
        NavHost(
            navController = controlNavegacion,
            startDestination = "principal",
            modifier = Modifier.padding(padding)
        ) {
            composable("principal") {
                PantallaPrincipal(navController = controlNavegacion, vistaModelo = vistaModelo)
            }
            composable("configuracion") {
                Configuracion.ConfiguracionScreen(
                    temperaturaObjetivo = vistaModelo.temperaturaObjetivo.collectAsState().value,
                    humedadObjetivo = vistaModelo.humedadObjetivo.collectAsState().value,
                    onTemperaturaChange = { vistaModelo.setTemperaturaObjetivo(it) },
                    onHumedadChange = { vistaModelo.setHumedadObjetivo(it) }
                )
            }
            composable("camara") {
                CamaraStreamScreen(vistaModelo = vistaModelo)
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
            .navigationBarsPadding() // ✅ sube todo el bloque por encima de los botones del sistema
            .padding(bottom = 8.dp), // pequeño margen extra opcional
        elevation = 8.dp,
        shape = RectangleShape,
        backgroundColor = colorTarjeta
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Actuadores Manuales",
                color = Color.White,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                BotonControl(Icons.Default.WaterDrop, "Bomba", riegoActivo) {
                    vistaModelo.alternarRiego()
                }
                BotonControl(Icons.Default.Air, "Ventilador", ventilacionActiva) {
                    vistaModelo.alternarVentilacion()
                }
                BotonControl(Icons.Default.DoorFront, "Puerta", puertaAbierta) {
                    vistaModelo.alternarPuerta()
                }
                BotonControl(Icons.Default.Lightbulb, "Luz", bombillaEncendida) {
                    vistaModelo.alternarLuz()
                }
            }
        }
    }
}








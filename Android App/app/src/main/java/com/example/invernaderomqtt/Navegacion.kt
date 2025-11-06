package com.example.invernaderomqtt.navigation

import android.net.Uri
import android.view.SurfaceView
import android.widget.VideoView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.invernaderomqtt.Configuracion
import com.example.invernaderomqtt.VistaModeloMQTT
import com.example.invernaderomqtt.ui.PantallaPrincipal
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout


@Composable
fun NavegacionApp(controlNavegacion: NavHostController, vistaModelo: VistaModeloMQTT) {
    NavHost(navController = controlNavegacion, startDestination = "principal") {
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
        // ✅ Nueva ruta para la cámara en directo
        composable("camara") {
            CamaraStreamScreen()
        }
    }
}

@Composable
fun CamaraStreamScreen() {
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

    AndroidView(factory = {
        val videoLayout = VLCVideoLayout(it)
        mediaPlayer.attachViews(videoLayout, null, false, false)

        val media = Media(libVLC, Uri.parse("rtsp://Invernadero:1981c4m4r41981@juanjomalaga.duckdns.org:554/stream1"))
        media.setHWDecoderEnabled(true, false)
        mediaPlayer.media = media
        mediaPlayer.play()

        videoLayout
    }, modifier = Modifier.fillMaxSize())
}








package com.example.invernaderomqtt.navigation

import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
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
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuGlobalTopBar(
    navController: NavHostController,
    titulo: String = "MENÚ"
) {
    val currentRoute = navController.currentBackStackEntryFlow
        .collectAsState(initial = navController.currentBackStackEntry)
        .value?.destination?.route
    var expanded by remember { mutableStateOf(false) }
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    val opciones = listOf(
        "principal" to "Página principal",
        "configuracion_pid" to "Configuración PID",
        "historial_eventos" to "Historial eventos",
        "configuracion_servidor" to "Configuración servidor",
        "about" to "About"
    )

    TopAppBar(
        title = {
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = Color.White)) {
                        append("Invernadero")
                    }
                    append(" ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF64B5F6))) {
                        append("TFG")
                    }
                },
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },


        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1A1A)),
        actions = {
            IconButton(onClick = { expanded = true }) {
                Icon(imageVector = Icons.Default.Menu, contentDescription = "Abrir menú", tint = Color.White)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(Color(0xFF2C2C2C), RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                opciones.filter { it.first != currentRoute }.forEach { (ruta, etiqueta) ->
                    DropdownMenuItem(
                        text = { Text(etiqueta, color = Color.White) },
                        onClick = {
                            expanded = false
                            navController.navigate(ruta) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                            }
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Cloud, contentDescription = null, tint = Color(0xFF81C784))
                        }
                    )
                }
                Divider(color = Color.Gray)
                DropdownMenuItem(
                    text = { Text("Salir", color = Color.Red) },
                    onClick = {
                        expanded = false
                        backDispatcher?.onBackPressed()
                    },
                    leadingIcon = {
                        Icon(Icons.Default.MeetingRoom, contentDescription = null, tint = Color.Red)
                    }
                )
            }
        }
    )
}

@Composable
fun NavegacionApp(controlNavegacion: NavHostController, vistaModelo: VistaModeloMQTT) {
    val riegoActivo = vistaModelo.riegoEncendido.collectAsState().value
    val ventilacionActiva = vistaModelo.ventilacionEncendida.collectAsState().value
    val puertaAbierta = vistaModelo.puertaAbierta.collectAsState().value
    val bombillaEncendida = vistaModelo.bombillaEncendida.collectAsState().value
    val colorTarjeta = Color(0xFF1A1A1A)

    val estadoVisual = vistaModelo.estadoVisual.collectAsState().value

    Scaffold(
        topBar = {
            MenuGlobalTopBar(
                navController = controlNavegacion,
                titulo = "MENÚ"
            )
        },
        bottomBar = {
            MenuActuadores(
                vistaModelo = vistaModelo,
                riegoActivo = riegoActivo,
                ventilacionActiva = ventilacionActiva,
                puertaAbierta = puertaAbierta,
                bombillaEncendida = bombillaEncendida,
                colorTarjeta = colorTarjeta,
                estado = estadoVisual
            )
        },
        containerColor = Color.Black
    ) { padding ->
        NavHost(
            navController = controlNavegacion,
            startDestination = "principal",
            modifier = Modifier.padding(padding)
        ) {
            composable("principal") { PantallaPrincipal(navController = controlNavegacion, vistaModelo = vistaModelo) }
            composable("configuracion_pid") { ConfiguracionScreen(vistaModelo) }
            composable("camara") { CamaraStreamScreen() }
            composable("historial_eventos") { PantallaHistorial() }
            composable("configuracion_servidor") { ConfiguracionServidorScreen(navController = controlNavegacion, vistaModelo = vistaModelo) }
            composable("about") { PantallaAbout() }
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
    estado: String,

) {

    val conectado = vistaModelo.conectadoMQTT.collectAsState().value
    val colorBorde = when (estado) {
        "BLOQUEO_RIEGO" -> Color.Red
        "BLOQUEO_VENTILACION" -> Color(0xFFFF9800) // naranja
        "BLOQUEO_TOTAL" -> Color.Magenta
        "RIEGO" -> Color.Blue
        "VENTILANDO" -> Color.Yellow
        "RIEGO+VENTILANDO" -> Color.Cyan
        "RIEGO+BLOQUEO_VENTILACION" -> Color.Blue // azul, pero indicando fallo
        "OK", "NORMAL" -> Color(0xFF81C784) // verde
        else -> Color.White
    }

    val textoEstado = when (estado) {
        "BLOQUEO_RIEGO" -> "⚠️ Bloqueo en Riego"
        "BLOQUEO_VENTILACION" -> "⚠️ Bloqueo en Ventilación"
        "BLOQUEO_TOTAL" -> "⛔ Sistema Bloqueado"
        "RIEGO" -> "💧 Regando"
        "VENTILANDO" -> "🌬️ Ventilando"
        "RIEGO+VENTILANDO" -> "💧🌬️ Regando y Ventilando"
        "RIEGO+BLOQUEO_VENTILACION" -> "💧 Regando (ventilación bloqueada)"
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
                //  Botón de bomba: deshabilitado si desconectado o bloqueo de riego o total
                BotonControl(
                    icono = Icons.Default.Opacity,
                    etiqueta = "Bomba",
                    activo = riegoActivo,
                    enabled = conectado &&
                            estado != "BLOQUEO_RIEGO" &&
                            estado != "BLOQUEO_TOTAL"
                ) {
                    vistaModelo.alternarRiego()
                }

                //  Botón de ventilador: deshabilitado si desconectado o bloqueo de ventilación o total
                BotonControl(
                    icono = Icons.Default.AcUnit,
                    etiqueta = "Ventilador",
                    activo = ventilacionActiva,
                    enabled = conectado &&
                            estado != "BLOQUEO_VENTILACION" &&
                            estado != "BLOQUEO_TOTAL"
                ) {
                    vistaModelo.alternarVentilacion()
                }

                BotonControl(
                    icono = Icons.Default.MeetingRoom,
                    etiqueta = "Puerta",
                    activo = puertaAbierta,
                    enabled = conectado
                ) {
                    vistaModelo.alternarPuerta()
                }

                BotonControl(
                    icono = Icons.Default.Lightbulb,
                    etiqueta = "Luz",
                    activo = bombillaEncendida,
                    enabled = conectado
                ) {
                    vistaModelo.alternarLuz()
                }
            }
        }
    }
}

@Composable
fun BotonControl(
    icono: ImageVector,
    etiqueta: String,
    activo: Boolean,
    enabled: Boolean = true,
    alPulsar: () -> Unit
) {
    val fondoAnimado by animateColorAsState(
        targetValue = if (activo) Color(0xFF2E7D32) else Color(0xFF1A1A1A),
        animationSpec = tween(durationMillis = 300)
    )

    Card(
        backgroundColor = fondoAnimado,
        elevation = if (activo) 12.dp else 4.dp,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier
            .padding(4.dp)
            .size(72.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            IconButton(
                onClick = alPulsar,
                enabled = enabled
            ) {
                Icon(icono, contentDescription = etiqueta, tint = Color.White)
            }

            Text(
                text = etiqueta,
                color = if (enabled) Color.White else Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}




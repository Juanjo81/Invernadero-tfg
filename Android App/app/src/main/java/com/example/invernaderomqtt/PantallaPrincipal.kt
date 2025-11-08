package com.example.invernaderomqtt.ui
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.content.MediaType.Companion.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.invernaderomqtt.R
import com.example.invernaderomqtt.VistaModeloMQTT
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2


@Composable
fun PantallaPrincipal(navController: NavController, vistaModelo: VistaModeloMQTT) {
    val temperatura by vistaModelo.temperaturaAire.collectAsState()
    val humedad by vistaModelo.humedadAire.collectAsState()
    val humedadSuelo by vistaModelo.humedadSuelo.collectAsState()
    val nivelDeposito by vistaModelo.nivelTanque.collectAsState()
    val temperaturaObjetivo by vistaModelo.temperaturaObjetivo.collectAsState()
    val humedadObjetivo by vistaModelo.humedadObjetivo.collectAsState()

    val conectado by vistaModelo.conectadoMQTT.collectAsState()
    val direccionIP by vistaModelo.direccionIP.collectAsState()
    val riegoActivo by vistaModelo.riegoEncendido.collectAsState()
    val ventilacionActiva by vistaModelo.ventilacionEncendida.collectAsState()
    val puertaAbierta by vistaModelo.puertaAbierta.collectAsState()
    val bombillaEncendida by vistaModelo.bombillaEncendida.collectAsState()

    val colorTarjeta = Color(0xFF1A1A1A)
    val scrollState = rememberScrollState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(Color(0xFF1B1B1B))
                    .padding(16.dp)
            ) {
                Text("Menú", color = Color.White, fontSize = 20.sp)
                Spacer(modifier = Modifier.height(16.dp))

                DrawerItem("Configuración PID") { /* TODO */ }
                DrawerItem("Versión") { /* TODO */ }
                DrawerItem("About") { /* TODO */ }
                DrawerItem("Servidor") { /* TODO */ }
                DrawerItem("Salir") { /* TODO */ }
            }
        },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .background(Color.Black)
                    .padding(16.dp)
            ) {
                // Botón de menú lateral
                IconButton(onClick = {
                    scope.launch { drawerState.open() }
                }) {
                    Icon(Icons.Default.Settings, contentDescription = "Configuración", tint = Color.White)
                }

                Text(
                    text = if (conectado) "Conectado MQTT: $direccionIP" else "No conectado",
                    color = if (conectado) Color.Green else Color.Red,
                    fontSize = 16.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 🟥 Columna izquierda
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TarjetaSensor(
                            titulo = "Temperatura Aire",
                            valor = "${temperatura}°C",
                            color = obtenerColorTemperatura(temperatura.toFloat()),
                            topic = "invernadero/aire/temperatura",
                            modifier = Modifier.fillMaxWidth()
                        )
                        TarjetaSensor(
                            titulo = "Humedad Aire",
                            valor = "${humedad}%",
                            color = obtenerColorAgua(humedad.toFloat()),
                            topic = "invernadero/aire/humedad",
                            modifier = Modifier.fillMaxWidth()
                        )
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clickable { navController.navigate("camara") },
                            shape = RoundedCornerShape(12.dp),
                            elevation = 4.dp
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.invernadero_foto),
                                contentDescription = "Vista del invernadero",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // 🟦 Columna derecha
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TarjetaCircular(
                            titulo = "Humedad Suelo",
                            valor = humedadSuelo.toFloat(),
                            color = obtenerColorAgua(humedadSuelo.toFloat()),
                            topic = "invernadero/suelo/humedad",
                            modifier = Modifier.fillMaxWidth()
                        )
                        TarjetaDeposito(
                            nivel = nivelDeposito.toFloat(),
                            topic = "invernadero/tanque/nivel",
                            modifier = Modifier.fillMaxWidth()
                        )
                        TarjetaSensor(
                            titulo = "Humedad Aire",
                            valor = "${humedad}%",
                            color = obtenerColorAgua(humedad.toFloat()),
                            topic = "invernadero/aire/humedad",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun DrawerItem(texto: String, onClick: () -> Unit) {
    Text(
        text = texto,
        color = Color.White,
        fontSize = 16.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp)
    )
}

@Composable
fun TarjetaSensor(
    titulo: String,
    valor: String,
    color: Color,
    topic: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .aspectRatio(1f),
        elevation = 8.dp,
        backgroundColor = Color(0xFF1A1A1A)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Título arriba a la izquierda
                Text(
                    text = titulo,
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Text(
                    text = valor,
                    color = color,
                    fontSize = 36.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Text(
                    text = topic,
                    color = Color(0xFF666666),
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun TarjetaCircular(
    titulo: String,
    valor: Float,
    color: Color,
    topic: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.aspectRatio(1f),
        elevation = 8.dp,
        backgroundColor = Color(0xFF1A1A1A)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = titulo,
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Start)
                )
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp).align(Alignment.CenterHorizontally)) {
                    CircularProgressIndicator(
                        progress = valor / 100f,
                        color = color,
                        strokeWidth = 8.dp,
                        modifier = Modifier.fillMaxSize()
                    )
                    Text(
                        text = "${"%.1f".format(valor)}%",
                        color = Color.White,
                        fontSize = 28.sp
                    )
                }
                Text(
                    text = topic,
                    color = Color(0xFF666666),
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
@Composable
fun TarjetaDeposito(
    nivel: Float,
    topic: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.aspectRatio(1f),
        elevation = 8.dp,
        backgroundColor = Color(0xFF1A1A1A)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Título arriba a la izquierda
                Text(
                    text = "Nivel Depósito",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Start)
                )

                // Indicador vertical centrado
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(24.dp)
                            .height(100.dp)
                            .background(Color.Gray)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height((nivel).dp.coerceAtMost(100.dp))
                                .align(Alignment.BottomCenter)
                                .background(obtenerColorAgua(nivel))
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "${"%.1f".format(nivel)}%",
                        color = Color.White,
                        fontSize = 28.sp
                    )
                }

                // Topic abajo a la derecha
                Text(
                    text = topic,
                    color = Color(0xFF666666),
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

@Composable
fun BotonControl(
    icono: ImageVector,
    etiqueta: String,
    activo: Boolean,
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
            IconButton(onClick = alPulsar) {
                Icon(icono, contentDescription = etiqueta, tint = Color.White)
            }
            Text(text = etiqueta, color = Color.White, fontSize = 12.sp)
        }
    }
}

fun obtenerColorTemperatura(temp: Float): Color = when {
    temp < 10 -> Color.Blue
    temp in 10f..25f -> Color.Green
    temp in 25f..35f -> Color(0xFFFFA500)
    else -> Color.Red
}

fun obtenerColorAgua(nivel: Float): Color = when {
    nivel < 10 -> Color.Red
    nivel in 10f..25f -> Color.Yellow
    nivel in 25f..49f -> Color.Green
    else -> Color.Blue
}
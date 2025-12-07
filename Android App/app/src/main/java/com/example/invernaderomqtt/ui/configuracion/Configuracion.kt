package com.example.invernaderomqtt.ui.configuracion

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.invernaderomqtt.ui.principal.VistaModeloMQTT

@Composable
fun ConfiguracionScreen(vistaModelo: VistaModeloMQTT) {
    val temperaturaObjetivo by vistaModelo.temperaturaObjetivo.collectAsState()
    val humedadObjetivo by vistaModelo.humedadObjetivo.collectAsState()
    val tiempoMaxRiego by vistaModelo.tiempoMaxRiego.collectAsState()
    val colorLedUsuario by vistaModelo.colorBombilla.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // 👈 scroll activado
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("⚙️ Configuración PID", color = Color.White, fontSize = 22.sp)

            TarjetaConfiguracion(
                titulo = "Temperatura Objetivo",
                unidad = "°C",
                valorActual = temperaturaObjetivo,
                rango = 20f..45f,
                onFinalizarCambio = { vistaModelo.setTemperaturaObjetivo(it) }
            )

            TarjetaConfiguracion(
                titulo = "Humedad Objetivo",
                unidad = "%",
                valorActual = humedadObjetivo,
                rango = 0f..100f,
                onFinalizarCambio = { vistaModelo.setHumedadObjetivo(it) }
            )

            TarjetaConfiguracion(
                titulo = "Tiempo Máximo Riego Manual",
                unidad = "segundos",
                valorActual = tiempoMaxRiego.toFloat(),
                rango = 5f..300f,
                onFinalizarCambio = { vistaModelo.setTiempoMaxRiego(it) }
            )

            TarjetaColorLed(
                colorActual = colorLedUsuario,
                onColorSeleccionado = { color -> vistaModelo.setModoUsuarioLed(color) },
                onVolverAutomatico = { vistaModelo.setModoAutomaticoLed() }
            )
        }
    }
}

@Composable
fun TarjetaColorLed(
    colorActual: Color,
    onColorSeleccionado: (Color) -> Unit,
    onVolverAutomatico: () -> Unit
) {
    var mostrarPicker by remember { mutableStateOf(false) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }
    var colorSeleccionado by remember { mutableStateOf(colorActual) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A1A), shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text("Selección Color LED", color = Color.White, fontSize = 18.sp)
        Spacer(Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Button(onClick = { mostrarPicker = true }) {
                Text("Seleccionar color")
            }
            Button(onClick = {
                onVolverAutomatico()
                mostrarConfirmacion = true
            }) {
                Text("Modo automático")
            }
        }

        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(colorSeleccionado, RoundedCornerShape(8.dp))
        )

        if (mostrarConfirmacion) {
            Text(
                text = "✅ Valor enviado",
                color = Color(0xFF81C784),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }

    if (mostrarPicker) {
        ColorRGBPicker(
            colorInicial = colorSeleccionado,
            onColorSeleccionado = { nuevoColor ->
                colorSeleccionado = nuevoColor
                onColorSeleccionado(nuevoColor)
                mostrarConfirmacion = true
            },
            onCerrar = { mostrarPicker = false }
        )
    }
}

@Composable
fun ColorRGBPicker(
    colorInicial: Color,
    onColorSeleccionado: (Color) -> Unit,
    onCerrar: () -> Unit
) {
    var red by remember { mutableStateOf((colorInicial.red * 255).toInt()) }
    var green by remember { mutableStateOf((colorInicial.green * 255).toInt()) }
    var blue by remember { mutableStateOf((colorInicial.blue * 255).toInt()) }

    val colorActual = Color(red, green, blue)

    val coloresPredefinidos = listOf(
        Color.White, Color.Black, Color.Red, Color.Green, Color.Blue,
        Color.Yellow, Color.Cyan, Color.Magenta, Color.Gray, Color(255, 165, 0) // naranja
    )

    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Selecciona color de luz") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // 🔹 Colores predefinidos
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    coloresPredefinidos.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(color, RoundedCornerShape(6.dp))
                                .border(2.dp, Color.White, RoundedCornerShape(6.dp))
                                .clickable {
                                    red = (color.red * 255).toInt()
                                    green = (color.green * 255).toInt()
                                    blue = (color.blue * 255).toInt()
                                    onColorSeleccionado(Color(red, green, blue))
                                }
                        )
                    }
                }

                // 🔹 Sliders RGB
                Text("Rojo: $red", color = Color.Red)
                Slider(value = red.toFloat(), onValueChange = {
                    red = it.toInt()
                    onColorSeleccionado(Color(red, green, blue))
                }, valueRange = 0f..255f)

                Text("Verde: $green", color = Color.Green)
                Slider(value = green.toFloat(), onValueChange = {
                    green = it.toInt()
                    onColorSeleccionado(Color(red, green, blue))
                }, valueRange = 0f..255f)

                Text("Azul: $blue", color = Color.Blue)
                Slider(value = blue.toFloat(), onValueChange = {
                    blue = it.toInt()
                    onColorSeleccionado(Color(red, green, blue))
                }, valueRange = 0f..255f)

                // 🔹 Preview
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(colorActual, RoundedCornerShape(8.dp))
                        .border(2.dp, Color.White, RoundedCornerShape(8.dp))
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onCerrar) { Text("Aceptar") }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) { Text("Cancelar") }
        }
    )
}

@Composable
fun TarjetaConfiguracion(
    titulo: String,
    unidad: String,
    valorActual: Float,
    rango: ClosedFloatingPointRange<Float>,
    onFinalizarCambio: (Float) -> Unit
) {
    var valorTemporal by remember { mutableStateOf(valorActual) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1A1A1A), shape = RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(text = titulo, color = Color.White, fontSize = 18.sp)
        Spacer(Modifier.height(4.dp))
        Text(text = "${valorTemporal.toInt()}$unidad", color = Color(0xFFB0BEC5), fontSize = 14.sp)

        Slider(
            value = valorTemporal,
            onValueChange = { valorTemporal = it },
            onValueChangeFinished = {
                onFinalizarCambio(valorTemporal)
                mostrarConfirmacion = true
            },
            valueRange = rango,
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = Color(0xFF64B5F6),
                inactiveTrackColor = Color(0xFF37474F)
            )
        )

        if (mostrarConfirmacion) {
            Text(
                text = "✅ Valor enviado",
                color = Color(0xFF81C784),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
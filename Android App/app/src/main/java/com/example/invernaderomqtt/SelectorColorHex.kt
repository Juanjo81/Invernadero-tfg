package com.example.invernaderomqtt.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SelectorColorHex(
    colorInicial: Color,
    alSeleccionarColor: (Color) -> Unit,
    alCerrar: () -> Unit
) {
    var colorSeleccionado by remember { mutableStateOf(colorInicial) }

    AlertDialog(
        onDismissRequest = alCerrar,
        title = { Text("Selecciona color de luz") },
        text = {
            Column {
                Text("Rojo", color = Color.Red)
                Slider(
                    value = colorSeleccionado.red,
                    onValueChange = { colorSeleccionado = colorSeleccionado.copy(red = it) },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(Color.Red)
                )
                Text("Verde", color = Color.Green)
                Slider(
                    value = colorSeleccionado.green,
                    onValueChange = { colorSeleccionado = colorSeleccionado.copy(green = it) },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(Color.Green)
                )
                Text("Azul", color = Color.Blue)
                Slider(
                    value = colorSeleccionado.blue,
                    onValueChange = { colorSeleccionado = colorSeleccionado.copy(blue = it) },
                    valueRange = 0f..1f,
                    colors = SliderDefaults.colors(Color.Blue)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "HEX: ${colorSeleccionado.aHex()}",
                    color = colorSeleccionado,
                    fontSize = 18.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                alSeleccionarColor(colorSeleccionado)
                alCerrar()
            }) {
                Text("Aceptar")
            }
        },
        dismissButton = {
            TextButton(onClick = alCerrar) {
                Text("Cancelar")
            }
        }
    )
}

fun Color.aHex(): String {
    val r = (red * 255).toInt()
    val v = (green * 255).toInt()
    val a = (blue * 255).toInt()
    return String.format("#%02X%02X%02X", r, v, a)
}
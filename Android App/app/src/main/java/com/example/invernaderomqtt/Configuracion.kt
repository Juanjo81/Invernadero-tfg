package com.example.invernaderomqtt

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class Configuracion {
    companion object {
        @Composable
        fun ConfiguracionScreen(
            temperaturaObjetivo: Float,
            humedadObjetivo: Float,
            onTemperaturaChange: (Float) -> Unit,
            onHumedadChange: (Float) -> Unit
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Configuración", color = Color.White, fontSize = 22.sp)

                TarjetaConfiguracion(
                    titulo = "Temperatura Objetivo",
                    valor = "${temperaturaObjetivo.toInt()}°C",
                    sliderValue = temperaturaObjetivo,
                    range = 20f..45f,
                    onValueChange = onTemperaturaChange
                )

                TarjetaConfiguracion(
                    titulo = "Humedad Objetivo",
                    valor = "${humedadObjetivo.toInt()}%",
                    sliderValue = humedadObjetivo,
                    range = 0f..100f,
                    onValueChange = onHumedadChange
                )
            }
        }

        @Composable
        fun TarjetaConfiguracion(
            titulo: String,
            valor: String,
            sliderValue: Float,
            range: ClosedFloatingPointRange<Float>,
            onValueChange: (Float) -> Unit
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1A1A), shape = RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text(titulo, color = Color.White, fontSize = 18.sp)
                Spacer(Modifier.height(4.dp))
                Text(valor, color = Color.Gray, fontSize = 14.sp)
                Slider(
                    value = sliderValue,
                    onValueChange = onValueChange,
                    valueRange = range,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color(0xFF64B5F6)
                    )
                )
            }
        }
    }
}
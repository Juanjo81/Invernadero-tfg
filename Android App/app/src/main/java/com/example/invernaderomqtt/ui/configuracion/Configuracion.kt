package com.example.invernaderomqtt.ui.configuracion

import android.view.Surface
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.invernaderomqtt.ui.principal.VistaModeloMQTT




        @Composable
        fun ConfiguracionScreen() {
            val viewModel: VistaModeloMQTT = viewModel()
            val contexto = LocalContext.current

            LaunchedEffect(Unit) {
                viewModel.inicializarMQTT(contexto)
            }

            val temperaturaObjetivo = viewModel.temperaturaObjetivo.collectAsState().value
            val humedadObjetivo = viewModel.humedadObjetivo.collectAsState().value


            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("⚙️ Configuración PID", color = Color.White, fontSize = 22.sp)

                    TarjetaConfiguracion(
                        titulo = "Temperatura Objetivo",
                        valor = "${temperaturaObjetivo.toInt()}°C",
                        sliderValue = temperaturaObjetivo,
                        range = 20f..45f,
                        onValueChange = { viewModel.setTemperaturaObjetivo(it) }
                    )

                    TarjetaConfiguracion(
                        titulo = "Humedad Objetivo",
                        valor = "${humedadObjetivo.toInt()}%",
                        sliderValue = humedadObjetivo,
                        range = 0f..100f,
                        onValueChange = { viewModel.setHumedadObjetivo(it) }
                    )
                }
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
                Text(
                    text = titulo,
                    color = Color.White,
                    fontSize = 18.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = valor,
                    color = Color(0xFFB0BEC5), // gris claro
                    fontSize = 14.sp
                )
                Slider(
                    value = sliderValue,
                    onValueChange = onValueChange,
                    valueRange = range,
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color(0xFF64B5F6),
                        inactiveTrackColor = Color(0xFF37474F)
                    )
                )
            }
        }


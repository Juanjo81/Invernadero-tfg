package com.example.invernaderomqtt.ui.configuracion

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.invernaderomqtt.ui.principal.VistaModeloMQTT
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ConfiguracionScreen() {
    val viewModel: VistaModeloMQTT = viewModel()
    val contexto = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.inicializarMQTT(contexto)
    }

    val temperaturaObjetivo by viewModel.temperaturaObjetivo.collectAsState()
    val humedadObjetivo by viewModel.humedadObjetivo.collectAsState()

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
                unidad = "°C",
                valorActual = temperaturaObjetivo,
                rango = 20f..45f,
                onFinalizarCambio = { viewModel.setTemperaturaObjetivo(it) }
            )

            TarjetaConfiguracion(
                titulo = "Humedad Objetivo",
                unidad = "%",
                valorActual = humedadObjetivo,
                rango = 0f..100f,
                onFinalizarCambio = { viewModel.setHumedadObjetivo(it) }
            )
        }
    }
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
    val scope = rememberCoroutineScope()

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
                scope.launch {
                    delay(2000)
                    mostrarConfirmacion = false
                }
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
package com.example.invernaderomqtt.ui.configuracion

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.invernaderomqtt.ui.principal.VistaModeloMQTT
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ConfiguracionServidorScreen() {
    val viewModel: VistaModeloMQTT = viewModel()
    val contexto = LocalContext.current
    val scope = rememberCoroutineScope()

    var textoServidor by remember { mutableStateOf(TextFieldValue(viewModel.direccionIP.value)) }
    var mensajePopup by remember { mutableStateOf<String?>(null) }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("🔌 Configuración del Servidor MQTT", color = Color.White, fontSize = 22.sp)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1A1A1A), shape = RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Text("Dirección del servidor", color = Color.White, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = textoServidor,
                    onValueChange = { textoServidor = it },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF2C2C2C),
                        unfocusedContainerColor = Color(0xFF2C2C2C),
                        focusedIndicatorColor = Color(0xFF64B5F6),
                        unfocusedIndicatorColor = Color.Gray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
                    scope.launch {
                        viewModel.setDireccionIP(textoServidor.text)
                        viewModel.inicializarMQTT(contexto)
                        delay(1500) // esperar a que se complete la conexión

                        if (viewModel.conectadoMQTT.value) {
                            mensajePopup = "✅ Conectado correctamente a ${textoServidor.text}"
                        } else {
                            mensajePopup = "❌ Error al conectar con ${textoServidor.text}"
                        }
                    }
                }) {
                    Text("Conectar")
                }

                Button(onClick = {
                    textoServidor = TextFieldValue(viewModel.direccionIP.value)
                    mensajePopup = null
                }) {
                    Text("Cancelar")
                }
            }

            if (mensajePopup != null) {
                Text(
                    text = mensajePopup!!,
                    color = if (mensajePopup!!.startsWith("✅")) Color(0xFF81C784) else Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
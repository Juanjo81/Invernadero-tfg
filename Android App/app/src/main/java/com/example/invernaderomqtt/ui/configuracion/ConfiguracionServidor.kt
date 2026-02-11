package com.example.invernaderomqtt.ui.configuracion

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
import androidx.navigation.NavHostController
import com.example.invernaderomqtt.ui.principal.VistaModeloMQTT
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
@Composable
fun ConfiguracionServidorScreen(navController: NavHostController, vistaModelo: VistaModeloMQTT) {

    val contexto = LocalContext.current
    val scope = rememberCoroutineScope()
    var textoServidor by remember { mutableStateOf(TextFieldValue(vistaModelo.direccionIP.value)) }
    var mostrarDialogoError by remember { mutableStateOf(false) }
    var mostrarDialogoOk by remember { mutableStateOf(false) }
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
                        vistaModelo.setDireccionIP(textoServidor.text)
                        vistaModelo.inicializarMQTT(contexto)
                        delay(1500) // esperar a que se complete la conexión

                        if (vistaModelo.conectadoMQTT.value) {
                            mensajePopup = "✅ Conectado correctamente a ${textoServidor.text}"
                            mostrarDialogoOk = true
                        } else {
                            mensajePopup = "❌ Error al conectar con ${textoServidor.text}"
                            mostrarDialogoError = true
                        }
                    }
                }) {
                    Text("Añadir")
                }

                Button(onClick = {
                    textoServidor = TextFieldValue(vistaModelo.direccionIP.value)
                    mensajePopup = null
                }) {
                    Text("Cancelar")
                }
            }
        }
    }

    // 🔹 Diálogo si falla la conexión
    if (mostrarDialogoError) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoError = false },
            title = { Text("Error de conexión") },
            text = { Text("Ha fallado la conexión con ${textoServidor.text}. ¿Quieres añadir igualmente este servidor?") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogoError = false
                    // Guardar la IP aunque falle
                    vistaModelo.setDireccionIP(textoServidor.text)

                    // Navegar al principal
                    navController.navigate("principal") {
                        popUpTo("principal") { inclusive = true }
                    }
                }) {
                    Text("Sí, añadir")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoError = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // 🔹 Diálogo si conecta correctamente
    if (mostrarDialogoOk) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoOk = false },
            title = { Text("Conexión exitosa") },
            text = { Text(mensajePopup ?: "✅ Conectado") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialogoOk = false
                    navController.navigate("principal") {
                        popUpTo("principal") { inclusive = true }
                    }
                }) {
                    Text("OK")
                }
            }
        )
    }
}
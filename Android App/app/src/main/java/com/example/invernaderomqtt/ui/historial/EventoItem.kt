package com.example.invernaderomqtt.ui.historial

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.invernaderomqtt.data.eventos.Evento
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EventoItem(evento: Evento) {
    val color = when (evento.tipo) {
        "alerta" -> Color.Red
        "notificacion" -> Color.Blue
        "debug" -> Color.Gray
        else -> Color.Black
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = evento.mensaje, color = color, style = MaterialTheme.typography.bodyLarge)
            Text(text = evento.topico, style = MaterialTheme.typography.bodySmall)
            Text(
                text = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(evento.timestamp)),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
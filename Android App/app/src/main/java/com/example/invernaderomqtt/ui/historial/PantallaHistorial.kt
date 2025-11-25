package com.example.invernaderomqtt.ui.historial

import android.app.DatePickerDialog
import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.invernaderomqtt.data.eventos.Evento
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PantallaHistorial() {
    val viewModel: HistorialViewModel = viewModel()
    val eventos = viewModel.eventos.collectAsState().value
    val context = LocalContext.current

    // Estado para fecha seleccionada
    var fechaSeleccionada by remember { mutableStateOf<Date?>(null) }

    // Filtrar eventos por fecha seleccionada o últimos 3 días
    val eventosFiltrados = remember(eventos, fechaSeleccionada) {
        if (fechaSeleccionada != null) {
            eventos.filter {
                val fechaEvento = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.timestamp))
                val fechaFiltro = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(fechaSeleccionada!!)
                fechaEvento == fechaFiltro
            }
        } else {
            val hoy = Calendar.getInstance()
            val tresDiasAntes = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -2) }
            eventos.filter {
                val fechaEvento = Calendar.getInstance().apply { time = Date(it.timestamp) }
                !fechaEvento.before(tresDiasAntes)
            }
        }
    }

    val eventosPorDia = eventosFiltrados.groupBy {
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it.timestamp))
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "📋 Listado de Eventos",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(12.dp)
            )

            LazyColumn(modifier = Modifier.weight(1f)) {
                eventosPorDia.forEach { (fecha, lista) ->
                    item {
                        Text(
                            text = fecha,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFFFD700), // amarillo dorado
                            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                        )
                    }
                    items(lista) { evento ->
                        EventoItemCompacto(evento)
                    }
                }
            }

            Button(
                onClick = {
                    val hoy = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            val cal = Calendar.getInstance()
                            cal.set(year, month, day, 0, 0, 0)
                            fechaSeleccionada = cal.time
                        },
                        hoy.get(Calendar.YEAR),
                        hoy.get(Calendar.MONTH),
                        hoy.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text("Buscar por fecha específica")
            }
        }
    }
}

@Composable
fun EventoItemCompacto(evento: Evento) {
    val hora = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(evento.timestamp))
    val color = when (evento.tipo) {
        "alerta" -> Color.Red
        "info" -> Color(0xFF2196F3) // azul
        else -> Color.Gray
    }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 4.dp)) {
        Text(
            text = evento.mensaje,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
        Text(
            text = "${evento.topico} · $hora",
            style = MaterialTheme.typography.labelSmall,
            color = Color.LightGray
        )
    }
}
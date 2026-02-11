package com.example.invernaderomqtt.ui.historial

import android.app.DatePickerDialog
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

    // Estado para rango de fechas
    var fechaInicio by remember { mutableStateOf<Date?>(null) }
    var fechaFin by remember { mutableStateOf<Date?>(null) }

    // Filtrar eventos por rango o últimos 3 días
    val eventosFiltrados = remember(eventos, fechaInicio, fechaFin) {
        if (fechaInicio != null && fechaFin != null) {
            eventos.filter {
                val fechaEvento = Date(it.timestamp)
                fechaEvento.after(fechaInicio) && fechaEvento.before(fechaFin)
            }
        } else {
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

            // Botón para seleccionar fecha de inicio
            Button(
                onClick = {
                    val hoy = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            val cal = Calendar.getInstance()
                            cal.set(year, month, day, 0, 0, 0)
                            fechaInicio = cal.time
                        },
                        hoy.get(Calendar.YEAR),
                        hoy.get(Calendar.MONTH),
                        hoy.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("Seleccionar fecha inicio")
            }

            // Botón para seleccionar fecha de fin
            Button(
                onClick = {
                    val hoy = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            val cal = Calendar.getInstance()
                            cal.set(year, month, day, 23, 59, 59)
                            fechaFin = cal.time
                        },
                        hoy.get(Calendar.YEAR),
                        hoy.get(Calendar.MONTH),
                        hoy.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("Seleccionar fecha fin")
            }
        }
    }
}

@Composable
fun EventoItemCompacto(evento: Evento) {
    val hora = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(evento.timestamp))
    val color = when (evento.tipo) {
        "alerta" -> Color.Red
        "notificacion" -> Color(0xFF2196F3) // azul
        else -> Color.Gray
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
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
package com.example.invernaderomqtt.ui.historial

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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.invernaderomqtt.data.eventos.Evento
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PantallaHistorial() {
    val viewModel: HistorialViewModel = viewModel()
    val eventos = viewModel.eventos.collectAsState().value

    val eventosPorDia = eventos.groupBy { evento ->
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(evento.timestamp))
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "📋 Listado de Eventos",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(16.dp)
            )
            // resto del contenido
           LazyColumn(modifier = Modifier.fillMaxSize()) {
                eventosPorDia.forEach { (fecha, lista) ->
                    item {
                        Text(
                            text = fecha,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    items(lista) { evento ->
                        EventoItem(evento)
                    }
                }
            }
        }
    }
}
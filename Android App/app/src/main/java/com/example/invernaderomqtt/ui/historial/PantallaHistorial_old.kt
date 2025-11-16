package com.example.invernaderomqtt.ui.historial

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.invernaderomqtt.data.eventos.Evento
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PantallaHistorial2() {
    val context = LocalContext.current.applicationContext as Application
    val viewModel: HistorialViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HistorialViewModel(context) as T
            }
        }
    )

    val eventos = viewModel.eventos.collectAsState().value

    val eventosPorDia = eventos.groupBy { evento ->
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(evento.timestamp))
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        eventosPorDia.forEach { (fecha, lista) ->
            item {
                Text(
                    text = fecha,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(8.dp)
                )
            }
            items(lista) { evento ->
                EventoItem(evento)
            }
        }
    }
}
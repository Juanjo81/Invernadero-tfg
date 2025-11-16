package com.example.invernaderomqtt.ui.historial

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.invernaderomqtt.data.eventos.Evento
import com.example.invernaderomqtt.data.eventos.EventoBD
import com.example.invernaderomqtt.data.eventos.RepositorioEventos
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class HistorialViewModel(application: Application) : AndroidViewModel(application) {
    private val dao by lazy {
        EventoBD.obtener(getApplication()).eventoDao()
    }
    private val repo = RepositorioEventos(dao)

    val eventos: StateFlow<List<Evento>> = repo.obtenerHistorial()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
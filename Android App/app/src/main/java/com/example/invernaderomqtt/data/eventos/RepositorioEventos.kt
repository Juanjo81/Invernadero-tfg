package com.example.invernaderomqtt.data.eventos

import kotlinx.coroutines.flow.Flow

class RepositorioEventos(private val dao: EventoDao) {

    suspend fun registrarEvento(tipo: String, mensaje: String, topico: String) {
        val evento = Evento(tipo = tipo, mensaje = mensaje, topico = topico)
        dao.insertar(evento)
    }

    suspend fun limpiarEventosAntiguos(dias: Int = 30) {
        val limite = System.currentTimeMillis() - dias * 24 * 60 * 60 * 1000
        dao.limpiarAntiguos(limite)
    }

    fun obtenerHistorial(): Flow<List<Evento>> = dao.obtenerTodos()
}
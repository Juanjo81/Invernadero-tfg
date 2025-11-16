package com.example.invernaderomqtt.data.eventos

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "eventos")
data class Evento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tipo: String,              // "alerta", "info", "debug"
    val mensaje: String,           // contenido del evento
    val topico: String,            // tópico MQTT asociado
    val timestamp: Long = System.currentTimeMillis()
)
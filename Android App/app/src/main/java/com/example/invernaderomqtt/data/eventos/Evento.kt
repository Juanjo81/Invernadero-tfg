package com.example.invernaderomqtt.data.eventos

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "eventos")
data class Evento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tipo: String,
    val mensaje: String,
    val topico: String,
    val timestamp: Long = System.currentTimeMillis()
)
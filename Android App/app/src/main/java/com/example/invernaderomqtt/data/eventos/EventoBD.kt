package com.example.invernaderomqtt.data.eventos

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.invernaderomqtt.data.eventos.Evento
import com.example.invernaderomqtt.data.eventos.EventoDao

@Database(entities = [Evento::class], version = 1)
abstract class EventoBD : RoomDatabase() {
    abstract fun eventoDao(): EventoDao

    companion object {
        @Volatile private var instancia: EventoBD? = null

        fun obtener(context: Context): EventoBD {
            return instancia ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    EventoBD::class.java,
                    "eventos.db"
                ).build().also { instancia = it }
            }
        }
    }
}
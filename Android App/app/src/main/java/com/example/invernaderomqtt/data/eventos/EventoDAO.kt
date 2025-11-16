package com.example.invernaderomqtt.data.eventos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EventoDao {

    @Query("SELECT * FROM eventos ORDER BY timestamp DESC LIMIT :limite")
    suspend fun obtenerUltimos(limite: Int): List<Evento>

    @Query("DELETE FROM eventos WHERE timestamp < :limiteTiempo")
    suspend fun limpiarAntiguos(limiteTiempo: Long)

    @Query("SELECT * FROM eventos ORDER BY timestamp DESC")
    fun obtenerTodos(): Flow<List<Evento>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(evento: Evento)


}
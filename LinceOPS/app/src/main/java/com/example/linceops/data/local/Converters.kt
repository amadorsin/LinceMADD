package com.example.linceops.data.local

import androidx.room.TypeConverter
import com.example.linceops.model.EstadoServicio
import com.example.linceops.model.ParticipacionServicio
import com.example.linceops.model.Pasajero
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromEstadoServicio(value: EstadoServicio): String = value.name

    @TypeConverter
    fun toEstadoServicio(value: String): EstadoServicio = enumValueOf(value)

    @TypeConverter
    fun fromStringList(value: List<String>): String = gson.toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromPasajeroList(value: List<Pasajero>): String = gson.toJson(value)

    @TypeConverter
    fun toPasajeroList(value: String): List<Pasajero> {
        val type = object : TypeToken<List<Pasajero>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromParticipacionList(value: List<ParticipacionServicio>): String = gson.toJson(value)

    @TypeConverter
    fun toParticipacionList(value: String): List<ParticipacionServicio> {
        val type = object : TypeToken<List<ParticipacionServicio>>() {}.type
        return gson.fromJson(value, type)
    }
}
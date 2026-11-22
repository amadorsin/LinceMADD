package com.example.linceops.data.local

import androidx.room.TypeConverter
import com.example.linceops.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter fun fromEstadoServicio(value: EstadoServicio): String = value.name
    @TypeConverter fun toEstadoServicio(value: String): EstadoServicio = enumValueOf(value)

    @TypeConverter fun fromCodigoOficina(value: CodigoOficina): String = value.name
    @TypeConverter fun toCodigoOficina(value: String): CodigoOficina = CodigoOficina.fromApi(value)

    @TypeConverter fun fromTipoServicio(value: TipoServicio): String = value.name
    @TypeConverter fun toTipoServicio(value: String): TipoServicio = TipoServicio.fromApi(value)

    @TypeConverter fun fromCodigoSrv(value: CodigoSrv): String = value.name
    @TypeConverter fun toCodigoSrv(value: String): CodigoSrv = CodigoSrv.fromApi(value)

    @TypeConverter fun fromEstadoTourplan(value: EstadoTourplan): String = value.name
    @TypeConverter fun toEstadoTourplan(value: String): EstadoTourplan = EstadoTourplan.fromApi(value)

    @TypeConverter fun fromStatusPL(value: StatusPL?): String? = value?.name
    @TypeConverter fun toStatusPL(value: String?): StatusPL? = StatusPL.fromApi(value)

    @TypeConverter fun fromCodigoIdioma(value: CodigoIdioma?): String? = value?.name
    @TypeConverter fun toCodigoIdioma(value: String?): CodigoIdioma? = CodigoIdioma.fromApi(value)

    @TypeConverter fun fromTipoVehiculo(value: TipoVehiculo?): String? = value?.name
    @TypeConverter fun toTipoVehiculo(value: String?): TipoVehiculo? = TipoVehiculo.fromApi(value)

    @TypeConverter fun fromStringList(value: List<String>): String = gson.toJson(value)
    @TypeConverter fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter fun fromPasajeroList(value: List<Pasajero>): String = gson.toJson(value)
    @TypeConverter fun toPasajeroList(value: String): List<Pasajero> {
        val type = object : TypeToken<List<Pasajero>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter fun fromParticipacionList(value: List<ParticipacionServicio>): String = gson.toJson(value)
    @TypeConverter fun toParticipacionList(value: String): List<ParticipacionServicio> {
        val type = object : TypeToken<List<ParticipacionServicio>>() {}.type
        return gson.fromJson(value, type)
    }
}
package com.example.linceops.network.dto

import com.google.gson.annotations.SerializedName

data class PaxDto(
    @SerializedName("texto") val texto: String? = null,
    @SerializedName("adultos") val adultos: Int? = 0,
    @SerializedName("ninos") val ninos: Int? = 0,
    @SerializedName("total") val total: Int? = null
)

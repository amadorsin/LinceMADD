package com.example.linceops.model

enum class CodigoOficina(val nombre: String) {
    SCL("Santiago"),
    PUX("Puerto Varas"),
    PUQ("Punta Arenas");

    companion object {
        fun fromApi(valor: String?): CodigoOficina =
            entries.firstOrNull { it.name.equals(valor, ignoreCase = true) } ?: SCL
    }
}

enum class CodigoIdioma(val nombre: String) {
    ES("Español"),
    EN("Inglés"),
    PT("Portugués"),
    DE("Alemán"),
    FR("Francés");

    companion object {
        fun fromApi(valor: String?): CodigoIdioma? =
            entries.firstOrNull { it.name.equals(valor, ignoreCase = true) }
    }
}

enum class TipoServicio(val srv: CodigoSrv, val etiqueta: String) {
    TRANSFER(CodigoSrv.TR, "Transfer"),
    GUIDED_SERVICE(CodigoSrv.GS, "Servicio Guiado"),
    EXCURSION(CodigoSrv.EX, "Excursión");

    companion object {
        fun fromApi(valor: String?): TipoServicio =
            entries.firstOrNull { it.name.equals(valor, ignoreCase = true) } ?: TRANSFER
    }
}

enum class CodigoSrv(val descripcion: String) {
    TR("Transfer"),
    GS("Guided Service"),
    EX("Excursion");

    companion object {
        fun fromApi(valor: String?): CodigoSrv =
            entries.firstOrNull { it.name.equals(valor, ignoreCase = true) } ?: TR
    }
}

/** SrvStatus y BkgStatus en Tourplan */
enum class EstadoTourplan(val descripcion: String) {
    CF("Confirmado"),
    RQ("En solicitud"),
    WL("Lista de espera"),
    XX("Cancelado");

    companion object {
        fun fromApi(valor: String?): EstadoTourplan =
            entries.firstOrNull { it.name.equals(valor, ignoreCase = true) } ?: RQ
    }
}

/** StatusPL de asignación del staff en Southbound */
enum class StatusPL(val descripcion: String) {
    CF("Confirmado"),
    RQ("En solicitud"),
    DL("Eliminado"),
    CX("Cancelado");

    companion object {
        fun fromApi(valor: String?): StatusPL? =
            entries.firstOrNull { it.name.equals(valor, ignoreCase = true) }
    }
}

enum class TipoVehiculo(val etiqueta: String) {
    SEDAN("Sedán"),
    VAN("Van"),
    MINIBUS("Minibús"),
    BUS("Bus");

    companion object {
        fun fromApi(valor: String?): TipoVehiculo? =
            entries.firstOrNull { it.name.equals(valor, ignoreCase = true) }
    }
}
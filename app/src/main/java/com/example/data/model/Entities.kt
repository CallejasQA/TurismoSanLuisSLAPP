package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["email"], unique = true)]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val email: String,
    val passwordHash: String,
    val rol: String, // "admin", "propietario", "cliente"
    val estado: String = "activo", // "activo", "inactivo"
    val creadoEn: String
)

@Entity(
    tableName = "clientes_perfiles",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["usuarioId"], unique = true)]
)
data class ClientePerfilEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usuarioId: Int,
    val primerNombre: String,
    val segundoNombre: String = "",
    val primerApellido: String,
    val cedula: String = "",
    val telefonoCodigo: String = "+57",
    val telefonoNumero: String,
    val municipioOrigen: String = "",
    val creadoEn: String
)

@Entity(
    tableName = "afiliados",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["usuarioId"])]
)
data class AfiliadoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val usuarioId: Int,
    val nombreNegocio: String,
    val tipo: String = "Finca", // Finca, Glamping, Hotel, Cabaña, Ecohotel
    val descripcion: String = "",
    val direccion: String = "",
    val estado: String = "pendiente", // pendiente, aprobado, rechazado
    val solicitadoEn: String
)

@Entity(
    tableName = "alojamientos",
    foreignKeys = [
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["propietarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["propietarioId"])]
)
data class AlojamientoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val propietarioId: Int,
    val nombre: String,
    val descripcion: String = "",
    val ubicacion: String = "",
    val precioNoche: Double = 0.0,
    val rangoPrecio: String = "",
    val imagen: String = "",
    val destacadoSlider: Boolean = false,
    val estado: String = "pendiente", // pendiente, aprobado, rechazado, inactivo, activo
    val creadoEn: String
)

@Entity(
    tableName = "servicios",
    indices = [Index(value = ["nombre"], unique = true)]
)
data class ServicioEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val creadoEn: String
)

@Entity(
    tableName = "alojamiento_servicio",
    primaryKeys = ["alojamientoId", "servicioId"],
    foreignKeys = [
        ForeignKey(
            entity = AlojamientoEntity::class,
            parentColumns = ["id"],
            childColumns = ["alojamientoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ServicioEntity::class,
            parentColumns = ["id"],
            childColumns = ["servicioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["servicioId"])]
)
data class AlojamientoServicioCrossRef(
    val alojamientoId: Int,
    val servicioId: Int
)

@Entity(
    tableName = "reservas",
    foreignKeys = [
        ForeignKey(
            entity = AlojamientoEntity::class,
            parentColumns = ["id"],
            childColumns = ["alojamientoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["clienteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["alojamientoId"]), Index(value = ["clienteId"])]
)
data class ReservaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val alojamientoId: Int,
    val clienteId: Int,
    val fechaInicio: String, // YYYY-MM-DD
    val fechaFin: String,    // YYYY-MM-DD
    val total: Double,
    val estado: String = "pendiente", // pendiente, confirmada, cancelada, finalizada
    val creadoEn: String
)

@Entity(
    tableName = "valoraciones",
    foreignKeys = [
        ForeignKey(
            entity = ReservaEntity::class,
            parentColumns = ["id"],
            childColumns = ["reservaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AlojamientoEntity::class,
            parentColumns = ["id"],
            childColumns = ["alojamientoId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UsuarioEntity::class,
            parentColumns = ["id"],
            childColumns = ["clienteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["reservaId"], unique = true),
        Index(value = ["alojamientoId"]),
        Index(value = ["clienteId"])
    ]
)
data class ValoracionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val reservaId: Int,
    val alojamientoId: Int,
    val clienteId: Int,
    val estrellas: Int,
    val comentario: String = "",
    val creadoEn: String
)

@Entity(tableName = "settings")
data class SettingEntity(
    @PrimaryKey val key: String,
    val value: String
)

// Domain / UI composite models

data class AlojamientoDetalle(
    val alojamiento: AlojamientoEntity,
    val nombreNegocio: String,
    val correoPropietario: String,
    val servicios: List<ServicioEntity>,
    val promedioEstrellas: Double,
    val totalValoraciones: Int
)

data class AfiliadoDetalle(
    val afiliado: AfiliadoEntity,
    val correoPropietario: String
)

data class ReservaDetalle(
    val reserva: ReservaEntity,
    val alojamientoNombre: String,
    val alojamientoUbicacion: String,
    val propietarioId: Int,
    val propietarioEmail: String,
    val clienteNombreCompleto: String,
    val clienteEmail: String
)

data class ValoracionDetalle(
    val valoracion: ValoracionEntity,
    val alojamientoNombre: String,
    val propietarioId: Int,
    val propietarioEmail: String,
    val clienteNombreCompleto: String,
    val clienteEmail: String
)

data class ClienteDetalle(
    val usuario: UsuarioEntity,
    val perfil: ClientePerfilEntity
)

data class AgendaDiaResumen(
    val fecha: String, // YYYY-MM-DD
    val numeroDia: String,
    val nombreDia: String,
    val reservas: List<ReservaDetalle>
)

data class AgendaResumenMensual(
    val dias: List<AgendaDiaResumen>,
    val diasConReservas: Int,
    val diasSinReservas: Int,
    val ocupacionPorcentaje: Int,
    val totalReservasEnDias: Int
)

sealed interface AuthResult {
    data class Success(val usuario: UsuarioEntity, val message: String = "") : AuthResult
    data class Error(val message: String) : AuthResult
}

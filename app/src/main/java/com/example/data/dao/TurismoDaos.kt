package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AfiliadoEntity
import com.example.data.model.AlojamientoEntity
import com.example.data.model.AlojamientoServicioCrossRef
import com.example.data.model.ClientePerfilEntity
import com.example.data.model.ReservaEntity
import com.example.data.model.ServicioEntity
import com.example.data.model.SettingEntity
import com.example.data.model.UsuarioEntity
import com.example.data.model.ValoracionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {
    @Query("SELECT * FROM usuarios ORDER BY creadoEn DESC")
    fun observeAll(): Flow<List<UsuarioEntity>>

    @Query("SELECT * FROM usuarios WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun findById(id: Int): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE email = :email AND id != :ignoreId LIMIT 1")
    suspend fun findByEmailIgnoring(email: String, ignoreId: Int): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE rol = 'cliente' ORDER BY creadoEn DESC")
    fun observeClientes(): Flow<List<UsuarioEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(usuario: UsuarioEntity): Long

    @Update
    suspend fun update(usuario: UsuarioEntity)

    @Query("DELETE FROM usuarios WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun count(): Int
}

@Dao
interface ClientePerfilDao {
    @Query("SELECT * FROM clientes_perfiles")
    fun observeAll(): Flow<List<ClientePerfilEntity>>

    @Query("SELECT * FROM clientes_perfiles WHERE usuarioId = :usuarioId LIMIT 1")
    suspend fun findByUsuarioId(usuarioId: Int): ClientePerfilEntity?

    @Query("SELECT * FROM clientes_perfiles WHERE cedula = :cedula LIMIT 1")
    suspend fun findByCedula(cedula: String): ClientePerfilEntity?

    @Query("SELECT * FROM clientes_perfiles WHERE cedula = :cedula AND usuarioId != :ignoreUsuarioId LIMIT 1")
    suspend fun findByCedulaIgnoring(cedula: String, ignoreUsuarioId: Int): ClientePerfilEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(perfil: ClientePerfilEntity): Long

    @Update
    suspend fun update(perfil: ClientePerfilEntity)
}

@Dao
interface AfiliadoDao {
    @Query("SELECT * FROM afiliados ORDER BY solicitadoEn DESC")
    fun observeAll(): Flow<List<AfiliadoEntity>>

    @Query("SELECT * FROM afiliados WHERE estado = 'pendiente' ORDER BY solicitadoEn DESC")
    fun observePendientes(): Flow<List<AfiliadoEntity>>

    @Query("SELECT * FROM afiliados WHERE usuarioId = :usuarioId LIMIT 1")
    suspend fun findByUsuarioId(usuarioId: Int): AfiliadoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(afiliado: AfiliadoEntity): Long

    @Query("UPDATE afiliados SET estado = :estado WHERE id = :id")
    suspend fun updateEstado(id: Int, estado: String)
}

@Dao
interface AlojamientoDao {
    @Query("SELECT * FROM alojamientos ORDER BY creadoEn DESC")
    fun observeAll(): Flow<List<AlojamientoEntity>>

    @Query("SELECT * FROM alojamientos WHERE propietarioId = :propietarioId ORDER BY creadoEn DESC")
    fun observeByPropietario(propietarioId: Int): Flow<List<AlojamientoEntity>>

    @Query("SELECT * FROM alojamientos WHERE id = :id LIMIT 1")
    suspend fun findById(id: Int): AlojamientoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alojamiento: AlojamientoEntity): Long

    @Update
    suspend fun update(alojamiento: AlojamientoEntity)

    @Query("DELETE FROM alojamientos WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("UPDATE alojamientos SET estado = :estado WHERE id = :id")
    suspend fun updateEstado(id: Int, estado: String)

    @Query("UPDATE alojamientos SET destacadoSlider = :destacado WHERE id = :id AND estado IN ('aprobado', 'activo')")
    suspend fun updateDestacadoSlider(id: Int, destacado: Boolean)
}

@Dao
interface ServicioDao {
    @Query("SELECT * FROM servicios ORDER BY nombre ASC")
    fun observeAll(): Flow<List<ServicioEntity>>

    @Query("SELECT * FROM servicios WHERE id = :id LIMIT 1")
    suspend fun findById(id: Int): ServicioEntity?

    @Query("SELECT * FROM servicios WHERE LOWER(nombre) = LOWER(:nombre) LIMIT 1")
    suspend fun findByNameIgnoreCase(nombre: String): ServicioEntity?

    @Query("SELECT * FROM servicios WHERE LOWER(nombre) = LOWER(:nombre) AND id != :ignoreId LIMIT 1")
    suspend fun findByNameIgnoreCaseIgnoring(nombre: String, ignoreId: Int): ServicioEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(servicio: ServicioEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(servicios: List<ServicioEntity>)

    @Query("UPDATE servicios SET nombre = :nombre WHERE id = :id")
    suspend fun updateNombre(id: Int, nombre: String)

    @Query("DELETE FROM servicios WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM alojamiento_servicio")
    fun observeAllCrossRefs(): Flow<List<AlojamientoServicioCrossRef>>

    @Query("SELECT servicioId FROM alojamiento_servicio WHERE alojamientoId = :alojamientoId")
    suspend fun getServiceIdsForAlojamiento(alojamientoId: Int): List<Int>

    @Query("DELETE FROM alojamiento_servicio WHERE alojamientoId = :alojamientoId")
    suspend fun clearServicesForAlojamiento(alojamientoId: Int)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRefs(refs: List<AlojamientoServicioCrossRef>)
}

@Dao
interface ReservaDao {
    @Query("SELECT * FROM reservas ORDER BY creadoEn DESC")
    fun observeAll(): Flow<List<ReservaEntity>>

    @Query("SELECT * FROM reservas WHERE clienteId = :clienteId ORDER BY creadoEn DESC")
    fun observeByCliente(clienteId: Int): Flow<List<ReservaEntity>>

    @Query("SELECT * FROM reservas WHERE id = :id LIMIT 1")
    suspend fun findById(id: Int): ReservaEntity?

    @Query("SELECT * FROM reservas WHERE alojamientoId = :alojamientoId AND clienteId = :clienteId AND estado = 'finalizada' ORDER BY fechaFin DESC, creadoEn DESC LIMIT 1")
    suspend fun findFinalizadaPorClienteYAlojamiento(alojamientoId: Int, clienteId: Int): ReservaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reserva: ReservaEntity): Long

    @Query("UPDATE reservas SET estado = :estado WHERE id = :id")
    suspend fun updateEstado(id: Int, estado: String)
}

@Dao
interface ValoracionDao {
    @Query("SELECT * FROM valoraciones ORDER BY creadoEn DESC")
    fun observeAll(): Flow<List<ValoracionEntity>>

    @Query("SELECT * FROM valoraciones WHERE clienteId = :clienteId AND alojamientoId = :alojamientoId LIMIT 1")
    suspend fun findByClienteYAlojamiento(clienteId: Int, alojamientoId: Int): ValoracionEntity?

    @Query("SELECT COUNT(*) FROM valoraciones WHERE reservaId = :reservaId AND clienteId = :clienteId")
    suspend fun countForReservaAndCliente(reservaId: Int, clienteId: Int): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(valoracion: ValoracionEntity): Long

    @Query("UPDATE valoraciones SET comentario = :comentario WHERE id = :id")
    suspend fun updateComentario(id: Int, comentario: String)

    @Query("DELETE FROM valoraciones WHERE id = :id")
    suspend fun deleteById(id: Int)
}

@Dao
interface SettingDao {
    @Query("SELECT * FROM settings WHERE key = :key LIMIT 1")
    fun observeByKey(key: String): Flow<SettingEntity?>

    @Query("SELECT * FROM settings WHERE key = :key LIMIT 1")
    suspend fun getByKey(key: String): SettingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(setting: SettingEntity)
}

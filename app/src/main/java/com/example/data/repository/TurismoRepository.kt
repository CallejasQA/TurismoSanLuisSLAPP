package com.example.data.repository

import android.content.Context
import android.net.Uri
import com.example.data.database.TurismoDatabase
import com.example.data.model.AfiliadoDetalle
import com.example.data.model.AfiliadoEntity
import com.example.data.model.AgendaDiaResumen
import com.example.data.model.AgendaResumenMensual
import com.example.data.model.AlojamientoDetalle
import com.example.data.model.AlojamientoEntity
import com.example.data.model.AlojamientoServicioCrossRef
import com.example.data.model.AuthResult
import com.example.data.model.ClienteDetalle
import com.example.data.model.ClientePerfilEntity
import com.example.data.model.ReservaDetalle
import com.example.data.model.ReservaEntity
import com.example.data.model.ServicioEntity
import com.example.data.model.SettingEntity
import com.example.data.model.UsuarioEntity
import com.example.data.model.ValoracionDetalle
import com.example.data.model.ValoracionEntity
import com.example.data.validation.Validators
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class TurismoRepository(private val db: TurismoDatabase) {

    private val usuarioDao = db.usuarioDao()
    private val clientePerfilDao = db.clientePerfilDao()
    private val afiliadoDao = db.afiliadoDao()
    private val alojamientoDao = db.alojamientoDao()
    private val servicioDao = db.servicioDao()
    private val reservaDao = db.reservaDao()
    private val valoracionDao = db.valoracionDao()
    private val settingDao = db.settingDao()

    private val tsFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private fun nowString(): String = LocalDateTime.now().format(tsFormatter)

    suspend fun initializeSeed() {
        TurismoDatabase.ensureSeeded(db)
    }

    // --- Settings ---

    fun observeBackgroundImage(): Flow<String> {
        return settingDao.observeByKey(TurismoDatabase.KEY_BACKGROUND_IMAGE).map {
            it?.value?.takeIf { path -> path.isNotBlank() } ?: TurismoDatabase.DEFAULT_BACKGROUND_IMAGE
        }
    }

    suspend fun saveBackgroundImage(imagePath: String) {
        settingDao.upsert(SettingEntity(TurismoDatabase.KEY_BACKGROUND_IMAGE, imagePath))
    }

    suspend fun resetBackgroundImage() {
        settingDao.upsert(SettingEntity(TurismoDatabase.KEY_BACKGROUND_IMAGE, TurismoDatabase.DEFAULT_BACKGROUND_IMAGE))
    }

    // --- Authentication & Registration ---

    suspend fun iniciarSesion(email: String, password: String): AuthResult {
        val cleanEmail = email.trim()
        val u = usuarioDao.findByEmail(cleanEmail)
        if (u == null || u.passwordHash != TurismoDatabase.sha256(password)) {
            return AuthResult.Error("Credenciales inválidas")
        }
        if (u.estado != "activo") {
            return AuthResult.Error("Tu cuenta se encuentra inactiva. Contacta al administrador.")
        }
        if (u.rol == "propietario") {
            val afiliacion = afiliadoDao.findByUsuarioId(u.id)
            if (afiliacion != null && afiliacion.estado != "aprobado") {
                val mensaje = if (afiliacion.estado == "pendiente") {
                    "Tu solicitud de afiliación aún no ha sido aprobada por el administrador."
                } else {
                    "Tu solicitud de afiliación fue rechazada. Contacta al administrador para más información."
                }
                return AuthResult.Error(mensaje)
            }
        }
        return AuthResult.Success(u)
    }

    suspend fun registrarAfiliado(
        nombreNegocio: String,
        tipo: String,
        direccion: String,
        descripcion: String,
        email: String,
        password: String
    ): Result<String> {
        val cleanEmail = email.replace(Regex("\\s+"), "")
        val errors = listOfNotNull(
            Validators.validateNombreNegocio(nombreNegocio).takeIf { !it.valid }?.message,
            Validators.validateTipoNegocio(tipo).takeIf { !it.valid }?.message,
            Validators.validateDireccion(direccion).takeIf { !it.valid }?.message,
            Validators.validateDescripcionNegocio(descripcion).takeIf { !it.valid }?.message,
            Validators.validateEmail(cleanEmail).takeIf { !it.valid }?.message,
            Validators.validatePassword(password, isRequired = true).takeIf { !it.valid }?.message
        )
        if (errors.isNotEmpty()) {
            return Result.failure(IllegalArgumentException(errors.joinToString(" ")))
        }
        if (usuarioDao.findByEmail(cleanEmail) != null) {
            return Result.failure(IllegalArgumentException("El correo ya está registrado"))
        }

        val now = nowString()
        val userId = usuarioDao.insert(
            UsuarioEntity(
                nombre = nombreNegocio.trim(),
                email = cleanEmail,
                passwordHash = TurismoDatabase.sha256(password),
                rol = "propietario",
                estado = "activo",
                creadoEn = now
            )
        ).toInt()

        afiliadoDao.insert(
            AfiliadoEntity(
                usuarioId = userId,
                nombreNegocio = nombreNegocio.trim(),
                tipo = tipo.trim(),
                descripcion = descripcion.trim(),
                direccion = direccion.trim(),
                estado = "pendiente",
                solicitadoEn = now
            )
        )
        return Result.success("Solicitud enviada. Un administrador la revisará.")
    }

    suspend fun registrarCliente(
        primerNombre: String,
        segundoNombre: String,
        primerApellido: String,
        cedula: String,
        telefonoCodigo: String,
        telefonoNumero: String,
        email: String,
        municipioOrigen: String,
        password: String
    ): Result<String> {
        val cleanEmail = email.replace(Regex("\\s+"), "")
        val cleanCedula = cedula.trim()
        val errors = listOfNotNull(
            Validators.validateNombrePersona(primerNombre, "El primer nombre", 3, 30, optional = false).takeIf { !it.valid }?.message,
            Validators.validateNombrePersona(segundoNombre, "El segundo nombre", 2, 30, optional = true).takeIf { !it.valid }?.message,
            Validators.validateNombrePersona(primerApellido, "El primer apellido", 3, 30, optional = false).takeIf { !it.valid }?.message,
            Validators.validateCedula(cleanCedula, required = true).takeIf { !it.valid }?.message,
            Validators.validateTelefonoCodigo(telefonoCodigo).takeIf { !it.valid }?.message,
            Validators.validateTelefonoNumero(telefonoNumero).takeIf { !it.valid }?.message,
            Validators.validateEmail(cleanEmail).takeIf { !it.valid }?.message,
            Validators.validateMunicipio(municipioOrigen).takeIf { !it.valid }?.message,
            Validators.validatePassword(password, isRequired = true).takeIf { !it.valid }?.message
        )
        if (errors.isNotEmpty()) {
            return Result.failure(IllegalArgumentException(errors.joinToString(" ")))
        }
        if (usuarioDao.findByEmail(cleanEmail) != null) {
            return Result.failure(IllegalArgumentException("El correo ya está registrado."))
        }
        if (clientePerfilDao.findByCedula(cleanCedula) != null) {
            return Result.failure(IllegalArgumentException("La cédula ya está registrada."))
        }

        val now = nowString()
        val nombreCompuesto = "${primerNombre.trim()} ${primerApellido.trim()}".trim()
        val userId = usuarioDao.insert(
            UsuarioEntity(
                nombre = nombreCompuesto,
                email = cleanEmail,
                passwordHash = TurismoDatabase.sha256(password),
                rol = "cliente",
                estado = "activo",
                creadoEn = now
            )
        ).toInt()

        clientePerfilDao.insert(
            ClientePerfilEntity(
                usuarioId = userId,
                primerNombre = primerNombre.trim(),
                segundoNombre = segundoNombre.trim(),
                primerApellido = primerApellido.trim(),
                cedula = cleanCedula,
                telefonoCodigo = telefonoCodigo.trim().ifEmpty { "+57" },
                telefonoNumero = telefonoNumero.trim(),
                municipioOrigen = municipioOrigen.trim(),
                creadoEn = now
            )
        )
        return Result.success("Registro exitoso. Ahora puedes iniciar sesión.")
    }

    // --- Catalog & Accommodations ---

    private data class CatalogBundle(
        val alojamientos: List<AlojamientoEntity>,
        val usuarios: List<UsuarioEntity>,
        val afiliados: List<AfiliadoEntity>,
        val servicios: List<ServicioEntity>,
        val crossRefs: List<AlojamientoServicioCrossRef>,
        val valoraciones: List<ValoracionEntity>
    )

    fun observeAllAlojamientosDetalle(): Flow<List<AlojamientoDetalle>> {
        val baseFlow = combine(
            alojamientoDao.observeAll(),
            usuarioDao.observeAll(),
            afiliadoDao.observeAll(),
            servicioDao.observeAll(),
            servicioDao.observeAllCrossRefs()
        ) { alojamientos, usuarios, afiliados, servicios, crossRefs ->
            CatalogBundle(alojamientos, usuarios, afiliados, servicios, crossRefs, emptyList())
        }

        return combine(baseFlow, valoracionDao.observeAll()) { bundle, valoraciones ->
            val userMap = bundle.usuarios.associateBy { it.id }
            val afiliadoApprovedByUser = bundle.afiliados
                .filter { it.estado == "aprobado" }
                .associateBy { it.usuarioId }
            val serviceMap = bundle.servicios.associateBy { it.id }
            val serviceIdsByAlojamiento = bundle.crossRefs.groupBy({ it.alojamientoId }, { it.servicioId })
            val ratingsByAlojamiento = valoraciones.groupBy { it.alojamientoId }

            bundle.alojamientos.map { aloj ->
                val owner = userMap[aloj.propietarioId]
                val afiliado = afiliadoApprovedByUser[aloj.propietarioId]
                val nombreNegocio = afiliado?.nombreNegocio ?: owner?.nombre ?: "Anfitrión San Luis"
                val servs = (serviceIdsByAlojamiento[aloj.id] ?: emptyList())
                    .mapNotNull { serviceMap[it] }
                    .sortedBy { it.nombre }
                val vals = ratingsByAlojamiento[aloj.id] ?: emptyList()
                val avg = if (vals.isNotEmpty()) vals.map { it.estrellas }.average() else 0.0
                AlojamientoDetalle(
                    alojamiento = aloj,
                    nombreNegocio = nombreNegocio,
                    correoPropietario = owner?.email ?: "",
                    servicios = servs,
                    promedioEstrellas = avg,
                    totalValoraciones = vals.size
                )
            }
        }
    }

    fun observeServicios(): Flow<List<ServicioEntity>> = servicioDao.observeAll()

    suspend fun getServiceIdsForAlojamiento(alojamientoId: Int): List<Int> =
        servicioDao.getServiceIdsForAlojamiento(alojamientoId)

    suspend fun getAlojamientoById(id: Int): AlojamientoEntity? = alojamientoDao.findById(id)

    suspend fun crearAlojamiento(
        propietarioId: Int,
        nombre: String,
        descripcion: String,
        ubicacion: String,
        precioNoche: Double,
        rangoPrecio: String,
        imagen: String,
        serviceIds: List<Int>
    ): Result<Int> {
        if (nombre.trim().isEmpty()) {
            return Result.failure(IllegalArgumentException("El nombre del alojamiento es obligatorio."))
        }
        if (precioNoche <= 0) {
            return Result.failure(IllegalArgumentException("El precio por noche debe ser mayor a 0."))
        }
        val id = alojamientoDao.insert(
            AlojamientoEntity(
                propietarioId = propietarioId,
                nombre = nombre.trim(),
                descripcion = descripcion.trim(),
                ubicacion = ubicacion.trim(),
                precioNoche = precioNoche,
                rangoPrecio = rangoPrecio.trim(),
                imagen = imagen,
                destacadoSlider = false,
                estado = "pendiente",
                creadoEn = nowString()
            )
        ).toInt()
        syncServices(id, serviceIds)
        return Result.success(id)
    }

    suspend fun actualizarAlojamiento(
        alojamientoId: Int,
        nombre: String,
        descripcion: String,
        ubicacion: String,
        precioNoche: Double,
        rangoPrecio: String,
        imagen: String,
        serviceIds: List<Int>
    ): Result<Unit> {
        val existing = alojamientoDao.findById(alojamientoId)
            ?: return Result.failure(IllegalArgumentException("Alojamiento no encontrado."))
        if (nombre.trim().isEmpty()) {
            return Result.failure(IllegalArgumentException("El nombre del alojamiento es obligatorio."))
        }
        if (precioNoche <= 0) {
            return Result.failure(IllegalArgumentException("El precio por noche debe ser mayor a 0."))
        }
        alojamientoDao.update(
            existing.copy(
                nombre = nombre.trim(),
                descripcion = descripcion.trim(),
                ubicacion = ubicacion.trim(),
                precioNoche = precioNoche,
                rangoPrecio = rangoPrecio.trim(),
                imagen = imagen
            )
        )
        syncServices(alojamientoId, serviceIds)
        return Result.success(Unit)
    }

    suspend fun eliminarAlojamiento(alojamientoId: Int) {
        alojamientoDao.deleteById(alojamientoId)
    }

    private suspend fun syncServices(alojamientoId: Int, serviceIds: List<Int>) {
        servicioDao.clearServicesForAlojamiento(alojamientoId)
        val distinct = serviceIds.distinct().filter { it > 0 }
        if (distinct.isNotEmpty()) {
            servicioDao.insertCrossRefs(distinct.map { AlojamientoServicioCrossRef(alojamientoId, it) })
        }
    }

    // --- Admin Actions ---

    fun observeAfiliadosDetalle(): Flow<List<AfiliadoDetalle>> {
        return combine(afiliadoDao.observeAll(), usuarioDao.observeAll()) { afiliados, usuarios ->
            val userMap = usuarios.associateBy { it.id }
            afiliados.map { af ->
                AfiliadoDetalle(
                    afiliado = af,
                    correoPropietario = userMap[af.usuarioId]?.email ?: ""
                )
            }
        }
    }

    suspend fun cambiarEstadoAfiliacion(afiliadoId: Int, estado: String) {
        afiliadoDao.updateEstado(afiliadoId, estado)
    }

    suspend fun cambiarEstadoAlojamiento(alojamientoId: Int, estado: String) {
        alojamientoDao.updateEstado(alojamientoId, estado)
        if (estado !in listOf("aprobado", "activo")) {
            alojamientoDao.updateDestacadoSlider(alojamientoId, false)
        }
    }

    suspend fun toggleSliderAlojamiento(alojamientoId: Int, activar: Boolean) {
        alojamientoDao.updateDestacadoSlider(alojamientoId, activar)
    }

    suspend fun crearServicio(rawNombre: String): Result<Unit> {
        val nombre = rawNombre.trim().replace(Regex("\\s+"), " ")
        if (nombre.isEmpty() || nombre.length > 60) {
            return Result.failure(IllegalArgumentException("El nombre debe tener entre 1 y 60 caracteres."))
        }
        if (servicioDao.findByNameIgnoreCase(nombre) != null) {
            return Result.failure(IllegalArgumentException("Ya existe un servicio con ese nombre."))
        }
        servicioDao.insert(ServicioEntity(nombre = nombre, creadoEn = nowString()))
        return Result.success(Unit)
    }

    suspend fun actualizarServicio(id: Int, rawNombre: String): Result<Unit> {
        val nombre = rawNombre.trim().replace(Regex("\\s+"), " ")
        if (nombre.isEmpty() || nombre.length > 60) {
            return Result.failure(IllegalArgumentException("El nombre debe tener entre 1 y 60 caracteres."))
        }
        if (servicioDao.findByNameIgnoreCaseIgnoring(nombre, id) != null) {
            return Result.failure(IllegalArgumentException("Ya existe otro servicio con ese nombre."))
        }
        servicioDao.updateNombre(id, nombre)
        return Result.success(Unit)
    }

    suspend fun eliminarServicio(id: Int) {
        servicioDao.deleteById(id)
    }

    // --- Admin Client CRUD ---

    fun observeClientesDetalle(): Flow<List<ClienteDetalle>> {
        return combine(usuarioDao.observeClientes(), clientePerfilDao.observeAll()) { users, profiles ->
            val profileMap = profiles.associateBy { it.usuarioId }
            users.mapNotNull { u ->
                val p = profileMap[u.id] ?: ClientePerfilEntity(
                    usuarioId = u.id,
                    primerNombre = u.nombre,
                    primerApellido = "",
                    telefonoNumero = "",
                    creadoEn = u.creadoEn
                )
                ClienteDetalle(u, p)
            }
        }
    }

    suspend fun getClienteDetalle(usuarioId: Int): ClienteDetalle? {
        val user = usuarioDao.findById(usuarioId) ?: return null
        val perfil = clientePerfilDao.findByUsuarioId(usuarioId) ?: return null
        return ClienteDetalle(user, perfil)
    }

    suspend fun crearClienteAdmin(
        primerNombre: String,
        segundoNombre: String,
        primerApellido: String,
        cedula: String,
        telefonoCodigo: String,
        telefonoNumero: String,
        email: String,
        municipioOrigen: String,
        password: String,
        estado: String
    ): Result<Unit> {
        val cleanEmail = email.trim()
        val cleanCedula = cedula.trim()
        val errors = listOfNotNull(
            Validators.validateNombrePersona(primerNombre, "El primer nombre", 3, 20, optional = false).takeIf { !it.valid }?.message,
            Validators.validateNombrePersona(primerApellido, "El primer apellido", 3, 20, optional = false).takeIf { !it.valid }?.message,
            Validators.validateEmail(cleanEmail).takeIf { !it.valid }?.message,
            Validators.validateTelefonoNumero(telefonoNumero).takeIf { !it.valid }?.message,
            Validators.validateCedula(cleanCedula, required = false).takeIf { !it.valid }?.message,
            Validators.validatePassword(password.ifEmpty { "123456" }, isRequired = true, maxLen = 40).takeIf { !it.valid }?.message
        )
        if (errors.isNotEmpty()) return Result.failure(IllegalArgumentException(errors.joinToString(" ")))
        if (usuarioDao.findByEmail(cleanEmail) != null) {
            return Result.failure(IllegalArgumentException("El correo ya está registrado."))
        }
        if (cleanCedula.isNotEmpty() && clientePerfilDao.findByCedula(cleanCedula) != null) {
            return Result.failure(IllegalArgumentException("La cédula ya está registrada."))
        }

        val now = nowString()
        val actualPass = password.ifBlank { "123456" }
        val userId = usuarioDao.insert(
            UsuarioEntity(
                nombre = "${primerNombre.trim()} ${primerApellido.trim()}".trim(),
                email = cleanEmail,
                passwordHash = TurismoDatabase.sha256(actualPass),
                rol = "cliente",
                estado = if (estado in listOf("activo", "inactivo")) estado else "activo",
                creadoEn = now
            )
        ).toInt()
        clientePerfilDao.insert(
            ClientePerfilEntity(
                usuarioId = userId,
                primerNombre = primerNombre.trim(),
                segundoNombre = segundoNombre.trim(),
                primerApellido = primerApellido.trim(),
                cedula = cleanCedula,
                telefonoCodigo = telefonoCodigo.trim().ifEmpty { "+57" },
                telefonoNumero = telefonoNumero.trim(),
                municipioOrigen = municipioOrigen.trim(),
                creadoEn = now
            )
        )
        return Result.success(Unit)
    }

    suspend fun actualizarClienteAdmin(
        usuarioId: Int,
        primerNombre: String,
        segundoNombre: String,
        primerApellido: String,
        cedula: String,
        telefonoCodigo: String,
        telefonoNumero: String,
        email: String,
        municipioOrigen: String,
        password: String,
        estado: String
    ): Result<Unit> {
        val existingUser = usuarioDao.findById(usuarioId)
            ?: return Result.failure(IllegalArgumentException("Cliente no encontrado."))
        val existingPerfil = clientePerfilDao.findByUsuarioId(usuarioId)
            ?: return Result.failure(IllegalArgumentException("Perfil de cliente no encontrado."))

        val cleanEmail = email.trim()
        val cleanCedula = cedula.trim()
        val errors = listOfNotNull(
            Validators.validateNombrePersona(primerNombre, "El primer nombre", 3, 20, optional = false).takeIf { !it.valid }?.message,
            Validators.validateNombrePersona(primerApellido, "El primer apellido", 3, 20, optional = false).takeIf { !it.valid }?.message,
            Validators.validateEmail(cleanEmail).takeIf { !it.valid }?.message,
            Validators.validateTelefonoNumero(telefonoNumero).takeIf { !it.valid }?.message,
            Validators.validateCedula(cleanCedula, required = false).takeIf { !it.valid }?.message,
            Validators.validatePassword(password, isRequired = false, maxLen = 40).takeIf { !it.valid }?.message
        )
        if (errors.isNotEmpty()) return Result.failure(IllegalArgumentException(errors.joinToString(" ")))
        if (usuarioDao.findByEmailIgnoring(cleanEmail, usuarioId) != null) {
            return Result.failure(IllegalArgumentException("El correo ya está registrado."))
        }
        if (cleanCedula.isNotEmpty() && clientePerfilDao.findByCedulaIgnoring(cleanCedula, usuarioId) != null) {
            return Result.failure(IllegalArgumentException("La cédula ya está registrada."))
        }

        val newHash = if (password.isNotBlank()) TurismoDatabase.sha256(password) else existingUser.passwordHash
        usuarioDao.update(
            existingUser.copy(
                nombre = "${primerNombre.trim()} ${primerApellido.trim()}".trim(),
                email = cleanEmail,
                estado = if (estado in listOf("activo", "inactivo")) estado else "activo",
                passwordHash = newHash
            )
        )
        clientePerfilDao.update(
            existingPerfil.copy(
                primerNombre = primerNombre.trim(),
                segundoNombre = segundoNombre.trim(),
                primerApellido = primerApellido.trim(),
                cedula = cleanCedula,
                telefonoCodigo = telefonoCodigo.trim().ifEmpty { "+57" },
                telefonoNumero = telefonoNumero.trim(),
                municipioOrigen = municipioOrigen.trim()
            )
        )
        return Result.success(Unit)
    }

    suspend fun eliminarClienteAdmin(usuarioId: Int) {
        usuarioDao.deleteById(usuarioId)
    }

    // --- Bookings & Agenda ---

    fun observeAllReservasDetalle(): Flow<List<ReservaDetalle>> {
        return combine(
            reservaDao.observeAll(),
            alojamientoDao.observeAll(),
            usuarioDao.observeAll(),
            clientePerfilDao.observeAll()
        ) { reservas, alojamientos, usuarios, perfiles ->
            val alojMap = alojamientos.associateBy { it.id }
            val userMap = usuarios.associateBy { it.id }
            val perfilMap = perfiles.associateBy { it.usuarioId }
            reservas.map { r ->
                val aloj = alojMap[r.alojamientoId]
                val owner = aloj?.let { userMap[it.propietarioId] }
                val client = userMap[r.clienteId]
                val perfil = perfilMap[r.clienteId]
                val clientFullName = if (perfil != null) {
                    "${perfil.primerNombre} ${perfil.primerApellido}".trim()
                } else {
                    client?.nombre ?: "Huésped"
                }
                ReservaDetalle(
                    reserva = r,
                    alojamientoNombre = aloj?.nombre ?: "Alojamiento",
                    alojamientoUbicacion = aloj?.ubicacion ?: "",
                    propietarioId = aloj?.propietarioId ?: 0,
                    propietarioEmail = owner?.email ?: "",
                    clienteNombreCompleto = clientFullName,
                    clienteEmail = client?.email ?: ""
                )
            }
        }
    }

    suspend fun crearReserva(
        alojamientoId: Int,
        clienteId: Int,
        fechaInicio: String,
        fechaFin: String
    ): Result<Unit> {
        val aloj = alojamientoDao.findById(alojamientoId)
            ?: return Result.failure(IllegalArgumentException("Alojamiento no encontrado."))
        val ini = runCatching { LocalDate.parse(fechaInicio) }.getOrNull()
        val fin = runCatching { LocalDate.parse(fechaFin) }.getOrNull()
        if (ini == null || fin == null) {
            return Result.failure(IllegalArgumentException("Selecciona la fecha de inicio y fin válidas."))
        }
        if (!fin.isAfter(ini)) {
            return Result.failure(IllegalArgumentException("La fecha de fin debe ser posterior a la de inicio."))
        }
        val noches = ChronoUnit.DAYS.between(ini, fin)
        val total = noches * aloj.precioNoche
        reservaDao.insert(
            ReservaEntity(
                alojamientoId = alojamientoId,
                clienteId = clienteId,
                fechaInicio = fechaInicio,
                fechaFin = fechaFin,
                total = total,
                estado = "pendiente",
                creadoEn = nowString()
            )
        )
        return Result.success(Unit)
    }

    suspend fun cambiarEstadoReserva(reservaId: Int, estado: String) {
        val clean = estado.lowercase().trim()
        if (clean in listOf("pendiente", "confirmada", "cancelada", "finalizada")) {
            reservaDao.updateEstado(reservaId, clean)
        }
    }

    fun construirAgenda(
        reservas: List<ReservaDetalle>,
        mesStr: String,
        diaEspecifico: String = ""
    ): AgendaResumenMensual {
        val ym = runCatching { YearMonth.parse(mesStr) }.getOrElse { YearMonth.now() }
        val inicioMes = ym.atDay(1)
        val finMes = ym.atEndOfMonth()

        val diasMap = linkedMapOf<LocalDate, MutableList<ReservaDetalle>>()
        var cursor = inicioMes
        while (!cursor.isAfter(finMes)) {
            diasMap[cursor] = mutableListOf()
            cursor = cursor.plusDays(1)
        }

        for (r in reservas) {
            val ini = runCatching { LocalDate.parse(r.reserva.fechaInicio) }.getOrNull() ?: continue
            val fin = runCatching { LocalDate.parse(r.reserva.fechaFin) }.getOrNull() ?: continue
            if (fin.isBefore(inicioMes) || ini.isAfter(finMes)) continue

            var iter = if (ini.isAfter(inicioMes)) ini else inicioMes
            val iterEnd = if (fin.isBefore(finMes)) fin else finMes
            while (!iter.isAfter(iterEnd)) {
                diasMap[iter]?.add(r)
                iter = iter.plusDays(1)
            }
        }

        val spanishWeekdays = mapOf(
            "MONDAY" to "Lun",
            "TUESDAY" to "Mar",
            "WEDNESDAY" to "Mié",
            "THURSDAY" to "Jue",
            "FRIDAY" to "Vie",
            "SATURDAY" to "Sáb",
            "SUNDAY" to "Dom"
        )

        val entriesToBuild = if (diaEspecifico.isNotBlank()) {
            val specific = runCatching { LocalDate.parse(diaEspecifico) }.getOrNull()
            if (specific != null) {
                listOf(specific to (diasMap[specific] ?: mutableListOf()))
            } else {
                diasMap.entries.map { it.key to it.value }
            }
        } else {
            diasMap.entries.map { it.key to it.value }
        }

        val diasList = entriesToBuild.map { (date, list) ->
            AgendaDiaResumen(
                fecha = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                numeroDia = "%02d".format(date.dayOfMonth),
                nombreDia = spanishWeekdays[date.dayOfWeek.name] ?: "",
                reservas = list
            )
        }

        val totalDias = diasList.size
        val diasConReservas = diasList.count { it.reservas.isNotEmpty() }
        val diasSinReservas = (totalDias - diasConReservas).coerceAtLeast(0)
        val ocupacion = if (totalDias > 0) ((diasConReservas.toDouble() / totalDias) * 100).toInt() else 0
        val totalReservas = diasList.sumOf { it.reservas.size }

        return AgendaResumenMensual(
            dias = diasList,
            diasConReservas = diasConReservas,
            diasSinReservas = diasSinReservas,
            ocupacionPorcentaje = ocupacion,
            totalReservasEnDias = totalReservas
        )
    }

    // --- Ratings / Valoraciones ---

    fun observeAllValoracionesDetalle(): Flow<List<ValoracionDetalle>> {
        return combine(
            valoracionDao.observeAll(),
            alojamientoDao.observeAll(),
            usuarioDao.observeAll(),
            clientePerfilDao.observeAll()
        ) { valoraciones, alojamientos, usuarios, perfiles ->
            val alojMap = alojamientos.associateBy { it.id }
            val userMap = usuarios.associateBy { it.id }
            val perfilMap = perfiles.associateBy { it.usuarioId }

            valoraciones.map { v ->
                val aloj = alojMap[v.alojamientoId]
                val owner = aloj?.let { userMap[it.propietarioId] }
                val client = userMap[v.clienteId]
                val perfil = perfilMap[v.clienteId]
                val clientFullName = if (perfil != null) {
                    "${perfil.primerNombre} ${perfil.primerApellido}".trim()
                } else {
                    client?.nombre ?: "Huésped"
                }
                ValoracionDetalle(
                    valoracion = v,
                    alojamientoNombre = aloj?.nombre ?: "Alojamiento",
                    propietarioId = aloj?.propietarioId ?: 0,
                    propietarioEmail = owner?.email ?: "",
                    clienteNombreCompleto = clientFullName,
                    clienteEmail = client?.email ?: ""
                )
            }
        }
    }

    suspend fun getValoracionClienteParaAlojamiento(clienteId: Int, alojamientoId: Int): ValoracionEntity? =
        valoracionDao.findByClienteYAlojamiento(clienteId, alojamientoId)

    suspend fun getReservaFinalizadaCliente(alojamientoId: Int, clienteId: Int): ReservaEntity? =
        reservaDao.findFinalizadaPorClienteYAlojamiento(alojamientoId, clienteId)

    suspend fun calificarReserva(
        reservaId: Int,
        clienteId: Int,
        estrellas: Int,
        comentario: String
    ): Result<Unit> {
        val reserva = reservaDao.findById(reservaId)
            ?: return Result.failure(IllegalArgumentException("Reserva no encontrada."))
        if (reserva.clienteId != clienteId) {
            return Result.failure(IllegalArgumentException("No autorizado para calificar esta reserva."))
        }
        if (reserva.estado.lowercase() != "finalizada") {
            return Result.failure(IllegalArgumentException("Solo puedes calificar reservas finalizadas."))
        }
        if (valoracionDao.countForReservaAndCliente(reservaId, clienteId) > 0) {
            return Result.failure(IllegalArgumentException("Esta reserva ya fue calificada."))
        }
        if (estrellas !in 1..5) {
            return Result.failure(IllegalArgumentException("Selecciona una puntuación válida de 1 a 5."))
        }

        valoracionDao.insert(
            ValoracionEntity(
                reservaId = reservaId,
                alojamientoId = reserva.alojamientoId,
                clienteId = clienteId,
                estrellas = estrellas,
                comentario = comentario.trim().take(1000),
                creadoEn = nowString()
            )
        )
        return Result.success(Unit)
    }

    suspend fun actualizarComentarioValoracion(id: Int, comentario: String): Result<Unit> {
        val clean = comentario.trim().take(1000)
        if (clean.isEmpty()) {
            return Result.failure(IllegalArgumentException("El comentario no puede estar vacío."))
        }
        valoracionDao.updateComentario(id, clean)
        return Result.success(Unit)
    }

    suspend fun eliminarValoracion(id: Int) {
        valoracionDao.deleteById(id)
    }

    // --- Media Persistence Helper (Zero-permission Photo Picker copy) ---

    suspend fun copyPickedImageToInternalStorage(context: Context, uri: Uri, prefix: String): String =
        withContext(Dispatchers.IO) {
            val dir = File(context.filesDir, "uploads")
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, "${prefix}_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file.toURI().toString()
        }
}

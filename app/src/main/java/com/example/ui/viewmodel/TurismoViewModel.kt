package com.example.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.TurismoDatabase
import com.example.data.model.AfiliadoDetalle
import com.example.data.model.AgendaResumenMensual
import com.example.data.model.AlojamientoDetalle
import com.example.data.model.AlojamientoEntity
import com.example.data.model.AuthResult
import com.example.data.model.ClienteDetalle
import com.example.data.model.ReservaDetalle
import com.example.data.model.ReservaEntity
import com.example.data.model.ServicioEntity
import com.example.data.model.UsuarioEntity
import com.example.data.model.ValoracionDetalle
import com.example.data.model.ValoracionEntity
import com.example.data.repository.TurismoRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HomeFilterState(
    val ubicacion: String = "",
    val operador: String = "",
    val minEstrellas: Int? = null
)

data class SitioDetailCustomerState(
    val valoracionCliente: ValoracionEntity? = null,
    val reservaFinalizada: ReservaEntity? = null,
    val puedeValorar: Boolean = false
)

class TurismoViewModel(private val repository: TurismoRepository) : ViewModel() {

    private val _currentUser = MutableStateFlow<UsuarioEntity?>(null)
    val currentUser: StateFlow<UsuarioEntity?> = _currentUser.asStateFlow()

    private val _filterState = MutableStateFlow(HomeFilterState())
    val filterState: StateFlow<HomeFilterState> = _filterState.asStateFlow()

    private val _snackbarMessages = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val snackbarMessages = _snackbarMessages.asSharedFlow()

    val backgroundImage: StateFlow<String> = repository.observeBackgroundImage()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TurismoDatabase.DEFAULT_BACKGROUND_IMAGE)

    val allAlojamientos: StateFlow<List<AlojamientoDetalle>> = repository.observeAllAlojamientosDetalle()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val publicAlojamientosBase: StateFlow<List<AlojamientoDetalle>> = allAlojamientos
        .map { list -> list.filter { it.alojamiento.estado in listOf("aprobado", "activo") } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredPublicAlojamientos: StateFlow<List<AlojamientoDetalle>> = combine(
        publicAlojamientosBase,
        _filterState
    ) { base, filters ->
        base.filter { item ->
            val matchUbicacion = filters.ubicacion.isBlank() ||
                item.alojamiento.ubicacion.contains(filters.ubicacion.trim(), ignoreCase = true) ||
                item.alojamiento.nombre.contains(filters.ubicacion.trim(), ignoreCase = true)
            val matchOperador = filters.operador.isBlank() ||
                item.nombreNegocio.equals(filters.operador.trim(), ignoreCase = true) ||
                item.nombreNegocio.contains(filters.operador.trim(), ignoreCase = true)
            val matchEstrellas = filters.minEstrellas == null ||
                (item.totalValoraciones > 0 && item.promedioEstrellas >= filters.minEstrellas)
            matchUbicacion && matchOperador && matchEstrellas
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sliderAlojamientos: StateFlow<List<AlojamientoDetalle>> = publicAlojamientosBase
        .map { list -> list.filter { it.alojamiento.destacadoSlider }.take(10) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recomendadosAlojamientos: StateFlow<List<AlojamientoDetalle>> = publicAlojamientosBase
        .map { list -> list.take(4) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val operadoresActivos: StateFlow<List<String>> = publicAlojamientosBase
        .map { list -> list.map { it.nombreNegocio.trim() }.filter { it.isNotEmpty() }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val servicios: StateFlow<List<ServicioEntity>> = repository.observeServicios()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val afiliados: StateFlow<List<AfiliadoDetalle>> = repository.observeAfiliadosDetalle()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReservas: StateFlow<List<ReservaDetalle>> = repository.observeAllReservasDetalle()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allValoraciones: StateFlow<List<ValoracionDetalle>> = repository.observeAllValoracionesDetalle()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val clientes: StateFlow<List<ClienteDetalle>> = repository.observeClientesDetalle()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateFilters(ubicacion: String, operador: String, minEstrellas: Int?) {
        _filterState.value = HomeFilterState(ubicacion, operador, minEstrellas)
    }

    fun clearFilters() {
        _filterState.value = HomeFilterState()
    }

    fun emitMessage(msg: String) {
        _snackbarMessages.tryEmit(msg)
    }

    // --- Auth ---

    fun login(email: String, password: String, onResult: (AuthResult) -> Unit) {
        viewModelScope.launch {
            val result = repository.iniciarSesion(email, password)
            if (result is AuthResult.Success) {
                _currentUser.value = result.usuario
                emitMessage("Bienvenido, ${result.usuario.nombre}")
            }
            onResult(result)
        }
    }

    fun logout() {
        _currentUser.value = null
        emitMessage("Sesión cerrada.")
    }

    fun registerAffiliate(
        nombreNegocio: String,
        tipo: String,
        direccion: String,
        descripcion: String,
        email: String,
        password: String,
        onResult: (Result<String>) -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.registrarAfiliado(nombreNegocio, tipo, direccion, descripcion, email, password)
            onResult(res)
        }
    }

    fun registerClient(
        primerNombre: String,
        segundoNombre: String,
        primerApellido: String,
        cedula: String,
        telefonoCodigo: String,
        telefonoNumero: String,
        email: String,
        municipioOrigen: String,
        password: String,
        onResult: (Result<String>) -> Unit
    ) {
        viewModelScope.launch {
            val res = repository.registrarCliente(
                primerNombre, segundoNombre, primerApellido, cedula,
                telefonoCodigo, telefonoNumero, email, municipioOrigen, password
            )
            onResult(res)
        }
    }

    // --- Detail & Customer Review ---

    suspend fun loadDetailCustomerState(alojamientoId: Int): SitioDetailCustomerState {
        val user = _currentUser.value
        if (user == null || user.rol != "cliente") return SitioDetailCustomerState()
        val valCliente = repository.getValoracionClienteParaAlojamiento(user.id, alojamientoId)
        val resFinalizada = repository.getReservaFinalizadaCliente(alojamientoId, user.id)
        return SitioDetailCustomerState(
            valoracionCliente = valCliente,
            reservaFinalizada = resFinalizada,
            puedeValorar = resFinalizada != null && valCliente == null
        )
    }

    fun submitRating(
        reservaId: Int,
        estrellas: Int,
        comentario: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.calificarReserva(reservaId, user.id, estrellas, comentario)
            if (res.isSuccess) emitMessage("¡Gracias por compartir tu experiencia!")
            onResult(res)
        }
    }

    // --- Bookings ---

    fun createBooking(
        alojamientoId: Int,
        fechaInicio: String,
        fechaFin: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.crearReserva(alojamientoId, user.id, fechaInicio, fechaFin)
            if (res.isSuccess) emitMessage("Solicitud de reserva registrada.")
            onResult(res)
        }
    }

    fun updateReservaEstado(reservaId: Int, estado: String) {
        viewModelScope.launch {
            repository.cambiarEstadoReserva(reservaId, estado)
            emitMessage("Estado de reserva actualizado a ${estado.replaceFirstChar { it.uppercase() }}.")
        }
    }

    fun buildAgenda(reservas: List<ReservaDetalle>, mesStr: String, diaEspecifico: String = ""): AgendaResumenMensual {
        return repository.construirAgenda(reservas, mesStr, diaEspecifico)
    }

    // --- Owner Sitios CRUD ---

    suspend fun getAlojamientoForEdit(id: Int): Pair<AlojamientoEntity?, List<Int>> {
        val sitio = repository.getAlojamientoById(id)
        val serviceIds = if (sitio != null) repository.getServiceIdsForAlojamiento(id) else emptyList()
        return sitio to serviceIds
    }

    fun saveAlojamiento(
        sitioId: Int?,
        nombre: String,
        descripcion: String,
        ubicacion: String,
        precioNoche: Double,
        rangoPrecio: String,
        imagen: String,
        serviceIds: List<Int>,
        onResult: (Result<Unit>) -> Unit
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val result = if (sitioId == null) {
                repository.crearAlojamiento(
                    user.id, nombre, descripcion, ubicacion, precioNoche, rangoPrecio, imagen, serviceIds
                ).map { Unit }
            } else {
                repository.actualizarAlojamiento(
                    sitioId, nombre, descripcion, ubicacion, precioNoche, rangoPrecio, imagen, serviceIds
                )
            }
            if (result.isSuccess) {
                emitMessage(if (sitioId == null) "Alojamiento creado (pendiente de revisión)." else "Alojamiento actualizado.")
            }
            onResult(result)
        }
    }

    fun deleteAlojamiento(id: Int) {
        viewModelScope.launch {
            repository.eliminarAlojamiento(id)
            emitMessage("Alojamiento eliminado.")
        }
    }

    // --- Reviews Moderation (Admin & Owner) ---

    fun updateValoracionComentario(id: Int, comentario: String) {
        viewModelScope.launch {
            val res = repository.actualizarComentarioValoracion(id, comentario)
            if (res.isSuccess) emitMessage("Comentario actualizado.")
            else emitMessage(res.exceptionOrNull()?.message ?: "Error al actualizar comentario.")
        }
    }

    fun deleteValoracion(id: Int) {
        viewModelScope.launch {
            repository.eliminarValoracion(id)
            emitMessage("Comentario eliminado.")
        }
    }

    // --- Admin Modules ---

    fun updateAfiliacionEstado(id: Int, estado: String) {
        viewModelScope.launch {
            repository.cambiarEstadoAfiliacion(id, estado)
            emitMessage("Afiliación marcada como $estado.")
        }
    }

    fun updateAlojamientoEstado(id: Int, estado: String) {
        viewModelScope.launch {
            repository.cambiarEstadoAlojamiento(id, estado)
            emitMessage("Alojamiento marcado como $estado.")
        }
    }

    fun toggleAlojamientoSlider(id: Int, activar: Boolean) {
        viewModelScope.launch {
            repository.toggleSliderAlojamiento(id, activar)
            emitMessage(if (activar) "Añadido al carrusel destacado." else "Quitado del carrusel.")
        }
    }

    fun createServicio(nombre: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            val res = repository.crearServicio(nombre)
            if (res.isSuccess) emitMessage("Servicio agregado.")
            else onError(res.exceptionOrNull()?.message ?: "No se pudo crear el servicio.")
        }
    }

    fun updateServicio(id: Int, nombre: String, onError: (String) -> Unit) {
        viewModelScope.launch {
            val res = repository.actualizarServicio(id, nombre)
            if (res.isSuccess) emitMessage("Servicio actualizado.")
            else onError(res.exceptionOrNull()?.message ?: "No se pudo actualizar el servicio.")
        }
    }

    fun deleteServicio(id: Int) {
        viewModelScope.launch {
            repository.eliminarServicio(id)
            emitMessage("Servicio eliminado.")
        }
    }

    suspend fun getClienteForEdit(usuarioId: Int): ClienteDetalle? =
        repository.getClienteDetalle(usuarioId)

    fun saveClienteAdmin(
        usuarioId: Int?,
        primerNombre: String,
        segundoNombre: String,
        primerApellido: String,
        cedula: String,
        telefonoCodigo: String,
        telefonoNumero: String,
        email: String,
        municipioOrigen: String,
        password: String,
        estado: String,
        onResult: (Result<Unit>) -> Unit
    ) {
        viewModelScope.launch {
            val res = if (usuarioId == null) {
                repository.crearClienteAdmin(
                    primerNombre, segundoNombre, primerApellido, cedula,
                    telefonoCodigo, telefonoNumero, email, municipioOrigen, password, estado
                )
            } else {
                repository.actualizarClienteAdmin(
                    usuarioId, primerNombre, segundoNombre, primerApellido, cedula,
                    telefonoCodigo, telefonoNumero, email, municipioOrigen, password, estado
                )
            }
            if (res.isSuccess) {
                emitMessage(if (usuarioId == null) "Cliente creado." else "Cliente actualizado.")
            }
            onResult(res)
        }
    }

    fun deleteClienteAdmin(usuarioId: Int) {
        viewModelScope.launch {
            repository.eliminarClienteAdmin(usuarioId)
            emitMessage("Cliente eliminado.")
        }
    }

    fun savePickedSiteImage(context: Context, uri: Uri, onSaved: (String) -> Unit) {
        viewModelScope.launch {
            val path = repository.copyPickedImageToInternalStorage(context, uri, "sitio")
            onSaved(path)
        }
    }

    fun savePickedBackgroundImage(context: Context, uri: Uri) {
        viewModelScope.launch {
            val path = repository.copyPickedImageToInternalStorage(context, uri, "background")
            repository.saveBackgroundImage(path)
            emitMessage("Imagen de fondo actualizada.")
        }
    }

    fun resetBackgroundImage() {
        viewModelScope.launch {
            repository.resetBackgroundImage()
            emitMessage("Imagen de fondo restablecida.")
        }
    }

    class Factory(private val repository: TurismoRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TurismoViewModel(repository) as T
        }
    }
}

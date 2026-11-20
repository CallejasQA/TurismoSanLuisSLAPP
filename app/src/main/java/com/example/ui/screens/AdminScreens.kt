package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RoomService
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.AfiliadoDetalle
import com.example.data.model.AlojamientoDetalle
import com.example.data.model.ClienteDetalle
import com.example.data.model.ServicioEntity
import com.example.ui.components.AccommodationImage
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.StatusPill
import com.example.ui.components.TurismoConfirmDialog
import com.example.ui.components.formatCopPrice
import com.example.ui.theme.BordeSuave
import com.example.ui.theme.PillDangerBg
import com.example.ui.theme.PillDangerFg
import com.example.ui.theme.VerdeBosque
import com.example.ui.theme.VerdeClaro
import com.example.ui.theme.VerdeProfundo

@Composable
fun AdminHubScreen(
    pendingAffiliatesCount: Int,
    pendingSitiosCount: Int,
    servicesCount: Int,
    clientsCount: Int,
    reservasCount: Int,
    reviewsCount: Int,
    onOpenAffiliates: () -> Unit,
    onOpenSites: () -> Unit,
    onOpenServices: () -> Unit,
    onOpenClients: () -> Unit,
    onOpenReservations: () -> Unit,
    onOpenAgenda: () -> Unit,
    onOpenReviews: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val modules = listOf(
        AdminModuleItem("Afiliaciones", "$pendingAffiliatesCount pendientes", Icons.Default.Handshake, "admin_mod_affiliates", onOpenAffiliates),
        AdminModuleItem("Alojamientos", "$pendingSitiosCount por revisar", Icons.Default.Apartment, "admin_mod_sites", onOpenSites),
        AdminModuleItem("Servicios", "$servicesCount en catálogo", Icons.Default.RoomService, "admin_mod_services", onOpenServices),
        AdminModuleItem("Clientes", "$clientsCount registrados", Icons.Default.Group, "admin_mod_clients", onOpenClients),
        AdminModuleItem("Reservas", "$reservasCount registradas", Icons.Default.EventNote, "admin_mod_reservations", onOpenReservations),
        AdminModuleItem("Agenda", "Ocupación mensual", Icons.Default.CalendarMonth, "admin_mod_agenda", onOpenAgenda),
        AdminModuleItem("Comentarios", "$reviewsCount valoraciones", Icons.Default.ChatBubbleOutline, "admin_mod_reviews", onOpenReviews),
        AdminModuleItem("Configuración", "Imagen de fondo", Icons.Default.Image, "admin_mod_settings", onOpenSettings)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 960.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Panel de administración",
                    style = MaterialTheme.typography.headlineMedium,
                    color = VerdeProfundo,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Gestiona afiliaciones, alojamientos, servicios, clientes, reservas y configuración.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(modules.chunked(2)) { pair ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pair.forEach { mod ->
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = mod.onClick)
                            .testTag(mod.testTag),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, BordeSuave)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(color = VerdeClaro, shape = RoundedCornerShape(12.dp)) {
                                Icon(
                                    imageVector = mod.icon,
                                    contentDescription = mod.title,
                                    tint = VerdeProfundo,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                            Text(
                                text = mod.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = VerdeProfundo,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = mod.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(Modifier.height(12.dp)) }
    }
}

private data class AdminModuleItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val testTag: String,
    val onClick: () -> Unit
)

@Composable
fun AdminAffiliatesScreen(
    afiliados: List<AfiliadoDetalle>,
    onUpdateEstado: (id: Int, estado: String) -> Unit
) {
    val pendientes = remember(afiliados) {
        afiliados.filter { it.afiliado.estado == "pendiente" }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 900.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Afiliaciones pendientes",
                style = MaterialTheme.typography.headlineMedium,
                color = VerdeProfundo,
                fontWeight = FontWeight.Bold
            )
        }
        if (pendientes.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No hay solicitudes pendientes",
                    subtitle = "Todas las solicitudes de afiliación ya fueron atendidas."
                )
            }
        } else {
            items(pendientes, key = { "aff_${it.afiliado.id}" }) { item ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, BordeSuave)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = item.afiliado.nombreNegocio,
                                style = MaterialTheme.typography.titleMedium,
                                color = VerdeProfundo,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            StatusPill(status = item.afiliado.estado)
                        }
                        Text("${item.afiliado.tipo} · ${item.correoPropietario}", style = MaterialTheme.typography.bodySmall)
                        if (item.afiliado.direccion.isNotBlank()) {
                            Text("Dirección: ${item.afiliado.direccion}", style = MaterialTheme.typography.bodySmall)
                        }
                        if (item.afiliado.descripcion.isNotBlank()) {
                            Text(item.afiliado.descripcion, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onUpdateEstado(item.afiliado.id, "aprobado") },
                                shape = RoundedCornerShape(999.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VerdeBosque),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("aff_approve_${item.afiliado.id}")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Aprobar")
                            }
                            OutlinedButton(
                                onClick = { onUpdateEstado(item.afiliado.id, "rechazado") },
                                shape = RoundedCornerShape(999.dp),
                                border = BorderStroke(1.dp, PillDangerFg),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("aff_reject_${item.afiliado.id}")
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, tint = PillDangerFg, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Rechazar", color = PillDangerFg)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSitesScreen(
    alojamientos: List<AlojamientoDetalle>,
    onChangeEstado: (id: Int, estado: String) -> Unit,
    onToggleSlider: (id: Int, activar: Boolean) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 960.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Alojamientos registrados",
                style = MaterialTheme.typography.headlineMedium,
                color = VerdeProfundo,
                fontWeight = FontWeight.Bold
            )
        }
        if (alojamientos.isEmpty()) {
            item {
                EmptyStateCard(title = "No hay alojamientos cargados", subtitle = "Aún no existen propiedades registradas.")
            }
        } else {
            items(alojamientos, key = { "adm_site_${it.alojamiento.id}" }) { item ->
                val a = item.alojamiento
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, BordeSuave)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = a.nombre,
                                style = MaterialTheme.typography.titleMedium,
                                color = VerdeProfundo,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            StatusPill(status = a.estado)
                        }
                        Text(
                            text = "Propietario: ${item.correoPropietario} · ${formatCopPrice(a.precioNoche)}/noche",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Carrusel: ${if (a.destacadoSlider) "Sí" else "No"} · Creado: ${a.creadoEn.take(10)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (a.estado == "pendiente" || a.estado == "rechazado") {
                                Button(
                                    onClick = { onChangeEstado(a.id, "aprobado") },
                                    shape = RoundedCornerShape(999.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = VerdeBosque),
                                    modifier = Modifier.weight(1f)
                                ) { Text("Aprobar") }
                            }
                            if (a.estado == "aprobado" || a.estado == "pendiente" || a.estado == "inactivo") {
                                Button(
                                    onClick = { onChangeEstado(a.id, "activo") },
                                    shape = RoundedCornerShape(999.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = VerdeBosque),
                                    modifier = Modifier.weight(1f)
                                ) { Text("Activar") }
                            }
                            if (a.estado == "activo") {
                                OutlinedButton(
                                    onClick = { onChangeEstado(a.id, "inactivo") },
                                    shape = RoundedCornerShape(999.dp),
                                    border = BorderStroke(1.dp, PillDangerFg),
                                    modifier = Modifier.weight(1f)
                                ) { Text("Desactivar", color = PillDangerFg) }
                            }
                            if (a.estado != "rechazado") {
                                OutlinedButton(
                                    onClick = { onChangeEstado(a.id, "rechazado") },
                                    shape = RoundedCornerShape(999.dp),
                                    border = BorderStroke(1.dp, PillDangerFg),
                                    modifier = Modifier.weight(1f)
                                ) { Text("Rechazar", color = PillDangerFg) }
                            }
                        }
                        if (a.estado in listOf("aprobado", "activo")) {
                            OutlinedButton(
                                onClick = { onToggleSlider(a.id, !a.destacadoSlider) },
                                shape = RoundedCornerShape(999.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (a.destacadoSlider) "Quitar del carrusel" else "Añadir al carrusel")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminServicesScreen(
    servicios: List<ServicioEntity>,
    onCreate: (String, (String) -> Unit) -> Unit,
    onUpdate: (Int, String, (String) -> Unit) -> Unit,
    onDelete: (Int) -> Unit
) {
    var newName by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var pendingDeleteId by remember { mutableStateOf<Int?>(null) }

    pendingDeleteId?.let { idToDelete ->
        TurismoConfirmDialog(
            title = "Eliminar servicio",
            message = "¿Eliminar este servicio?",
            onConfirm = {
                onDelete(idToDelete)
                pendingDeleteId = null
            },
            onDismiss = { pendingDeleteId = null }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 860.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Servicios de alojamientos",
                    style = MaterialTheme.typography.headlineMedium,
                    color = VerdeProfundo,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Administra el catálogo de servicios disponibles para los alojamientos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, BordeSuave)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Agregar nuevo servicio",
                        style = MaterialTheme.typography.titleMedium,
                        color = VerdeProfundo,
                        fontWeight = FontWeight.Bold
                    )
                    errorMsg?.let { err ->
                        Surface(color = PillDangerBg, shape = RoundedCornerShape(10.dp)) {
                            Text(err, color = PillDangerFg, modifier = Modifier.padding(10.dp))
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it.take(60) },
                            label = { Text("Nombre del servicio") },
                            placeholder = { Text("Ej. WiFi premium") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_new_service_input")
                        )
                        Button(
                            onClick = {
                                errorMsg = null
                                onCreate(newName) { err -> errorMsg = err }
                                newName = ""
                            },
                            modifier = Modifier
                                .height(52.dp)
                                .testTag("admin_add_service_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VerdeBosque)
                        ) {
                            Text("Agregar")
                        }
                    }
                }
            }
        }

        items(servicios, key = { "adm_svc_${it.id}" }) { servicio ->
            var editName by remember(servicio.id, servicio.nombre) { mutableStateOf(servicio.nombre) }
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, BordeSuave)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it.take(60) },
                        label = { Text("Servicio #${servicio.id}") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                errorMsg = null
                                onUpdate(servicio.id, editName) { err -> errorMsg = err }
                            },
                            shape = RoundedCornerShape(999.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VerdeBosque),
                            modifier = Modifier.weight(1f)
                        ) { Text("Guardar") }
                        OutlinedButton(
                            onClick = { pendingDeleteId = servicio.id },
                            shape = RoundedCornerShape(999.dp),
                            border = BorderStroke(1.dp, PillDangerFg)
                        ) { Text("Eliminar", color = PillDangerFg) }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
fun AdminClientsScreen(
    clientes: List<ClienteDetalle>,
    onCreateClient: () -> Unit,
    onEditClient: (Int) -> Unit,
    onDeleteClient: (Int) -> Unit
) {
    var pendingDeleteId by remember { mutableStateOf<Int?>(null) }

    pendingDeleteId?.let { idToDelete ->
        TurismoConfirmDialog(
            title = "Eliminar cliente",
            message = "¿Eliminar cliente?",
            onConfirm = {
                onDeleteClient(idToDelete)
                pendingDeleteId = null
            },
            onDismiss = { pendingDeleteId = null }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateClient,
                containerColor = VerdeBosque,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("admin_add_client_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo cliente")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .widthIn(max = 960.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Clientes",
                        style = MaterialTheme.typography.headlineMedium,
                        color = VerdeProfundo,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Gestiona los huéspedes que pueden reservar en la plataforma.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (clientes.isEmpty()) {
                item {
                    EmptyStateCard(title = "No hay clientes registrados aún", subtitle = "Usa el botón + para agregar un cliente.")
                }
            } else {
                items(clientes, key = { "adm_cli_${it.usuario.id}" }) { c ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, BordeSuave)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${c.perfil.primerNombre} ${c.perfil.segundoNombre} ${c.perfil.primerApellido}".replace(Regex("\\s+"), " ").trim(),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = VerdeProfundo,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                StatusPill(status = c.usuario.estado)
                            }
                            Text("Correo: ${c.usuario.email}", style = MaterialTheme.typography.bodySmall)
                            Text("Celular: ${c.perfil.telefonoCodigo} ${c.perfil.telefonoNumero}", style = MaterialTheme.typography.bodySmall)
                            if (c.perfil.cedula.isNotBlank()) {
                                Text("Cédula: ${c.perfil.cedula}", style = MaterialTheme.typography.bodySmall)
                            }
                            if (c.perfil.municipioOrigen.isNotBlank()) {
                                Text("Municipio: ${c.perfil.municipioOrigen}", style = MaterialTheme.typography.bodySmall)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onEditClient(c.usuario.id) },
                                    shape = RoundedCornerShape(999.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Editar")
                                }
                                OutlinedButton(
                                    onClick = { pendingDeleteId = c.usuario.id },
                                    shape = RoundedCornerShape(999.dp),
                                    border = BorderStroke(1.dp, PillDangerFg),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = PillDangerFg, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Eliminar", color = PillDangerFg)
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(72.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminClientFormScreen(
    clienteId: Int?,
    loadInitial: suspend (Int) -> ClienteDetalle?,
    onSave: (
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
    ) -> Unit,
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    var primerNombre by remember { mutableStateOf("") }
    var segundoNombre by remember { mutableStateOf("") }
    var primerApellido by remember { mutableStateOf("") }
    var cedula by remember { mutableStateOf("") }
    var telefonoCodigo by remember { mutableStateOf("+57") }
    var telefonoNumero by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var municipioOrigen by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("activo") }
    var estadoOpen by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var loaded by remember(clienteId) { mutableStateOf(clienteId == null) }

    LaunchedEffect(clienteId) {
        if (clienteId != null && !loaded) {
            loadInitial(clienteId)?.let { c ->
                primerNombre = c.perfil.primerNombre
                segundoNombre = c.perfil.segundoNombre
                primerApellido = c.perfil.primerApellido
                cedula = c.perfil.cedula
                telefonoCodigo = c.perfil.telefonoCodigo
                telefonoNumero = c.perfil.telefonoNumero
                email = c.usuario.email
                municipioOrigen = c.perfil.municipioOrigen
                estado = c.usuario.estado
            }
            loaded = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .widthIn(max = 720.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (clienteId == null) "Nuevo cliente" else "Editar cliente",
            style = MaterialTheme.typography.headlineMedium,
            color = VerdeProfundo,
            fontWeight = FontWeight.Bold
        )
        errorMsg?.let { err ->
            Surface(color = PillDangerBg, shape = RoundedCornerShape(12.dp)) {
                Text(err, color = PillDangerFg, modifier = Modifier.padding(12.dp))
            }
        }
        OutlinedTextField(value = primerNombre, onValueChange = { primerNombre = it.take(20) }, label = { Text("Primer nombre *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = segundoNombre, onValueChange = { segundoNombre = it.take(20) }, label = { Text("Segundo nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = primerApellido, onValueChange = { primerApellido = it.take(20) }, label = { Text("Primer apellido *") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = cedula, onValueChange = { cedula = it.filter(Char::isDigit).take(20) }, label = { Text("Cédula") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = telefonoCodigo, onValueChange = { telefonoCodigo = it.take(6) }, label = { Text("Cód.") }, singleLine = true, modifier = Modifier.width(100.dp))
            OutlinedTextField(value = telefonoNumero, onValueChange = { telefonoNumero = it.filter(Char::isDigit).take(20) }, label = { Text("Celular *") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.weight(1f))
        }
        OutlinedTextField(value = email, onValueChange = { email = it.trim() }, label = { Text("Correo electrónico *") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = municipioOrigen, onValueChange = { municipioOrigen = it.take(100) }, label = { Text("Municipio de origen") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(if (clienteId == null) "Contraseña (opcional, def. 123456)" else "Nueva contraseña (opcional)") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        ExposedDropdownMenuBox(expanded = estadoOpen, onExpandedChange = { estadoOpen = !estadoOpen }) {
            OutlinedTextField(
                value = estado.replaceFirstChar { it.uppercase() },
                onValueChange = {},
                readOnly = true,
                label = { Text("Estado") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = estadoOpen) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(expanded = estadoOpen, onDismissRequest = { estadoOpen = false }) {
                listOf("activo", "inactivo").forEach { est ->
                    DropdownMenuItem(text = { Text(est.replaceFirstChar { it.uppercase() }) }, onClick = { estado = est; estadoOpen = false })
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = {
                    errorMsg = null
                    onSave(clienteId, primerNombre, segundoNombre, primerApellido, cedula, telefonoCodigo, telefonoNumero, email, municipioOrigen, password, estado) { res ->
                        if (res.isSuccess) onSaved() else errorMsg = res.exceptionOrNull()?.message
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerdeBosque)
            ) { Text("Guardar", fontWeight = FontWeight.Bold) }
            OutlinedButton(onClick = onCancel, modifier = Modifier.height(48.dp), shape = RoundedCornerShape(999.dp)) {
                Text("Cancelar")
            }
        }
    }
}

@Composable
fun AdminSettingsScreen(
    backgroundImageUrl: String,
    onPickBackground: (android.content.Context, Uri) -> Unit,
    onResetBackground: () -> Unit
) {
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { onPickBackground(context, it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .widthIn(max = 760.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Imagen de fondo",
            style = MaterialTheme.typography.headlineMedium,
            color = VerdeProfundo,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Administra la imagen que se muestra en las pantallas de autenticación.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, BordeSuave)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Imagen actual:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(backgroundImageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Imagen de fondo actual",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("admin_pick_bg_button"),
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VerdeBosque)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Actualizar imagen")
                    }
                    OutlinedButton(
                        onClick = onResetBackground,
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text("Restablecer")
                    }
                }
            }
        }
    }
}

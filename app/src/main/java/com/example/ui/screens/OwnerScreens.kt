package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.AlojamientoDetalle
import com.example.data.model.AlojamientoEntity
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
fun OwnerSitesScreen(
    sitios: List<AlojamientoDetalle>,
    onCreateSite: () -> Unit,
    onEditSite: (Int) -> Unit,
    onDeleteSite: (Int) -> Unit,
    onOpenReservations: () -> Unit,
    onOpenAgenda: () -> Unit,
    onOpenReviews: () -> Unit
) {
    var pendingDeleteId by remember { mutableStateOf<Int?>(null) }

    pendingDeleteId?.let { idToDelete ->
        TurismoConfirmDialog(
            title = "Eliminar alojamiento",
            message = "¿Deseas eliminar este alojamiento? Esta acción eliminará también sus reservas asociadas.",
            onConfirm = {
                onDeleteSite(idToDelete)
                pendingDeleteId = null
            },
            onDismiss = { pendingDeleteId = null }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateSite,
                containerColor = VerdeBosque,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("owner_add_site_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo alojamiento")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .widthIn(max = 960.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Mis Alojamientos",
                        style = MaterialTheme.typography.headlineMedium,
                        color = VerdeProfundo,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Publica propiedades, gestiona agenda, reservas y comentarios de huéspedes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenAgenda,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("owner_agenda_nav"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Agenda", maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = onOpenReservations,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("owner_reservations_nav"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.EventNote, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Reservas", maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = onOpenReviews,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("owner_reviews_nav"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Reseñas", maxLines = 1)
                    }
                }
            }

            if (sitios.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No hay alojamientos",
                        subtitle = "Toca el botón + para registrar tu primer alojamiento en San Luis."
                    )
                }
            } else {
                items(sitios, key = { "own_sitio_${it.alojamiento.id}" }) { item ->
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, BordeSuave)
                    ) {
                        Column {
                            AccommodationImage(
                                imagePath = item.alojamiento.imagen,
                                contentDescription = item.alojamiento.nombre,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(170.dp)
                            )
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
                                        text = item.alojamiento.nombre,
                                        style = MaterialTheme.typography.titleLarge,
                                        color = VerdeProfundo,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    StatusPill(status = item.alojamiento.estado)
                                }
                                Text(
                                    text = item.alojamiento.ubicacion.ifBlank { "Ubicación por definir" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${formatCopPrice(item.alojamiento.precioNoche)} / noche",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = VerdeBosque,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { onEditSite(item.alojamiento.id) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("owner_edit_site_${item.alojamiento.id}"),
                                        shape = RoundedCornerShape(999.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Editar")
                                    }
                                    OutlinedButton(
                                        onClick = { pendingDeleteId = item.alojamiento.id },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("owner_delete_site_${item.alojamiento.id}"),
                                        shape = RoundedCornerShape(999.dp),
                                        border = BorderStroke(1.dp, PillDangerFg)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = null, tint = PillDangerFg, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Eliminar", color = PillDangerFg)
                                    }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SiteFormScreen(
    sitioId: Int?,
    allServicios: List<ServicioEntity>,
    loadInitial: suspend (Int) -> Pair<AlojamientoEntity?, List<Int>>,
    onPickImage: (android.content.Context, Uri, (String) -> Unit) -> Unit,
    onSave: (
        sitioId: Int?,
        nombre: String,
        descripcion: String,
        ubicacion: String,
        precioNoche: Double,
        rangoPrecio: String,
        imagen: String,
        serviceIds: List<Int>,
        onResult: (Result<Unit>) -> Unit
    ) -> Unit,
    onSaved: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var ubicacion by remember { mutableStateOf("") }
    var precioNoche by remember { mutableStateOf("") }
    var rangoPrecio by remember { mutableStateOf("") }
    var imagenPath by remember { mutableStateOf("") }
    var selectedServiceIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var loaded by remember(sitioId) { mutableStateOf(sitioId == null) }

    LaunchedEffect(sitioId) {
        if (sitioId != null && !loaded) {
            val (sitio, svcIds) = loadInitial(sitioId)
            if (sitio != null) {
                nombre = sitio.nombre
                descripcion = sitio.descripcion
                ubicacion = sitio.ubicacion
                precioNoche = sitio.precioNoche.toBigDecimal().stripTrailingZeros().toPlainString()
                rangoPrecio = sitio.rangoPrecio
                imagenPath = sitio.imagen
                selectedServiceIds = svcIds.toSet()
            }
            loaded = true
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            onPickImage(context, it) { savedPath ->
                imagenPath = savedPath
            }
        }
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
            text = if (sitioId == null) "Nuevo alojamiento" else "Editar alojamiento",
            style = MaterialTheme.typography.headlineMedium,
            color = VerdeProfundo,
            fontWeight = FontWeight.Bold
        )

        errorMsg?.let { err ->
            Surface(color = PillDangerBg, shape = RoundedCornerShape(12.dp)) {
                Text(
                    text = err,
                    color = PillDangerFg,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre *") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("site_form_name")
        )
        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción") },
            minLines = 3,
            maxLines = 5,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("site_form_description")
        )
        OutlinedTextField(
            value = ubicacion,
            onValueChange = { ubicacion = it },
            label = { Text("Ubicación") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("site_form_location")
        )
        OutlinedTextField(
            value = precioNoche,
            onValueChange = { precioNoche = it.filter { ch -> ch.isDigit() || ch == '.' } },
            label = { Text("Precio por noche *") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("site_form_price")
        )
        OutlinedTextField(
            value = rangoPrecio,
            onValueChange = { rangoPrecio = it },
            label = { Text("Rango de precio") },
            placeholder = { Text("Ej. \$180.000 - \$250.000") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

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
                    text = "Imagen del alojamiento",
                    style = MaterialTheme.typography.titleMedium,
                    color = VerdeProfundo,
                    fontWeight = FontWeight.Bold
                )
                if (imagenPath.isNotBlank()) {
                    AccommodationImage(
                        imagePath = imagenPath,
                        contentDescription = "Vista previa",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                }
                OutlinedButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier.testTag("site_pick_image_button")
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (imagenPath.isBlank()) "Seleccionar imagen" else "Cambiar imagen")
                }
            }
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, BordeSuave)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Servicios disponibles",
                    style = MaterialTheme.typography.titleMedium,
                    color = VerdeProfundo,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Selecciona los servicios que ofreces para ayudar a los viajeros a elegir tu alojamiento.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    allServicios.forEach { servicio ->
                        val selected = servicio.id in selectedServiceIds
                        FilterChip(
                            selected = selected,
                            onClick = {
                                selectedServiceIds = if (selected) {
                                    selectedServiceIds - servicio.id
                                } else {
                                    selectedServiceIds + servicio.id
                                }
                            },
                            label = { Text(servicio.nombre) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VerdeClaro,
                                selectedLabelColor = VerdeProfundo
                            )
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    errorMsg = null
                    val precio = precioNoche.toDoubleOrNull() ?: 0.0
                    onSave(
                        sitioId,
                        nombre,
                        descripcion,
                        ubicacion,
                        precio,
                        rangoPrecio,
                        imagenPath,
                        selectedServiceIds.toList()
                    ) { res ->
                        if (res.isSuccess) onSaved()
                        else errorMsg = res.exceptionOrNull()?.message
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("site_form_save_button"),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(containerColor = VerdeBosque)
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.height(48.dp),
                shape = RoundedCornerShape(999.dp)
            ) {
                Text("Cancelar")
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

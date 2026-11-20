package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AgendaResumenMensual
import com.example.data.model.ReservaDetalle
import com.example.data.model.ValoracionDetalle
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.StatusPill
import com.example.ui.components.TurismoConfirmDialog
import com.example.ui.components.formatCopPrice
import com.example.ui.theme.BordeSuave
import com.example.ui.theme.PillDangerFg
import com.example.ui.theme.StarAmber
import com.example.ui.theme.VerdeBosque
import com.example.ui.theme.VerdeClaro
import com.example.ui.theme.VerdeProfundo
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationsManageScreen(
    isAdmin: Boolean,
    reservas: List<ReservaDetalle>,
    onUpdateEstado: (reservaId: Int, estado: String) -> Unit
) {
    var mesFilter by remember { mutableStateOf(YearMonth.now().toString()) }
    var selectedOwnerEmail by remember { mutableStateOf("") }
    var ownerDropdownOpen by remember { mutableStateOf(false) }

    val propietarios = remember(reservas) {
        reservas.map { it.propietarioEmail }.filter { it.isNotBlank() }.distinct().sorted()
    }

    val filtered = remember(reservas, mesFilter, selectedOwnerEmail) {
        reservas.filter { r ->
            val matchMes = mesFilter.isBlank() ||
                r.reserva.fechaInicio.startsWith(mesFilter.trim()) ||
                r.reserva.fechaFin.startsWith(mesFilter.trim())
            val matchOwner = selectedOwnerEmail.isBlank() || r.propietarioEmail == selectedOwnerEmail
            matchMes && matchOwner
        }
    }

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
                    text = if (isAdmin) "Reservas" else "Reservas de mis alojamientos",
                    style = MaterialTheme.typography.headlineMedium,
                    color = VerdeProfundo,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isAdmin) {
                        "Gestiona todas las reservas, filtra por mes y propietario."
                    } else {
                        "Filtra por mes y responde rápidamente a tus huéspedes."
                    },
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
                    OutlinedTextField(
                        value = mesFilter,
                        onValueChange = { mesFilter = it.take(7) },
                        label = { Text("Mes (AAAA-MM)") },
                        trailingIcon = {
                            if (mesFilter.isNotEmpty()) {
                                IconButton(onClick = { mesFilter = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Limpiar mes")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (isAdmin) {
                        ExposedDropdownMenuBox(
                            expanded = ownerDropdownOpen,
                            onExpandedChange = { ownerDropdownOpen = !ownerDropdownOpen }
                        ) {
                            OutlinedTextField(
                                value = selectedOwnerEmail.ifBlank { "Todos los propietarios" },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Propietario") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ownerDropdownOpen) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )
                            ExposedDropdownMenu(
                                expanded = ownerDropdownOpen,
                                onDismissRequest = { ownerDropdownOpen = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Todos los propietarios") },
                                    onClick = {
                                        selectedOwnerEmail = ""
                                        ownerDropdownOpen = false
                                    }
                                )
                                propietarios.forEach { email ->
                                    DropdownMenuItem(
                                        text = { Text(email) },
                                        onClick = {
                                            selectedOwnerEmail = email
                                            ownerDropdownOpen = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            mesFilter = ""
                            selectedOwnerEmail = ""
                        },
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text("Limpiar filtros")
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "Sin reservas",
                    subtitle = "Ajusta los filtros para ver resultados."
                )
            }
        } else {
            items(filtered, key = { "mgr_res_${it.reserva.id}" }) { r ->
                var estadoMenuOpen by remember { mutableStateOf(false) }
                val estados = listOf("pendiente", "confirmada", "finalizada", "cancelada")
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
                                text = r.alojamientoNombre,
                                style = MaterialTheme.typography.titleMedium,
                                color = VerdeProfundo,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            StatusPill(status = r.reserva.estado)
                        }
                        if (isAdmin && r.propietarioEmail.isNotBlank()) {
                            Text(
                                text = "Propietario: ${r.propietarioEmail}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "Huésped: ${r.clienteNombreCompleto} (${r.clienteEmail})",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${r.reserva.fechaInicio} → ${r.reserva.fechaFin}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatCopPrice(r.reserva.total),
                                style = MaterialTheme.typography.titleSmall,
                                color = VerdeBosque,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        ExposedDropdownMenuBox(
                            expanded = estadoMenuOpen,
                            onExpandedChange = { estadoMenuOpen = !estadoMenuOpen }
                        ) {
                            OutlinedTextField(
                                value = r.reserva.estado.replaceFirstChar { it.uppercase() },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Cambiar estado") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = estadoMenuOpen) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                    .testTag("reserva_estado_dropdown_${r.reserva.id}")
                            )
                            ExposedDropdownMenu(
                                expanded = estadoMenuOpen,
                                onDismissRequest = { estadoMenuOpen = false }
                            ) {
                                estados.forEach { est ->
                                    DropdownMenuItem(
                                        text = { Text(est.replaceFirstChar { it.uppercase() }) },
                                        onClick = {
                                            onUpdateEstado(r.reserva.id, est)
                                            estadoMenuOpen = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(
    isAdmin: Boolean,
    reservas: List<ReservaDetalle>,
    buildAgenda: (List<ReservaDetalle>, String, String) -> AgendaResumenMensual
) {
    var mes by remember { mutableStateOf(YearMonth.now().toString()) }
    var diaEspecifico by remember { mutableStateOf("") }
    var selectedFilterKey by remember { mutableStateOf("") } // ownerEmail for admin, alojamientoNombre for owner
    var filterMenuOpen by remember { mutableStateOf(false) }
    var soloConReservas by remember { mutableStateOf(false) }

    val filterOptions = remember(reservas, isAdmin) {
        if (isAdmin) {
            reservas.map { it.propietarioEmail }.filter { it.isNotBlank() }.distinct().sorted()
        } else {
            reservas.map { it.alojamientoNombre }.filter { it.isNotBlank() }.distinct().sorted()
        }
    }

    val filteredReservas = remember(reservas, selectedFilterKey, isAdmin) {
        if (selectedFilterKey.isBlank()) reservas
        else if (isAdmin) reservas.filter { it.propietarioEmail == selectedFilterKey }
        else reservas.filter { it.alojamientoNombre == selectedFilterKey }
    }

    val effectiveMes = if (diaEspecifico.length >= 7) diaEspecifico.take(7) else mes
    val resumen = remember(filteredReservas, effectiveMes, diaEspecifico) {
        buildAgenda(filteredReservas, effectiveMes, diaEspecifico)
    }

    val visibleDias = remember(resumen, soloConReservas) {
        if (soloConReservas) resumen.dias.filter { it.reservas.isNotEmpty() }
        else resumen.dias
    }

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
                    text = if (isAdmin) "Agenda de ocupación" else "Mi agenda de ocupación",
                    style = MaterialTheme.typography.headlineMedium,
                    color = VerdeProfundo,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isAdmin) {
                        "Visualiza tus reservas por mes y propietario."
                    } else {
                        "Visualiza tus reservas por mes, alojamiento o día específico."
                    },
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
                    ExposedDropdownMenuBox(
                        expanded = filterMenuOpen,
                        onExpandedChange = { filterMenuOpen = !filterMenuOpen }
                    ) {
                        OutlinedTextField(
                            value = selectedFilterKey.ifBlank {
                                if (isAdmin) "Todos los propietarios" else "Todos mis alojamientos"
                            },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (isAdmin) "Propietario" else "Alojamiento") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = filterMenuOpen) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = filterMenuOpen,
                            onDismissRequest = { filterMenuOpen = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (isAdmin) "Todos los propietarios" else "Todos mis alojamientos") },
                                onClick = {
                                    selectedFilterKey = ""
                                    filterMenuOpen = false
                                }
                            )
                            filterOptions.forEach { opt ->
                                DropdownMenuItem(
                                    text = { Text(opt) },
                                    onClick = {
                                        selectedFilterKey = opt
                                        filterMenuOpen = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = mes,
                        onValueChange = { mes = it.take(7) },
                        label = { Text("Mes (AAAA-MM)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (!isAdmin) {
                        OutlinedTextField(
                            value = diaEspecifico,
                            onValueChange = { diaEspecifico = it.take(10) },
                            label = { Text("Día específico (AAAA-MM-DD)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mostrar sólo días con reservas",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Switch(
                            checked = soloConReservas,
                            onCheckedChange = { soloConReservas = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = VerdeBosque),
                            modifier = Modifier.testTag("agenda_only_reserved_switch")
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            mes = YearMonth.now().toString()
                            diaEspecifico = ""
                            selectedFilterKey = ""
                            soloConReservas = false
                        },
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text("Limpiar filtros")
                    }
                }
            }
        }

        // 4 Summary KPI cards matching agenda.php
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AgendaKpiCard(
                        label = "Días con reservas",
                        value = "${resumen.diasConReservas}",
                        modifier = Modifier.weight(1f)
                    )
                    AgendaKpiCard(
                        label = "Días sin reservas",
                        value = "${resumen.diasSinReservas}",
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AgendaKpiCard(
                        label = "Ocupación del mes",
                        value = "${resumen.ocupacionPorcentaje}%",
                        modifier = Modifier.weight(1f)
                    )
                    AgendaKpiCard(
                        label = "Reservas totales",
                        value = "${resumen.totalReservasEnDias}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (visibleDias.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "Sin datos",
                    subtitle = "Ajusta los filtros para ver la agenda."
                )
            }
        } else {
            items(visibleDias, key = { "day_${it.fecha}" }) { day ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (day.reservas.isEmpty()) {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        }
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (day.reservas.isEmpty()) BordeSuave else VerdeBosque.copy(alpha = 0.35f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${day.numeroDia} · ${day.nombreDia} (${day.fecha})",
                                style = MaterialTheme.typography.titleMedium,
                                color = VerdeProfundo,
                                fontWeight = FontWeight.Bold
                            )
                            if (day.reservas.isNotEmpty()) {
                                Surface(color = VerdeClaro, shape = RoundedCornerShape(999.dp)) {
                                    Text(
                                        text = "${day.reservas.size} reserva${if (day.reservas.size > 1) "s" else ""}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = VerdeProfundo,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        if (day.reservas.isEmpty()) {
                            Text(
                                text = "Sin reservas",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            day.reservas.forEach { res ->
                                HorizontalDivider(color = BordeSuave)
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = res.alojamientoNombre,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        StatusPill(status = res.reserva.estado)
                                    }
                                    if (isAdmin && res.propietarioEmail.isNotBlank()) {
                                        Text(
                                            text = res.propietarioEmail,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "Huésped: ${res.clienteNombreCompleto}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "Del ${res.reserva.fechaInicio} al ${res.reserva.fechaFin}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = VerdeBosque,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

@Composable
private fun AgendaKpiCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, BordeSuave)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = VerdeProfundo,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewsManageScreen(
    isAdmin: Boolean,
    valoraciones: List<ValoracionDetalle>,
    onUpdateComment: (id: Int, comment: String) -> Unit,
    onDeleteReview: (id: Int) -> Unit
) {
    var selectedOwner by remember { mutableStateOf("") }
    var selectedAloj by remember { mutableStateOf("") }
    var fechaDesde by remember { mutableStateOf("") }
    var fechaHasta by remember { mutableStateOf("") }
    var ownerMenuOpen by remember { mutableStateOf(false) }
    var alojMenuOpen by remember { mutableStateOf(false) }
    var pendingDeleteId by remember { mutableStateOf<Int?>(null) }

    pendingDeleteId?.let { idToDelete ->
        TurismoConfirmDialog(
            title = "Eliminar comentario",
            message = "¿Eliminar este comentario?",
            onConfirm = {
                onDeleteReview(idToDelete)
                pendingDeleteId = null
            },
            onDismiss = { pendingDeleteId = null }
        )
    }

    val owners = remember(valoraciones) {
        valoraciones.map { it.propietarioEmail }.filter { it.isNotBlank() }.distinct().sorted()
    }
    val alojamientos = remember(valoraciones, selectedOwner) {
        valoraciones
            .filter { selectedOwner.isBlank() || it.propietarioEmail == selectedOwner }
            .map { it.alojamientoNombre }
            .distinct()
            .sorted()
    }

    val filtered = remember(valoraciones, selectedOwner, selectedAloj, fechaDesde, fechaHasta) {
        valoraciones.filter { v ->
            val dateStr = v.valoracion.creadoEn.take(10)
            val matchOwner = selectedOwner.isBlank() || v.propietarioEmail == selectedOwner
            val matchAloj = selectedAloj.isBlank() || v.alojamientoNombre == selectedAloj
            val matchDesde = fechaDesde.isBlank() || dateStr >= fechaDesde.trim()
            val matchHasta = fechaHasta.isBlank() || dateStr <= fechaHasta.trim()
            matchOwner && matchAloj && matchDesde && matchHasta
        }
    }

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
                    text = if (isAdmin) "Gestión de comentarios" else "Comentarios de mis alojamientos",
                    style = MaterialTheme.typography.headlineMedium,
                    color = VerdeProfundo,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Actualiza o elimina comentarios realizados por los clientes y filtra por fechas o alojamiento.",
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
                    if (isAdmin) {
                        ExposedDropdownMenuBox(
                            expanded = ownerMenuOpen,
                            onExpandedChange = { ownerMenuOpen = !ownerMenuOpen }
                        ) {
                            OutlinedTextField(
                                value = selectedOwner.ifBlank { "Todos" },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Propietario") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = ownerMenuOpen) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            )
                            ExposedDropdownMenu(
                                expanded = ownerMenuOpen,
                                onDismissRequest = { ownerMenuOpen = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Todos") },
                                    onClick = {
                                        selectedOwner = ""
                                        selectedAloj = ""
                                        ownerMenuOpen = false
                                    }
                                )
                                owners.forEach { ow ->
                                    DropdownMenuItem(
                                        text = { Text(ow) },
                                        onClick = {
                                            selectedOwner = ow
                                            selectedAloj = ""
                                            ownerMenuOpen = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = alojMenuOpen,
                        onExpandedChange = { alojMenuOpen = !alojMenuOpen }
                    ) {
                        OutlinedTextField(
                            value = selectedAloj.ifBlank { "Todos" },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Alojamiento") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = alojMenuOpen) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = alojMenuOpen,
                            onDismissRequest = { alojMenuOpen = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Todos") },
                                onClick = {
                                    selectedAloj = ""
                                    alojMenuOpen = false
                                }
                            )
                            alojamientos.forEach { al ->
                                DropdownMenuItem(
                                    text = { Text(al) },
                                    onClick = {
                                        selectedAloj = al
                                        alojMenuOpen = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = fechaDesde,
                            onValueChange = { fechaDesde = it.take(10) },
                            label = { Text("Desde (AAAA-MM-DD)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = fechaHasta,
                            onValueChange = { fechaHasta = it.take(10) },
                            label = { Text("Hasta (AAAA-MM-DD)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            selectedOwner = ""
                            selectedAloj = ""
                            fechaDesde = ""
                            fechaHasta = ""
                        },
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text("Limpiar filtros")
                    }
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "No se encontraron comentarios",
                    subtitle = "No hay comentarios con los filtros seleccionados."
                )
            }
        } else {
            items(filtered, key = { "mod_rev_${it.valoracion.id}" }) { item ->
                var commentDraft by remember(item.valoracion.id, item.valoracion.comentario) {
                    mutableStateOf(item.valoracion.comentario)
                }
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
                                text = item.alojamientoNombre,
                                style = MaterialTheme.typography.titleMedium,
                                color = VerdeProfundo,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = StarAmber, modifier = Modifier.size(18.dp))
                                Text(" ${item.valoracion.estrellas}", fontWeight = FontWeight.Bold)
                            }
                        }
                        if (isAdmin) {
                            Text(
                                text = "Propietario: ${item.propietarioEmail}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "Cliente: ${item.clienteNombreCompleto} (${item.clienteEmail}) · ${item.valoracion.creadoEn.take(10)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        OutlinedTextField(
                            value = commentDraft,
                            onValueChange = { commentDraft = it.take(1000) },
                            label = { Text("Comentario") },
                            minLines = 2,
                            maxLines = 4,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("review_edit_input_${item.valoracion.id}")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onUpdateComment(item.valoracion.id, commentDraft) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("review_save_button_${item.valoracion.id}"),
                                shape = RoundedCornerShape(999.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VerdeBosque)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Actualizar")
                            }
                            OutlinedButton(
                                onClick = { pendingDeleteId = item.valoracion.id },
                                modifier = Modifier.testTag("review_delete_button_${item.valoracion.id}"),
                                shape = RoundedCornerShape(999.dp),
                                border = BorderStroke(1.dp, PillDangerFg)
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
        item { Spacer(Modifier.height(12.dp)) }
    }
}

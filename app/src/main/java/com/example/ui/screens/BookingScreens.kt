package com.example.ui.screens

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AlojamientoDetalle
import com.example.data.model.ReservaDetalle
import com.example.ui.components.AccommodationImage
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.StatusPill
import com.example.ui.components.formatCopPrice
import com.example.ui.theme.BordeSuave
import com.example.ui.theme.PillDangerBg
import com.example.ui.theme.PillDangerFg
import com.example.ui.theme.VerdeBosque
import com.example.ui.theme.VerdeClaro
import com.example.ui.theme.VerdeProfundo
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFormScreen(
    sitio: AlojamientoDetalle?,
    onCreateBooking: (alojamientoId: Int, fechaInicio: String, fechaFin: String, onResult: (Result<Unit>) -> Unit) -> Unit,
    onBookingCreated: () -> Unit,
    onCancel: () -> Unit
) {
    if (sitio == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            EmptyStateCard(title = "Alojamiento no encontrado", subtitle = "Regresa al catálogo e intenta nuevamente.")
        }
        return
    }

    var fechaInicio by remember { mutableStateOf(LocalDate.now().plusDays(1).toString()) }
    var fechaFin by remember { mutableStateOf(LocalDate.now().plusDays(3).toString()) }
    var pickingStart by remember { mutableStateOf(false) }
    var pickingEnd by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val parsedIni = runCatching { LocalDate.parse(fechaInicio) }.getOrNull()
    val parsedFin = runCatching { LocalDate.parse(fechaFin) }.getOrNull()
    val nights = if (parsedIni != null && parsedFin != null && parsedFin.isAfter(parsedIni)) {
        ChronoUnit.DAYS.between(parsedIni, parsedFin)
    } else 0L
    val estimatedTotal = nights * sitio.alojamiento.precioNoche

    if (pickingStart) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { pickingStart = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        fechaInicio = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()
                    }
                    pickingStart = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { pickingStart = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = state)
        }
    }

    if (pickingEnd) {
        val state = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { pickingEnd = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        fechaFin = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()
                    }
                    pickingEnd = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { pickingEnd = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = state)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .widthIn(max = 720.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AccommodationImage(
            imagePath = sitio.alojamiento.imagen,
            contentDescription = "Imagen de ${sitio.alojamiento.nombre}",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(20.dp))
        )

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, BordeSuave)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(color = VerdeClaro, shape = RoundedCornerShape(999.dp)) {
                    Text(
                        text = "Reserva",
                        color = VerdeProfundo,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = "Solicitar reserva en ${sitio.alojamiento.nombre}",
                    style = MaterialTheme.typography.headlineSmall,
                    color = VerdeProfundo,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ubicación: ${sitio.alojamiento.ubicacion.ifBlank { "Por definir" }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${formatCopPrice(sitio.alojamiento.precioNoche)} / noche",
                    style = MaterialTheme.typography.titleLarge,
                    color = VerdeBosque,
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
                    value = fechaInicio,
                    onValueChange = { fechaInicio = it },
                    label = { Text("Fecha de inicio (AAAA-MM-DD) *") },
                    trailingIcon = {
                        IconButton(onClick = { pickingStart = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Elegir fecha de inicio")
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("booking_start_date")
                )

                OutlinedTextField(
                    value = fechaFin,
                    onValueChange = { fechaFin = it },
                    label = { Text("Fecha de fin (AAAA-MM-DD) *") },
                    trailingIcon = {
                        IconButton(onClick = { pickingEnd = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = "Elegir fecha de fin")
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("booking_end_date")
                )

                if (nights > 0) {
                    Surface(
                        color = VerdeClaro,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$nights noche${if (nights > 1) "s" else ""}",
                                style = MaterialTheme.typography.titleSmall,
                                color = VerdeProfundo
                            )
                            Text(
                                text = "Total: ${formatCopPrice(estimatedTotal)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = VerdeBosque,
                                fontWeight = FontWeight.Bold
                            )
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
                            onCreateBooking(sitio.alojamiento.id, fechaInicio.trim(), fechaFin.trim()) { res ->
                                if (res.isSuccess) onBookingCreated()
                                else errorMsg = res.exceptionOrNull()?.message
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("booking_submit_button"),
                        shape = RoundedCornerShape(999.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VerdeBosque)
                    ) {
                        Text("Enviar solicitud", fontWeight = FontWeight.Bold)
                    }
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text("Cancelar")
                    }
                }
            }
        }
    }
}

@Composable
fun ClientReservationsScreen(
    reservas: List<ReservaDetalle>,
    onOpenSitio: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 900.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Mis reservas",
                    style = MaterialTheme.typography.headlineMedium,
                    color = VerdeProfundo,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Consulta el estado de tus solicitudes de alojamiento.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (reservas.isEmpty()) {
            item {
                EmptyStateCard(
                    title = "Aún no tienes reservas registradas",
                    subtitle = "Explora los alojamientos disponibles en San Luis y solicita tu primera estadía."
                )
            }
        } else {
            items(reservas, key = { "cli_res_${it.reserva.id}" }) { r ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenSitio(r.reserva.alojamientoId) }
                        .testTag("client_reservation_${r.reserva.id}"),
                    shape = RoundedCornerShape(18.dp),
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
                        if (r.alojamientoUbicacion.isNotBlank()) {
                            Text(
                                text = r.alojamientoUbicacion,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${r.reserva.fechaInicio} → ${r.reserva.fechaFin}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total: ${formatCopPrice(r.reserva.total)}",
                                style = MaterialTheme.typography.titleSmall,
                                color = VerdeBosque,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Solicitada: ${r.reserva.creadoEn.take(10)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (r.reserva.estado == "finalizada") {
                            OutlinedButton(
                                onClick = { onOpenSitio(r.reserva.alojamientoId) },
                                shape = RoundedCornerShape(999.dp)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                                Text("Calificar estadía")
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(Modifier.height(12.dp)) }
    }
}

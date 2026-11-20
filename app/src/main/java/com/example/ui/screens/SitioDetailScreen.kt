package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AlojamientoDetalle
import com.example.data.model.UsuarioEntity
import com.example.data.model.ValoracionDetalle
import com.example.ui.components.AccommodationImage
import com.example.ui.components.BusinessOperatorChip
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.InteractiveStarSelector
import com.example.ui.components.RatingStarsRow
import com.example.ui.components.ServiceChip
import com.example.ui.components.StatusPill
import com.example.ui.components.formatCopPrice
import com.example.ui.theme.BordeSuave
import com.example.ui.theme.PillDangerBg
import com.example.ui.theme.PillDangerFg
import com.example.ui.theme.StarAmber
import com.example.ui.theme.VerdeBosque
import com.example.ui.theme.VerdeClaro
import com.example.ui.theme.VerdeProfundo
import com.example.ui.viewmodel.SitioDetailCustomerState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SitioDetailScreen(
    sitio: AlojamientoDetalle?,
    reviews: List<ValoracionDetalle>,
    currentUser: UsuarioEntity?,
    loadCustomerState: suspend () -> SitioDetailCustomerState,
    onSubmitRating: (reservaId: Int, estrellas: Int, comentario: String, onDone: (Result<Unit>) -> Unit) -> Unit,
    onBookClick: (Int) -> Unit,
    onRequireLogin: () -> Unit,
    onBack: () -> Unit
) {
    if (sitio == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EmptyStateCard(
                title = "Alojamiento no disponible",
                subtitle = "Este alojamiento ya no se encuentra publicado o fue retirado."
            )
            Spacer(Modifier.height(16.dp))
            Button(onClick = onBack) { Text("Regresar al inicio") }
        }
        return
    }

    var customerState by remember { mutableStateOf(SitioDetailCustomerState()) }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var selectedStars by remember { mutableIntStateOf(5) }
    var comentarioInput by remember { mutableStateOf("") }
    var ratingError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(sitio.alojamiento.id, currentUser?.id, refreshTrigger) {
        customerState = loadCustomerState()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 900.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AccommodationImage(
                imagePath = sitio.alojamiento.imagen,
                contentDescription = "Imagen de ${sitio.alojamiento.nombre}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(22.dp))
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, BordeSuave)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusPill(status = sitio.alojamiento.estado)
                        if (sitio.totalValoraciones > 0) {
                            RatingStarsRow(
                                rating = sitio.promedioEstrellas,
                                reviewCount = sitio.totalValoraciones
                            )
                        }
                    }

                    Text(
                        text = sitio.alojamiento.nombre,
                        style = MaterialTheme.typography.headlineMedium,
                        color = VerdeProfundo,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Ubicación: ${sitio.alojamiento.ubicacion.ifBlank { "Por definir" }}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    BusinessOperatorChip(name = sitio.nombreNegocio)

                    Text(
                        text = "${formatCopPrice(sitio.alojamiento.precioNoche)} / noche",
                        style = MaterialTheme.typography.headlineSmall,
                        color = VerdeBosque,
                        fontWeight = FontWeight.Bold
                    )

                    if (sitio.alojamiento.rangoPrecio.isNotBlank()) {
                        Text(
                            text = "Rango: ${sitio.alojamiento.rangoPrecio}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (sitio.servicios.isNotEmpty()) {
                        HorizontalDivider(color = BordeSuave)
                        Text(
                            text = "Servicios incluidos",
                            style = MaterialTheme.typography.titleMedium,
                            color = VerdeProfundo,
                            fontWeight = FontWeight.Bold
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            sitio.servicios.forEach { servicio ->
                                ServiceChip(label = servicio.nombre)
                            }
                        }
                    }

                    HorizontalDivider(color = BordeSuave)
                    Text(
                        text = "Descripción",
                        style = MaterialTheme.typography.titleMedium,
                        color = VerdeProfundo,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = sitio.alojamiento.descripcion.ifBlank { "Sin descripción disponible." },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                if (currentUser?.rol == "cliente") {
                                    onBookClick(sitio.alojamiento.id)
                                } else {
                                    onRequireLogin()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("detail_book_button"),
                            shape = RoundedCornerShape(999.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VerdeBosque)
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.size(6.dp))
                            Text("Solicitar reserva", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier.height(48.dp),
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text("Inicio")
                        }
                    }
                }
            }
        }

        // Ratings & Client Rating Submission Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, BordeSuave)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Experiencia de huéspedes",
                        style = MaterialTheme.typography.titleLarge,
                        color = VerdeProfundo,
                        fontWeight = FontWeight.Bold
                    )

                    if (sitio.totalValoraciones > 0) {
                        RatingStarsRow(
                            rating = sitio.promedioEstrellas,
                            reviewCount = sitio.totalValoraciones
                        )
                    } else {
                        Text(
                            text = "Aún no hay calificaciones para este alojamiento.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (currentUser?.rol == "cliente") {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = VerdeClaro.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, Color(0xFFD3E6CF))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val valCliente = customerState.valoracionCliente
                                val resFin = customerState.reservaFinalizada
                                if (valCliente != null) {
                                    Text(
                                        text = "Tu calificación: ${valCliente.estrellas} ⭐",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = VerdeProfundo,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (valCliente.comentario.isNotBlank()) {
                                        Text(
                                            text = "\"${valCliente.comentario}\"",
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Text(
                                        text = "Gracias por compartir tu experiencia.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else if (customerState.puedeValorar && resFin != null) {
                                    Text(
                                        text = "Califica tu estadía",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = VerdeProfundo,
                                        fontWeight = FontWeight.Bold
                                    )
                                    InteractiveStarSelector(
                                        selectedStars = selectedStars,
                                        onSelect = { selectedStars = it }
                                    )
                                    OutlinedTextField(
                                        value = comentarioInput,
                                        onValueChange = { comentarioInput = it },
                                        label = { Text("Comentario") },
                                        placeholder = { Text("Cuéntanos cómo fue tu estadía") },
                                        minLines = 2,
                                        maxLines = 4,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("rating_comment_input")
                                    )
                                    ratingError?.let { err ->
                                        Surface(color = PillDangerBg, shape = RoundedCornerShape(10.dp)) {
                                            Text(
                                                text = err,
                                                color = PillDangerFg,
                                                style = MaterialTheme.typography.bodySmall,
                                                modifier = Modifier.padding(10.dp)
                                            )
                                        }
                                    }
                                    Button(
                                        onClick = {
                                            ratingError = null
                                            onSubmitRating(resFin.id, selectedStars, comentarioInput) { res ->
                                                if (res.isSuccess) {
                                                    comentarioInput = ""
                                                    refreshTrigger++
                                                } else {
                                                    ratingError = res.exceptionOrNull()?.message
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(999.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = VerdeBosque),
                                        modifier = Modifier.testTag("submit_rating_button")
                                    ) {
                                        Text("Enviar calificación", fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    Text(
                                        text = "Solo podrás calificar cuando tu reserva haya sido marcada como Finalizada.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (reviews.isNotEmpty()) {
            items(reviews, key = { "rev_${it.valoracion.id}" }) { review ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, BordeSuave)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = review.clienteNombreCompleto,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = StarAmber,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = " ${review.valoracion.estrellas}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        if (review.valoracion.comentario.isNotBlank()) {
                            Text(
                                text = review.valoracion.comentario,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = review.valoracion.creadoEn.take(10),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

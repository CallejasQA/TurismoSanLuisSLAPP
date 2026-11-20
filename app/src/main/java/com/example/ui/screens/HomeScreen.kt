package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.AlojamientoDetalle
import com.example.ui.components.AccommodationImage
import com.example.ui.components.BusinessOperatorChip
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.RatingStarsRow
import com.example.ui.components.ServiceChip
import com.example.ui.components.StatusPill
import com.example.ui.components.formatCopPrice
import com.example.ui.theme.BordeSuave
import com.example.ui.theme.VerdeBosque
import com.example.ui.theme.VerdeClaro
import com.example.ui.theme.VerdeProfundo
import com.example.ui.viewmodel.HomeFilterState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    sliderItems: List<AlojamientoDetalle>,
    recomendados: List<AlojamientoDetalle>,
    sitios: List<AlojamientoDetalle>,
    operadores: List<String>,
    filterState: HomeFilterState,
    onApplyFilters: (String, String, Int?) -> Unit,
    onClearFilters: () -> Unit,
    onOpenSitio: (Int) -> Unit,
    onOpenAffiliateRegister: () -> Unit
) {
    var ubicacionText by remember(filterState.ubicacion) { mutableStateOf(filterState.ubicacion) }
    var selectedOperador by remember(filterState.operador) { mutableStateOf(filterState.operador) }
    var selectedEstrellas by remember(filterState.minEstrellas) { mutableStateOf(filterState.minEstrellas) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWide = maxWidth >= 700.dp
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 1100.dp)
                .align(Alignment.TopCenter),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 1. Highlight Slider (Carrusel destacado)
            if (sliderItems.isNotEmpty()) {
                item {
                    HighlightSliderSection(
                        items = sliderItems,
                        onOpenSitio = onOpenSitio
                    )
                }
            }

            // 2. Hero Nature Banner + Search Card
            item {
                HeroSearchSection(
                    ubicacion = ubicacionText,
                    onUbicacionChange = { ubicacionText = it },
                    operadores = operadores,
                    selectedOperador = selectedOperador,
                    onSelectOperador = { selectedOperador = it },
                    selectedEstrellas = selectedEstrellas,
                    onSelectEstrellas = { selectedEstrellas = it },
                    hasActiveFilter = filterState.ubicacion.isNotBlank() ||
                        filterState.operador.isNotBlank() ||
                        filterState.minEstrellas != null,
                    onSearch = { onApplyFilters(ubicacionText, selectedOperador, selectedEstrellas) },
                    onClear = {
                        ubicacionText = ""
                        selectedOperador = ""
                        selectedEstrellas = null
                        onClearFilters()
                    },
                    onOpenAffiliateRegister = onOpenAffiliateRegister
                )
            }

            // 3. Recomendados para ti
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column {
                        Text(
                            text = "Recomendados para ti",
                            style = MaterialTheme.typography.headlineSmall,
                            color = VerdeProfundo,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Selección curada de alojamientos con los mejores paisajes y comodidades.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (recomendados.isEmpty()) {
                        EmptyStateCard(
                            title = "Sin recomendados todavía",
                            subtitle = "Aún no hay alojamientos destacados. ¡Publica el primero y luce aquí!"
                        )
                    } else {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(recomendados, key = { "rec_${it.alojamiento.id}" }) { item ->
                                RecommendedCard(
                                    item = item,
                                    onClick = { onOpenSitio(item.alojamiento.id) }
                                )
                            }
                        }
                    }
                }
            }

            // 4. Alojamientos disponibles
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Alojamientos disponibles",
                        style = MaterialTheme.typography.headlineSmall,
                        color = VerdeProfundo,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Opciones activas y aprobadas listas para recibirte en San Luis.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (sitios.isEmpty()) {
                item {
                    EmptyStateCard(
                        title = "No se encontraron alojamientos",
                        subtitle = "Ajusta los filtros de búsqueda o limpia los criterios para ver toda la oferta."
                    )
                }
            } else if (isWide) {
                val rows = sitios.chunked(2)
                items(rows) { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        pair.forEach { item ->
                            Box(modifier = Modifier.weight(1f)) {
                                AccommodationCard(item = item, onClick = { onOpenSitio(item.alojamiento.id) })
                            }
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else {
                items(sitios, key = { "sitio_${it.alojamiento.id}" }) { item ->
                    AccommodationCard(item = item, onClick = { onOpenSitio(item.alojamiento.id) })
                }
            }

            item { Spacer(Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun HighlightSliderSection(
    items: List<AlojamientoDetalle>,
    onOpenSitio: (Int) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { items.size })
    val scope = rememberCoroutineScope()

    LaunchedEffect(items.size) {
        if (items.size > 1) {
            while (true) {
                delay(6500)
                val next = (pagerState.currentPage + 1) % items.size
                pagerState.animateScrollToPage(next)
            }
        }
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, BordeSuave),
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "RECOMENDADOS",
                        style = MaterialTheme.typography.labelSmall,
                        color = VerdeBosque,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Alojamientos destacados",
                        style = MaterialTheme.typography.titleLarge,
                        color = VerdeProfundo,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (items.size > 1) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            shape = CircleShape,
                            border = BorderStroke(1.dp, Color(0xFFD3E6CF)),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        val prev = if (pagerState.currentPage == 0) items.lastIndex else pagerState.currentPage - 1
                                        pagerState.animateScrollToPage(prev)
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("slider_prev_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Anterior",
                                    tint = VerdeProfundo
                                )
                            }
                        }
                        Surface(
                            shape = CircleShape,
                            border = BorderStroke(1.dp, Color(0xFFD3E6CF)),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        val next = (pagerState.currentPage + 1) % items.size
                                        pagerState.animateScrollToPage(next)
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("slider_next_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Siguiente",
                                    tint = VerdeProfundo
                                )
                            }
                        }
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                pageSpacing = 12.dp
            ) { page ->
                val item = items[page]
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenSitio(item.alojamiento.id) }
                        .testTag("slider_card_${item.alojamiento.id}"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFBFDF9)),
                    border = BorderStroke(1.dp, BordeSuave)
                ) {
                    Column {
                        AccommodationImage(
                            imagePath = item.alojamiento.imagen,
                            contentDescription = "Imagen de ${item.alojamiento.nombre}",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                        )
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                color = VerdeClaro,
                                shape = RoundedCornerShape(999.dp)
                            ) {
                                Text(
                                    text = "Carrusel",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = VerdeProfundo,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = item.alojamiento.nombre,
                                style = MaterialTheme.typography.titleLarge,
                                color = VerdeProfundo,
                                fontWeight = FontWeight.Bold
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = item.alojamiento.ubicacion.ifBlank { "Ubicación por definir" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${formatCopPrice(item.alojamiento.precioNoche)} / noche",
                                style = MaterialTheme.typography.titleMedium,
                                color = VerdeBosque,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Operado por ${item.nombreNegocio}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Button(
                                onClick = { onOpenSitio(item.alojamiento.id) },
                                shape = RoundedCornerShape(999.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = VerdeBosque)
                            ) {
                                Text("Ver detalles")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeroSearchSection(
    ubicacion: String,
    onUbicacionChange: (String) -> Unit,
    operadores: List<String>,
    selectedOperador: String,
    onSelectOperador: (String) -> Unit,
    selectedEstrellas: Int?,
    onSelectEstrellas: (Int?) -> Unit,
    hasActiveFilter: Boolean,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    onOpenAffiliateRegister: () -> Unit
) {
    var operadorMenuOpen by remember { mutableStateOf(false) }
    var estrellasMenuOpen by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data("file:///android_asset/images/1763917708_rio-samana-san-luis-antioquia.jpg")
                .crossfade(true)
                .build(),
            contentDescription = "Paisaje natural de San Luis",
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xB31B5E20),
                            Color(0xD91B5E20)
                        )
                    )
                )
        )
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "ESCÁPATE A LA NATURALEZA",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFFD4E9D8),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Encuentra alojamientos rodeados de naturaleza",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Explora hospedajes con encanto natural, tarifas claras y anfitriones verificados en San Luis.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFE9F4EA)
            )

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = ubicacion,
                        onValueChange = onUbicacionChange,
                        label = { Text("Ubicación o nombre") },
                        placeholder = { Text("Ej. Vereda La Cristalina, Samaná...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_location_input")
                    )

                    ExposedDropdownMenuBox(
                        expanded = operadorMenuOpen,
                        onExpandedChange = { operadorMenuOpen = !operadorMenuOpen }
                    ) {
                        OutlinedTextField(
                            value = selectedOperador.ifBlank { "Cualquier operador" },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Operado por") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = operadorMenuOpen) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .testTag("filter_operator_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = operadorMenuOpen,
                            onDismissRequest = { operadorMenuOpen = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Cualquier operador") },
                                onClick = {
                                    onSelectOperador("")
                                    operadorMenuOpen = false
                                }
                            )
                            operadores.forEach { op ->
                                DropdownMenuItem(
                                    text = { Text(op) },
                                    onClick = {
                                        onSelectOperador(op)
                                        operadorMenuOpen = false
                                    }
                                )
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = estrellasMenuOpen,
                        onExpandedChange = { estrellasMenuOpen = !estrellasMenuOpen }
                    ) {
                        val ratingText = selectedEstrellas?.let {
                            "$it estrella${if (it > 1) "s" else ""} o más"
                        } ?: "Cualquier calificación"
                        OutlinedTextField(
                            value = ratingText,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Experiencia mínima") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = estrellasMenuOpen) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .testTag("filter_stars_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = estrellasMenuOpen,
                            onDismissRequest = { estrellasMenuOpen = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Cualquier calificación") },
                                onClick = {
                                    onSelectEstrellas(null)
                                    estrellasMenuOpen = false
                                }
                            )
                            (1..5).forEach { stars ->
                                DropdownMenuItem(
                                    text = { Text("$stars estrella${if (stars > 1) "s" else ""} o más") },
                                    onClick = {
                                        onSelectEstrellas(stars)
                                        estrellasMenuOpen = false
                                    }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onSearch,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("home_search_button"),
                            shape = RoundedCornerShape(999.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VerdeBosque)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Buscar", fontWeight = FontWeight.Bold)
                        }
                        if (hasActiveFilter) {
                            OutlinedButton(
                                onClick = onClear,
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("home_clear_filters_button"),
                                shape = RoundedCornerShape(999.dp)
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar", modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onOpenAffiliateRegister,
                    shape = RoundedCornerShape(999.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = VerdeProfundo),
                    modifier = Modifier.testTag("hero_affiliate_cta")
                ) {
                    Text("Afíliate como anfitrión", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun RecommendedCard(
    item: AlojamientoDetalle,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(265.dp)
            .clickable(onClick = onClick)
            .testTag("recommended_card_${item.alojamiento.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BordeSuave),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AccommodationImage(
                imagePath = item.alojamiento.imagen,
                contentDescription = "Imagen de ${item.alojamiento.nombre}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusPill(status = item.alojamiento.estado)
                Text(
                    text = item.alojamiento.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = VerdeProfundo,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.alojamiento.ubicacion.ifBlank { "Ubicación por definir" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${formatCopPrice(item.alojamiento.precioNoche)} / noche",
                    style = MaterialTheme.typography.titleSmall,
                    color = VerdeBosque,
                    fontWeight = FontWeight.Bold
                )
                if (item.servicios.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item.servicios.take(2).forEach { serv ->
                            ServiceChip(label = serv.nombre)
                        }
                    }
                }
                TextButton(
                    onClick = onClick,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Ver detalles", color = VerdeProfundo, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AccommodationCard(
    item: AlojamientoDetalle,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("sitio_card_${item.alojamiento.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BordeSuave),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AccommodationImage(
                imagePath = item.alojamiento.imagen,
                contentDescription = "Imagen de ${item.alojamiento.nombre}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(195.dp)
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
                    StatusPill(status = item.alojamiento.estado)
                    if (item.totalValoraciones > 0) {
                        RatingStarsRow(
                            rating = item.promedioEstrellas,
                            reviewCount = item.totalValoraciones
                        )
                    } else {
                        Text(
                            text = "Sin calificaciones todavía",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = item.alojamiento.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    color = VerdeProfundo,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = item.alojamiento.ubicacion.ifBlank { "Ubicación por definir" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                BusinessOperatorChip(name = item.nombreNegocio)

                Text(
                    text = "${formatCopPrice(item.alojamiento.precioNoche)} / noche",
                    style = MaterialTheme.typography.titleMedium,
                    color = VerdeBosque,
                    fontWeight = FontWeight.Bold
                )

                if (item.alojamiento.descripcion.isNotBlank()) {
                    Text(
                        text = item.alojamiento.descripcion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (item.servicios.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item.servicios.take(6).forEach { serv ->
                            ServiceChip(label = serv.nombre)
                        }
                        if (item.servicios.size > 6) {
                            ServiceChip(
                                label = "+${item.servicios.size - 6}",
                                highlighted = true
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))
                OutlinedButton(
                    onClick = onClick,
                    shape = RoundedCornerShape(999.dp),
                    border = BorderStroke(1.dp, VerdeProfundo),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver detalles", color = VerdeProfundo, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

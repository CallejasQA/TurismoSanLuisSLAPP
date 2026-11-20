package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.UsuarioEntity
import com.example.ui.theme.BordeSuave
import com.example.ui.theme.PillDangerBg
import com.example.ui.theme.PillDangerFg
import com.example.ui.theme.PillSuccessBg
import com.example.ui.theme.PillSuccessFg
import com.example.ui.theme.PillWarningBg
import com.example.ui.theme.PillWarningFg
import com.example.ui.theme.StarAmber
import com.example.ui.theme.VerdeBosque
import com.example.ui.theme.VerdeClaro
import com.example.ui.theme.VerdeProfundo
import java.text.NumberFormat
import java.util.Locale

fun formatCopPrice(amount: Double): String {
    val format = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-CO")).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }
    return "$" + format.format(amount)
}

@Composable
fun StatusPill(
    status: String,
    modifier: Modifier = Modifier
) {
    val clean = status.lowercase().trim()
    val (bg, fg) = when (clean) {
        "activo", "aprobado", "confirmada", "finalizada" -> PillSuccessBg to PillSuccessFg
        "rechazado", "cancelada", "inactivo" -> PillDangerBg to PillDangerFg
        else -> PillWarningBg to PillWarningFg
    }
    Surface(
        modifier = modifier,
        color = bg,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = clean.replaceFirstChar { it.uppercase() },
            color = fg,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun ServiceChip(
    label: String,
    highlighted: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = if (highlighted) VerdeBosque else VerdeClaro,
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, if (highlighted) VerdeBosque else Color(0xFFD3E6CF))
    ) {
        Text(
            text = label,
            color = if (highlighted) Color.White else VerdeProfundo,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun BusinessOperatorChip(
    name: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color(0xFFEEF6EC),
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, Color(0xFFD8E9D5))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Apartment,
                contentDescription = null,
                tint = VerdeProfundo,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "OPERADO POR",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = name,
                style = MaterialTheme.typography.labelMedium,
                color = VerdeProfundo,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun AccommodationImage(
    imagePath: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    if (imagePath.isNotBlank()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imagePath)
                .crossfade(true)
                .build(),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier.background(VerdeClaro)
        )
    } else {
        Box(
            modifier = modifier.background(VerdeClaro),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Landscape,
                    contentDescription = null,
                    tint = VerdeProfundo.copy(alpha = 0.6f),
                    modifier = Modifier.size(36.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Sin imagen",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun RatingStarsRow(
    rating: Double,
    reviewCount: Int? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Calificación",
            tint = StarAmber,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = String.format(Locale.US, "%.1f", rating),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (reviewCount != null) {
            Text(
                text = "($reviewCount reseña${if (reviewCount == 1) "" else "s"})",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun InteractiveStarSelector(
    selectedStars: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        (1..5).forEach { star ->
            IconButton(
                onClick = { onSelect(star) },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("star_select_$star")
            ) {
                Icon(
                    imageVector = if (star <= selectedStars) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "$star estrellas",
                    tint = if (star <= selectedStars) StarAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, BordeSuave),
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = VerdeClaro,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Default.Eco,
                        contentDescription = null,
                        tint = VerdeBosque,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = VerdeProfundo,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TurismoTopBar(
    title: String,
    currentUser: UsuarioEntity?,
    showBack: Boolean,
    onBack: () -> Unit,
    onLoginClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!showBack) {
                    Icon(
                        imageVector = Icons.Default.Eco,
                        contentDescription = null,
                        tint = VerdeBosque,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = VerdeProfundo,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        navigationIcon = {
            if (showBack) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("top_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver",
                        tint = VerdeProfundo
                    )
                }
            }
        },
        actions = {
            if (currentUser != null) {
                Surface(
                    color = VerdeClaro,
                    shape = RoundedCornerShape(999.dp),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(
                        text = currentUser.rol.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.labelSmall,
                        color = VerdeProfundo,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                IconButton(
                    onClick = onLogoutClick,
                    modifier = Modifier.testTag("top_logout_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Salir",
                        tint = VerdeProfundo
                    )
                }
            } else {
                TextButton(
                    onClick = onLoginClick,
                    modifier = Modifier.testTag("top_login_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Login,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = VerdeBosque
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Ingresar", color = VerdeProfundo, fontWeight = FontWeight.Bold)
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

enum class MainTab {
    HOME,
    ROLE_PORTAL,
    ACCOUNT
}

@Composable
fun TurismoBottomBar(
    currentTab: MainTab,
    currentUser: UsuarioEntity?,
    onSelectHome: () -> Unit,
    onSelectPortal: () -> Unit,
    onSelectAccount: () -> Unit
) {
    val (portalLabel, portalSelectedIcon, portalUnselectedIcon) = when (currentUser?.rol) {
        "admin" -> Triple("Admin", Icons.Default.AdminPanelSettings, Icons.Outlined.AdminPanelSettings)
        "propietario" -> Triple("Mi Panel", Icons.Default.Storefront, Icons.Outlined.Storefront)
        else -> Triple("Mis reservas", Icons.Default.CalendarMonth, Icons.Outlined.CalendarMonth)
    }

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        NavigationBarItem(
            selected = currentTab == MainTab.HOME,
            onClick = onSelectHome,
            modifier = Modifier.testTag("nav_tab_home"),
            icon = {
                Icon(
                    imageVector = if (currentTab == MainTab.HOME) Icons.Default.Home else Icons.Outlined.Home,
                    contentDescription = "Inicio"
                )
            },
            label = { Text("Inicio") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = VerdeProfundo,
                selectedTextColor = VerdeProfundo,
                indicatorColor = VerdeClaro
            )
        )
        if (currentUser != null) {
            NavigationBarItem(
                selected = currentTab == MainTab.ROLE_PORTAL,
                onClick = onSelectPortal,
                modifier = Modifier.testTag("nav_tab_portal"),
                icon = {
                    Icon(
                        imageVector = if (currentTab == MainTab.ROLE_PORTAL) portalSelectedIcon else portalUnselectedIcon,
                        contentDescription = portalLabel
                    )
                },
                label = { Text(portalLabel) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = VerdeProfundo,
                    selectedTextColor = VerdeProfundo,
                    indicatorColor = VerdeClaro
                )
            )
        }
        NavigationBarItem(
            selected = currentTab == MainTab.ACCOUNT,
            onClick = onSelectAccount,
            modifier = Modifier.testTag("nav_tab_account"),
            icon = {
                Icon(
                    imageVector = if (currentTab == MainTab.ACCOUNT) Icons.Default.Person else Icons.Outlined.Person,
                    contentDescription = if (currentUser == null) "Ingresar" else "Cuenta"
                )
            },
            label = { Text(if (currentUser == null) "Ingresar" else "Cuenta") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = VerdeProfundo,
                selectedTextColor = VerdeProfundo,
                indicatorColor = VerdeClaro
            )
        )
    }
}

@Composable
fun TurismoNavigationRail(
    currentTab: MainTab,
    currentUser: UsuarioEntity?,
    onSelectHome: () -> Unit,
    onSelectPortal: () -> Unit,
    onSelectAccount: () -> Unit
) {
    val (portalLabel, portalSelectedIcon, portalUnselectedIcon) = when (currentUser?.rol) {
        "admin" -> Triple("Admin", Icons.Default.AdminPanelSettings, Icons.Outlined.AdminPanelSettings)
        "propietario" -> Triple("Mi Panel", Icons.Default.Storefront, Icons.Outlined.Storefront)
        else -> Triple("Mis reservas", Icons.Default.CalendarMonth, Icons.Outlined.CalendarMonth)
    }

    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Spacer(Modifier.height(12.dp))
        NavigationRailItem(
            selected = currentTab == MainTab.HOME,
            onClick = onSelectHome,
            icon = {
                Icon(
                    imageVector = if (currentTab == MainTab.HOME) Icons.Default.Home else Icons.Outlined.Home,
                    contentDescription = "Inicio"
                )
            },
            label = { Text("Inicio") }
        )
        if (currentUser != null) {
            NavigationRailItem(
                selected = currentTab == MainTab.ROLE_PORTAL,
                onClick = onSelectPortal,
                icon = {
                    Icon(
                        imageVector = if (currentTab == MainTab.ROLE_PORTAL) portalSelectedIcon else portalUnselectedIcon,
                        contentDescription = portalLabel
                    )
                },
                label = { Text(portalLabel) }
            )
        }
        NavigationRailItem(
            selected = currentTab == MainTab.ACCOUNT,
            onClick = onSelectAccount,
            icon = {
                Icon(
                    imageVector = if (currentTab == MainTab.ACCOUNT) Icons.Default.Person else Icons.Outlined.Person,
                    contentDescription = if (currentUser == null) "Ingresar" else "Cuenta"
                )
            },
            label = { Text(if (currentUser == null) "Ingresar" else "Cuenta") }
        )
    }
}

@Composable
fun TurismoConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String = "Eliminar",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = PillDangerFg)
            ) {
                Text(confirmLabel, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

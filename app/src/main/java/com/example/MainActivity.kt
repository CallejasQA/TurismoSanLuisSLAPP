package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.ui.components.MainTab
import com.example.ui.components.TurismoBottomBar
import com.example.ui.components.TurismoNavigationRail
import com.example.ui.components.TurismoTopBar
import com.example.ui.navigation.AdminAffiliatesRoute
import com.example.ui.navigation.AdminAgendaRoute
import com.example.ui.navigation.AdminClientFormRoute
import com.example.ui.navigation.AdminClientsRoute
import com.example.ui.navigation.AdminHubRoute
import com.example.ui.navigation.AdminReservationsRoute
import com.example.ui.navigation.AdminReviewsRoute
import com.example.ui.navigation.AdminServicesRoute
import com.example.ui.navigation.AdminSettingsRoute
import com.example.ui.navigation.AdminSitesRoute
import com.example.ui.navigation.BookSitioRoute
import com.example.ui.navigation.ClientReservationsRoute
import com.example.ui.navigation.HomeRoute
import com.example.ui.navigation.LoginRoute
import com.example.ui.navigation.OwnerAgendaRoute
import com.example.ui.navigation.OwnerReservationsRoute
import com.example.ui.navigation.OwnerReviewsRoute
import com.example.ui.navigation.OwnerSiteFormRoute
import com.example.ui.navigation.OwnerSitesRoute
import com.example.ui.navigation.RegisterAffiliateRoute
import com.example.ui.navigation.RegisterClientRoute
import com.example.ui.navigation.SitioDetailRoute
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.AdminAffiliatesScreen
import com.example.ui.screens.AdminClientFormScreen
import com.example.ui.screens.AdminClientsScreen
import com.example.ui.screens.AdminHubScreen
import com.example.ui.screens.AdminServicesScreen
import com.example.ui.screens.AdminSettingsScreen
import com.example.ui.screens.AdminSitesScreen
import com.example.ui.screens.AgendaScreen
import com.example.ui.screens.BookingFormScreen
import com.example.ui.screens.ClientReservationsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.OwnerSitesScreen
import com.example.ui.screens.RegisterAffiliateScreen
import com.example.ui.screens.RegisterClientScreen
import com.example.ui.screens.ReservationsManageScreen
import com.example.ui.screens.ReviewsManageScreen
import com.example.ui.screens.SiteFormScreen
import com.example.ui.screens.SitioDetailScreen
import com.example.ui.theme.TurismoSanLuisTheme
import com.example.ui.viewmodel.TurismoViewModel
import kotlinx.serialization.Serializable

@Serializable
object AccountRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as TurismoSanLuisApp
        setContent {
            TurismoSanLuisTheme {
                val vm: TurismoViewModel = viewModel(
                    factory = TurismoViewModel.Factory(app.repository)
                )
                TurismoSanLuisRoot(vm = vm)
            }
        }
    }
}

@Composable
private fun TurismoSanLuisRoot(vm: TurismoViewModel) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentUser by vm.currentUser.collectAsStateWithLifecycle()
    val backgroundImage by vm.backgroundImage.collectAsStateWithLifecycle()
    val allAlojamientos by vm.allAlojamientos.collectAsStateWithLifecycle()
    val publicSitios by vm.filteredPublicAlojamientos.collectAsStateWithLifecycle()
    val sliderItems by vm.sliderAlojamientos.collectAsStateWithLifecycle()
    val recomendados by vm.recomendadosAlojamientos.collectAsStateWithLifecycle()
    val operadores by vm.operadoresActivos.collectAsStateWithLifecycle()
    val filterState by vm.filterState.collectAsStateWithLifecycle()
    val servicios by vm.servicios.collectAsStateWithLifecycle()
    val afiliados by vm.afiliados.collectAsStateWithLifecycle()
    val allReservas by vm.allReservas.collectAsStateWithLifecycle()
    val allValoraciones by vm.allValoraciones.collectAsStateWithLifecycle()
    val clientes by vm.clientes.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        vm.snackbarMessages.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val destRoute = backStackEntry?.destination?.route.orEmpty()

    val isHome = destRoute.contains("HomeRoute") || destRoute.isEmpty()
    val currentTab = when {
        isHome -> MainTab.HOME
        destRoute.contains("LoginRoute") ||
            destRoute.contains("RegisterAffiliateRoute") ||
            destRoute.contains("RegisterClientRoute") ||
            destRoute.contains("AccountRoute") -> MainTab.ACCOUNT
        destRoute.contains("Owner") ||
            destRoute.contains("Admin") ||
            destRoute.contains("ClientReservationsRoute") -> MainTab.ROLE_PORTAL
        else -> MainTab.HOME
    }

    val topTitle = when {
        destRoute.contains("SitioDetailRoute") -> "Detalle del alojamiento"
        destRoute.contains("BookSitioRoute") -> "Solicitar reserva"
        destRoute.contains("ClientReservationsRoute") -> "Mis reservas"
        destRoute.contains("LoginRoute") -> "Ingresar"
        destRoute.contains("RegisterAffiliateRoute") -> "Afíliate"
        destRoute.contains("RegisterClientRoute") -> "Regístrate"
        destRoute.contains("AccountRoute") -> "Mi cuenta"
        destRoute.contains("OwnerSitesRoute") -> "Mis Alojamientos"
        destRoute.contains("OwnerSiteFormRoute") -> "Alojamiento"
        destRoute.contains("OwnerReservationsRoute") -> "Reservas"
        destRoute.contains("OwnerAgendaRoute") -> "Mi agenda"
        destRoute.contains("OwnerReviewsRoute") -> "Comentarios"
        destRoute.contains("AdminHubRoute") -> "Panel Administrador"
        destRoute.contains("AdminAffiliatesRoute") -> "Afiliaciones"
        destRoute.contains("AdminSitesRoute") -> "Alojamientos"
        destRoute.contains("AdminServicesRoute") -> "Servicios"
        destRoute.contains("AdminClientsRoute") -> "Clientes"
        destRoute.contains("AdminClientFormRoute") -> "Cliente"
        destRoute.contains("AdminReservationsRoute") -> "Reservas"
        destRoute.contains("AdminAgendaRoute") -> "Agenda"
        destRoute.contains("AdminReviewsRoute") -> "Comentarios"
        destRoute.contains("AdminSettingsRoute") -> "Configuración"
        else -> "Turismo San Luis"
    }

    fun navigateToRolePortal() {
        when (currentUser?.rol) {
            "admin" -> navController.navigate(AdminHubRoute) { launchSingleTop = true }
            "propietario" -> navController.navigate(OwnerSitesRoute) { launchSingleTop = true }
            "cliente" -> navController.navigate(ClientReservationsRoute) { launchSingleTop = true }
            else -> navController.navigate(LoginRoute) { launchSingleTop = true }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 840.dp

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TurismoTopBar(
                    title = topTitle,
                    currentUser = currentUser,
                    showBack = !isHome && navController.previousBackStackEntry != null,
                    onBack = { navController.popBackStack() },
                    onLoginClick = { navController.navigate(LoginRoute) { launchSingleTop = true } },
                    onLogoutClick = {
                        vm.logout()
                        navController.navigate(HomeRoute) {
                            popUpTo(HomeRoute) { inclusive = true }
                        }
                    }
                )
            },
            bottomBar = {
                if (!useNavRail) {
                    TurismoBottomBar(
                        currentTab = currentTab,
                        currentUser = currentUser,
                        onSelectHome = {
                            navController.navigate(HomeRoute) {
                                popUpTo(HomeRoute) { inclusive = true }
                            }
                        },
                        onSelectPortal = { navigateToRolePortal() },
                        onSelectAccount = {
                            if (currentUser == null) {
                                navController.navigate(LoginRoute) { launchSingleTop = true }
                            } else {
                                navController.navigate(AccountRoute) { launchSingleTop = true }
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavRail) {
                    TurismoNavigationRail(
                        currentTab = currentTab,
                        currentUser = currentUser,
                        onSelectHome = {
                            navController.navigate(HomeRoute) {
                                popUpTo(HomeRoute) { inclusive = true }
                            }
                        },
                        onSelectPortal = { navigateToRolePortal() },
                        onSelectAccount = {
                            if (currentUser == null) {
                                navController.navigate(LoginRoute) { launchSingleTop = true }
                            } else {
                                navController.navigate(AccountRoute) { launchSingleTop = true }
                            }
                        }
                    )
                }

                NavHost(
                    navController = navController,
                    startDestination = HomeRoute,
                    modifier = Modifier.weight(1f)
                ) {
                    composable<HomeRoute> {
                        HomeScreen(
                            sliderItems = sliderItems,
                            recomendados = recomendados,
                            sitios = publicSitios,
                            operadores = operadores,
                            filterState = filterState,
                            onApplyFilters = vm::updateFilters,
                            onClearFilters = vm::clearFilters,
                            onOpenSitio = { id -> navController.navigate(SitioDetailRoute(id)) },
                            onOpenAffiliateRegister = { navController.navigate(RegisterAffiliateRoute) }
                        )
                    }

                    composable<SitioDetailRoute> { entry ->
                        val args = entry.toRoute<SitioDetailRoute>()
                        val sitio = remember(allAlojamientos, args.sitioId) {
                            allAlojamientos.find {
                                it.alojamiento.id == args.sitioId &&
                                    it.alojamiento.estado in listOf("aprobado", "activo")
                            }
                        }
                        val sitioReviews = remember(allValoraciones, args.sitioId) {
                            allValoraciones.filter { it.valoracion.alojamientoId == args.sitioId }
                        }
                        SitioDetailScreen(
                            sitio = sitio,
                            reviews = sitioReviews,
                            currentUser = currentUser,
                            loadCustomerState = { vm.loadDetailCustomerState(args.sitioId) },
                            onSubmitRating = vm::submitRating,
                            onBookClick = { id -> navController.navigate(BookSitioRoute(id)) },
                            onRequireLogin = {
                                vm.emitMessage("Inicia sesión como cliente para solicitar una reserva.")
                                navController.navigate(LoginRoute)
                            },
                            onBack = { navController.popBackStack(HomeRoute, inclusive = false) }
                        )
                    }

                    composable<LoginRoute> {
                        LoginScreen(
                            backgroundImageUrl = backgroundImage,
                            onLogin = vm::login,
                            onLoginSuccess = { rol ->
                                when (rol) {
                                    "admin" -> navController.navigate(AdminHubRoute) {
                                        popUpTo(HomeRoute)
                                    }
                                    "propietario" -> navController.navigate(OwnerSitesRoute) {
                                        popUpTo(HomeRoute)
                                    }
                                    else -> navController.navigate(HomeRoute) {
                                        popUpTo(HomeRoute) { inclusive = true }
                                    }
                                }
                            },
                            onNavigateAffiliate = { navController.navigate(RegisterAffiliateRoute) },
                            onNavigateRegisterClient = { navController.navigate(RegisterClientRoute) }
                        )
                    }

                    composable<RegisterAffiliateRoute> {
                        RegisterAffiliateScreen(
                            backgroundImageUrl = backgroundImage,
                            onRegister = vm::registerAffiliate,
                            onNavigateLogin = { navController.navigate(LoginRoute) }
                        )
                    }

                    composable<RegisterClientRoute> {
                        RegisterClientScreen(
                            backgroundImageUrl = backgroundImage,
                            onRegister = vm::registerClient,
                            onNavigateLogin = { navController.navigate(LoginRoute) }
                        )
                    }

                    composable<AccountRoute> {
                        val user = currentUser
                        if (user == null) {
                            LaunchedEffect(Unit) {
                                navController.navigate(LoginRoute) {
                                    popUpTo(AccountRoute) { inclusive = true }
                                }
                            }
                        } else {
                            AccountScreen(
                                user = user,
                                onOpenRolePortal = { navigateToRolePortal() },
                                onLogout = {
                                    vm.logout()
                                    navController.navigate(HomeRoute) {
                                        popUpTo(HomeRoute) { inclusive = true }
                                    }
                                }
                            )
                        }
                    }

                    composable<BookSitioRoute> { entry ->
                        val args = entry.toRoute<BookSitioRoute>()
                        val sitio = remember(allAlojamientos, args.sitioId) {
                            allAlojamientos.find { it.alojamiento.id == args.sitioId }
                        }
                        BookingFormScreen(
                            sitio = sitio,
                            onCreateBooking = vm::createBooking,
                            onBookingCreated = {
                                navController.navigate(ClientReservationsRoute) {
                                    popUpTo(HomeRoute)
                                }
                            },
                            onCancel = { navController.popBackStack() }
                        )
                    }

                    composable<ClientReservationsRoute> {
                        val userId = currentUser?.id ?: -1
                        val myReservas = remember(allReservas, userId) {
                            allReservas.filter { it.reserva.clienteId == userId }
                        }
                        ClientReservationsScreen(
                            reservas = myReservas,
                            onOpenSitio = { id -> navController.navigate(SitioDetailRoute(id)) }
                        )
                    }

                    // --- Propietario ---
                    composable<OwnerSitesRoute> {
                        val userId = currentUser?.id ?: -1
                        val ownerSitios = remember(allAlojamientos, userId) {
                            allAlojamientos.filter { it.alojamiento.propietarioId == userId }
                        }
                        OwnerSitesScreen(
                            sitios = ownerSitios,
                            onCreateSite = { navController.navigate(OwnerSiteFormRoute(null)) },
                            onEditSite = { id -> navController.navigate(OwnerSiteFormRoute(id)) },
                            onDeleteSite = vm::deleteAlojamiento,
                            onOpenReservations = { navController.navigate(OwnerReservationsRoute) },
                            onOpenAgenda = { navController.navigate(OwnerAgendaRoute) },
                            onOpenReviews = { navController.navigate(OwnerReviewsRoute) }
                        )
                    }

                    composable<OwnerSiteFormRoute> { entry ->
                        val args = entry.toRoute<OwnerSiteFormRoute>()
                        SiteFormScreen(
                            sitioId = args.sitioId,
                            allServicios = servicios,
                            loadInitial = vm::getAlojamientoForEdit,
                            onPickImage = vm::savePickedSiteImage,
                            onSave = vm::saveAlojamiento,
                            onSaved = { navController.popBackStack() },
                            onCancel = { navController.popBackStack() }
                        )
                    }

                    composable<OwnerReservationsRoute> {
                        val userId = currentUser?.id ?: -1
                        val ownerReservas = remember(allReservas, userId) {
                            allReservas.filter { it.propietarioId == userId }
                        }
                        ReservationsManageScreen(
                            isAdmin = false,
                            reservas = ownerReservas,
                            onUpdateEstado = vm::updateReservaEstado
                        )
                    }

                    composable<OwnerAgendaRoute> {
                        val userId = currentUser?.id ?: -1
                        val ownerReservas = remember(allReservas, userId) {
                            allReservas.filter { it.propietarioId == userId }
                        }
                        AgendaScreen(
                            isAdmin = false,
                            reservas = ownerReservas,
                            buildAgenda = vm::buildAgenda
                        )
                    }

                    composable<OwnerReviewsRoute> {
                        val userId = currentUser?.id ?: -1
                        val ownerReviews = remember(allValoraciones, userId) {
                            allValoraciones.filter { it.propietarioId == userId }
                        }
                        ReviewsManageScreen(
                            isAdmin = false,
                            valoraciones = ownerReviews,
                            onUpdateComment = vm::updateValoracionComentario,
                            onDeleteReview = vm::deleteValoracion
                        )
                    }

                    // --- Admin ---
                    composable<AdminHubRoute> {
                        AdminHubScreen(
                            pendingAffiliatesCount = afiliados.count { it.afiliado.estado == "pendiente" },
                            pendingSitiosCount = allAlojamientos.count { it.alojamiento.estado == "pendiente" },
                            servicesCount = servicios.size,
                            clientsCount = clientes.size,
                            reservasCount = allReservas.size,
                            reviewsCount = allValoraciones.size,
                            onOpenAffiliates = { navController.navigate(AdminAffiliatesRoute) },
                            onOpenSites = { navController.navigate(AdminSitesRoute) },
                            onOpenServices = { navController.navigate(AdminServicesRoute) },
                            onOpenClients = { navController.navigate(AdminClientsRoute) },
                            onOpenReservations = { navController.navigate(AdminReservationsRoute) },
                            onOpenAgenda = { navController.navigate(AdminAgendaRoute) },
                            onOpenReviews = { navController.navigate(AdminReviewsRoute) },
                            onOpenSettings = { navController.navigate(AdminSettingsRoute) }
                        )
                    }

                    composable<AdminAffiliatesRoute> {
                        AdminAffiliatesScreen(
                            afiliados = afiliados,
                            onUpdateEstado = vm::updateAfiliacionEstado
                        )
                    }

                    composable<AdminSitesRoute> {
                        AdminSitesScreen(
                            alojamientos = allAlojamientos,
                            onChangeEstado = vm::updateAlojamientoEstado,
                            onToggleSlider = vm::toggleAlojamientoSlider
                        )
                    }

                    composable<AdminServicesRoute> {
                        AdminServicesScreen(
                            servicios = servicios,
                            onCreate = vm::createServicio,
                            onUpdate = vm::updateServicio,
                            onDelete = vm::deleteServicio
                        )
                    }

                    composable<AdminClientsRoute> {
                        AdminClientsScreen(
                            clientes = clientes,
                            onCreateClient = { navController.navigate(AdminClientFormRoute(null)) },
                            onEditClient = { id -> navController.navigate(AdminClientFormRoute(id)) },
                            onDeleteClient = vm::deleteClienteAdmin
                        )
                    }

                    composable<AdminClientFormRoute> { entry ->
                        val args = entry.toRoute<AdminClientFormRoute>()
                        AdminClientFormScreen(
                            clienteId = args.clienteId,
                            loadInitial = vm::getClienteForEdit,
                            onSave = vm::saveClienteAdmin,
                            onSaved = { navController.popBackStack() },
                            onCancel = { navController.popBackStack() }
                        )
                    }

                    composable<AdminReservationsRoute> {
                        ReservationsManageScreen(
                            isAdmin = true,
                            reservas = allReservas,
                            onUpdateEstado = vm::updateReservaEstado
                        )
                    }

                    composable<AdminAgendaRoute> {
                        AgendaScreen(
                            isAdmin = true,
                            reservas = allReservas,
                            buildAgenda = vm::buildAgenda
                        )
                    }

                    composable<AdminReviewsRoute> {
                        ReviewsManageScreen(
                            isAdmin = true,
                            valoraciones = allValoraciones,
                            onUpdateComment = vm::updateValoracionComentario,
                            onDeleteReview = vm::deleteValoracion
                        )
                    }

                    composable<AdminSettingsRoute> {
                        AdminSettingsScreen(
                            backgroundImageUrl = backgroundImage,
                            onPickBackground = vm::savePickedBackgroundImage,
                            onResetBackground = vm::resetBackgroundImage
                        )
                    }
                }
            }
        }
    }
}

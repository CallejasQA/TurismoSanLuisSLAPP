package com.example.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
data class SitioDetailRoute(val sitioId: Int)

@Serializable
object LoginRoute

@Serializable
object RegisterAffiliateRoute

@Serializable
object RegisterClientRoute

@Serializable
data class BookSitioRoute(val sitioId: Int)

@Serializable
object ClientReservationsRoute

@Serializable
object OwnerSitesRoute

@Serializable
data class OwnerSiteFormRoute(val sitioId: Int? = null)

@Serializable
object OwnerReservationsRoute

@Serializable
object OwnerAgendaRoute

@Serializable
object OwnerReviewsRoute

@Serializable
object AdminHubRoute

@Serializable
object AdminAffiliatesRoute

@Serializable
object AdminSitesRoute

@Serializable
object AdminServicesRoute

@Serializable
object AdminClientsRoute

@Serializable
data class AdminClientFormRoute(val clienteId: Int? = null)

@Serializable
object AdminReservationsRoute

@Serializable
object AdminAgendaRoute

@Serializable
object AdminReviewsRoute

@Serializable
object AdminSettingsRoute

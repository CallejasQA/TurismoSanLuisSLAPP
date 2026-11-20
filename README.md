# Turismo San Luis (Android)

Aplicación Android nativa (Kotlin + Jetpack Compose + Material 3 + Room) para explorar, reservar, administrar y valorar alojamientos turísticos en San Luis (fincas, glamping, hoteles, cabañas y ecohoteles).

## Características principales

- **Catálogo público:** inicio con carrusel de alojamientos destacados, banner natural con filtros por ubicación, operador y calificación mínima, sección de recomendados y listado de alojamientos activos.
- **Detalle del alojamiento:** galería, ubicación, operador verificado, tarifa por noche, servicios incluidos, reseñas de huéspedes, formulario de calificación para estadías finalizadas y solicitud de reserva.
- **Autenticación y registro:** inicio de sesión con validación de estado de afiliación, registro de afiliados (propietarios) y registro de clientes con validaciones en tiempo real.
- **Panel del propietario:** creación, edición y eliminación de alojamientos con selección de imagen mediante Android Photo Picker, asignación de servicios, agenda mensual de ocupación, gestión de reservas y moderación de comentarios.
- **Panel del administrador:** aprobación/rechazo de afiliaciones, revisión y activación de alojamientos, gestión del carrusel destacado, CRUD del catálogo de servicios, CRUD de clientes, agenda global, gestión de reservas, comentarios e imagen de fondo para pantallas de autenticación.

## Cuentas demo precargadas

La base de datos local Room se inicializa automáticamente con los usuarios de prueba (contraseña: `123456`):

- **Administrador:** `admin@turismosl.com` / `123456`
- **Propietario:** `propietario@turismosl.com` / `123456`
- **Cliente:** `cliente@turismosl.com` / `123456`

## Stack técnico

- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose + Material Design 3 + tipografía local Poppins
- **Navegación:** Navigation Compose (`@Serializable` type-safe routes) con soporte adaptativo (`NavigationBar` / `NavigationRail`)
- **Persistencia:** Room Database + KSP + Coroutines / `StateFlow`
- **Imágenes:** Coil + Android Photo Picker (`PickVisualMedia`) y recursos precargados en `app/src/main/assets/images/`

## Compilación

```bash
gradle assembleDebug
```

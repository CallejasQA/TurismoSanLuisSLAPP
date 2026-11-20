package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.AfiliadoDao
import com.example.data.dao.AlojamientoDao
import com.example.data.dao.ClientePerfilDao
import com.example.data.dao.ReservaDao
import com.example.data.dao.ServicioDao
import com.example.data.dao.SettingDao
import com.example.data.dao.UsuarioDao
import com.example.data.dao.ValoracionDao
import com.example.data.model.AfiliadoEntity
import com.example.data.model.AlojamientoEntity
import com.example.data.model.AlojamientoServicioCrossRef
import com.example.data.model.ClientePerfilEntity
import com.example.data.model.ReservaEntity
import com.example.data.model.ServicioEntity
import com.example.data.model.SettingEntity
import com.example.data.model.UsuarioEntity
import com.example.data.model.ValoracionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Database(
    entities = [
        UsuarioEntity::class,
        ClientePerfilEntity::class,
        AfiliadoEntity::class,
        AlojamientoEntity::class,
        ServicioEntity::class,
        AlojamientoServicioCrossRef::class,
        ReservaEntity::class,
        ValoracionEntity::class,
        SettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class TurismoDatabase : RoomDatabase() {
    abstract fun usuarioDao(): UsuarioDao
    abstract fun clientePerfilDao(): ClientePerfilDao
    abstract fun afiliadoDao(): AfiliadoDao
    abstract fun alojamientoDao(): AlojamientoDao
    abstract fun servicioDao(): ServicioDao
    abstract fun reservaDao(): ReservaDao
    abstract fun valoracionDao(): ValoracionDao
    abstract fun settingDao(): SettingDao

    companion object {
        const val KEY_BACKGROUND_IMAGE = "background_image"
        const val DEFAULT_BACKGROUND_IMAGE = "file:///android_asset/images/background-1764091565.jpg"
        const val FALLBACK_BACKGROUND_IMAGE = "file:///android_asset/images/default-bg.jpg"

        val BASE_SERVICES = listOf(
            "WiFi", "Parqueadero", "Piscina", "Cocina", "Baño privado", "Agua caliente",
            "Televisión", "Aire acondicionado", "Ventilador", "Desayuno incluido",
            "Recepción 24 horas", "Zona BBQ", "Jacuzzi", "Sauna", "Restaurante",
            "Bar", "Terraza", "Jardín", "Servicio de transporte",
            "Permitido fumar (zonas designadas)", "Zona de juegos infantiles",
            "Senderos ecológicos", "Actividades guiadas", "Hamacas",
            "Zona de camping", "Mascotas permitidas (con/sin costo)", "Cocina compartida"
        )

        fun sha256(input: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
            return bytes.joinToString("") { "%02x".format(it) }
        }

        @Volatile
        private var INSTANCE: TurismoDatabase? = null

        fun getInstance(context: Context): TurismoDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): TurismoDatabase {
            lateinit var db: TurismoDatabase
            db = Room.databaseBuilder(
                context,
                TurismoDatabase::class.java,
                "turismo_sanluis.db"
            )
                .addCallback(object : Callback() {
                    override fun onCreate(sqliteDb: SupportSQLiteDatabase) {
                        super.onCreate(sqliteDb)
                        CoroutineScope(Dispatchers.IO).launch {
                            seedInitialData(db)
                        }
                    }
                })
                .build()
            return db
        }

        suspend fun ensureSeeded(db: TurismoDatabase) {
            if (db.usuarioDao().count() == 0) {
                seedInitialData(db)
            }
        }

        private suspend fun seedInitialData(db: TurismoDatabase) {
            if (db.usuarioDao().count() > 0) return

            val today = LocalDate.now()
            val fmt = DateTimeFormatter.ISO_LOCAL_DATE
            val nowTs = "${today.format(fmt)} 09:00:00"
            val demoPassHash = sha256("123456")

            // 1. Users (exact demo users from database_schema.sql + README.md)
            val adminId = db.usuarioDao().insert(
                UsuarioEntity(
                    nombre = "Administrador",
                    email = "admin@turismosl.com",
                    passwordHash = demoPassHash,
                    rol = "admin",
                    estado = "activo",
                    creadoEn = nowTs
                )
            ).toInt()

            val prop1Id = db.usuarioDao().insert(
                UsuarioEntity(
                    nombre = "Propietario Demo",
                    email = "propietario@turismosl.com",
                    passwordHash = demoPassHash,
                    rol = "propietario",
                    estado = "activo",
                    creadoEn = nowTs
                )
            ).toInt()

            val clienteId = db.usuarioDao().insert(
                UsuarioEntity(
                    nombre = "Cliente Demo",
                    email = "cliente@turismosl.com",
                    passwordHash = demoPassHash,
                    rol = "cliente",
                    estado = "activo",
                    creadoEn = nowTs
                )
            ).toInt()

            val prop2Id = db.usuarioDao().insert(
                UsuarioEntity(
                    nombre = "Reserva La Montañera",
                    email = "montanera@turismosl.com",
                    passwordHash = demoPassHash,
                    rol = "propietario",
                    estado = "activo",
                    creadoEn = nowTs
                )
            ).toInt()

            val propPendingId = db.usuarioDao().insert(
                UsuarioEntity(
                    nombre = "Glamping Samaná",
                    email = "glampingsamana@turismosl.com",
                    passwordHash = demoPassHash,
                    rol = "propietario",
                    estado = "activo",
                    creadoEn = nowTs
                )
            ).toInt()

            // Suppress unused warning on adminId
            require(adminId > 0)

            // 2. Client profile
            db.clientePerfilDao().insert(
                ClientePerfilEntity(
                    usuarioId = clienteId,
                    primerNombre = "Cliente",
                    segundoNombre = "Demo",
                    primerApellido = "Turismo",
                    cedula = "123456789",
                    telefonoCodigo = "+57",
                    telefonoNumero = "3001234567",
                    municipioOrigen = "San Luis",
                    creadoEn = nowTs
                )
            )

            // 3. Affiliates (2 approved, 1 pending for Admin review)
            db.afiliadoDao().insert(
                AfiliadoEntity(
                    usuarioId = prop1Id,
                    nombreNegocio = "EcoNativos San Luis",
                    tipo = "Ecohotel",
                    descripcion = "Cabañas ecológicas y refugios junto al río Dormilón y senderos naturales.",
                    direccion = "Vereda La Cristalina, San Luis, Antioquia",
                    estado = "aprobado",
                    solicitadoEn = nowTs
                )
            )
            db.afiliadoDao().insert(
                AfiliadoEntity(
                    usuarioId = prop2Id,
                    nombreNegocio = "Reserva La Montañera",
                    tipo = "Finca",
                    descripcion = "Finca cafetera tradicional y miradores de bosque húmedo tropical.",
                    direccion = "Vereda Manizales, San Luis, Antioquia",
                    estado = "aprobado",
                    solicitadoEn = nowTs
                )
            )
            db.afiliadoDao().insert(
                AfiliadoEntity(
                    usuarioId = propPendingId,
                    nombreNegocio = "Glamping Samaná",
                    tipo = "Glamping",
                    descripcion = "Domos panorámicos frente al cañón del río Samaná Norte.",
                    direccion = "Sector Puente Samaná, San Luis",
                    estado = "pendiente",
                    solicitadoEn = nowTs
                )
            )

            // 4. Services catalog (all 27 services)
            val serviceIdByName = mutableMapOf<String, Int>()
            BASE_SERVICES.forEach { name ->
                val id = db.servicioDao().insert(
                    ServicioEntity(nombre = name, creadoEn = nowTs)
                ).toInt()
                if (id > 0) serviceIdByName[name] = id
            }

            // 5. Accommodations using bundled photos from public/storage/subidas/
            val sitio1Id = db.alojamientoDao().insert(
                AlojamientoEntity(
                    propietarioId = prop1Id,
                    nombre = "Cabaña Nativos del Bosque",
                    descripcion = "Acogedora cabaña de madera inmersa en el bosque húmedo de San Luis, con acceso directo a quebradas cristalinas, terraza con hamacas y desayuno campesino.",
                    ubicacion = "Vereda La Cristalina, San Luis",
                    precioNoche = 220000.0,
                    rangoPrecio = "$200.000 - $260.000",
                    imagen = "file:///android_asset/images/1764297034_nativos-1.jpg",
                    destacadoSlider = true,
                    estado = "activo",
                    creadoEn = nowTs
                )
            ).toInt()

            val sitio2Id = db.alojamientoDao().insert(
                AlojamientoEntity(
                    propietarioId = prop1Id,
                    nombre = "Suite Casona del Río",
                    descripcion = "Suite amplia con balcón privado sobre el valle, baño con agua caliente, decoración artesanal y rutas guiadas de avistamiento de aves.",
                    ubicacion = "Sector Río Dormilón, San Luis",
                    precioNoche = 285000.0,
                    rangoPrecio = "$260.000 - $320.000",
                    imagen = "file:///android_asset/images/1764284256_suit-casona.jpg",
                    destacadoSlider = true,
                    estado = "activo",
                    creadoEn = nowTs
                )
            ).toInt()

            val sitio3Id = db.alojamientoDao().insert(
                AlojamientoEntity(
                    propietarioId = prop2Id,
                    nombre = "Casona Alpina La Montañera",
                    descripcion = "Arquitectura alpina rodeada de montañas verdes, ideal para familias y grupos que buscan tranquilidad, zona BBQ y fogata nocturna.",
                    ubicacion = "Vereda Manizales, San Luis",
                    precioNoche = 340000.0,
                    rangoPrecio = "$300.000 - $400.000",
                    imagen = "file:///android_asset/images/1764288674_casona-alpina.jpg",
                    destacadoSlider = true,
                    estado = "aprobado",
                    creadoEn = nowTs
                )
            ).toInt()

            val sitio4Id = db.alojamientoDao().insert(
                AlojamientoEntity(
                    propietarioId = prop1Id,
                    nombre = "Cabaña Mirador Samaná",
                    descripcion = "Refugio frente a las aguas turquesa del río Samaná, perfecto para desconectarse y disfrutar de senderos ecológicos.",
                    ubicacion = "Cañón del Samaná, San Luis",
                    precioNoche = 195000.0,
                    rangoPrecio = "$180.000 - $220.000",
                    imagen = "file:///android_asset/images/1763917708_rio-samana-san-luis-antioquia.jpg",
                    destacadoSlider = false,
                    estado = "activo",
                    creadoEn = nowTs
                )
            ).toInt()

            val sitio5Id = db.alojamientoDao().insert(
                AlojamientoEntity(
                    propietarioId = prop2Id,
                    nombre = "Cabaña Ecológica El Sendero",
                    descripcion = "Cabaña rústica rodeada de palmas y guaduales con hamacas al aire libre, cocina equipada y parqueadero privado.",
                    ubicacion = "Corregimiento El Prodigio, San Luis",
                    precioNoche = 175000.0,
                    rangoPrecio = "$160.000 - $200.000",
                    imagen = "file:///android_asset/images/1764281239_cabana-4.jpg",
                    destacadoSlider = false,
                    estado = "activo",
                    creadoEn = nowTs
                )
            ).toInt()

            val sitio6Id = db.alojamientoDao().insert(
                AlojamientoEntity(
                    propietarioId = prop1Id,
                    nombre = "Refugio La Montañera",
                    descripcion = "Nuevo refugio de montaña con jacuzzi exterior y vista panorámica al bosque. Pendiente de revisión final.",
                    ubicacion = "Alto de San Luis",
                    precioNoche = 310000.0,
                    rangoPrecio = "$290.000 - $350.000",
                    imagen = "file:///android_asset/images/1764268342_monta-era.jpg",
                    destacadoSlider = false,
                    estado = "pendiente",
                    creadoEn = nowTs
                )
            ).toInt()

            // Attach services to accommodations
            fun linkServices(alojamientoId: Int, names: List<String>): List<AlojamientoServicioCrossRef> {
                return names.mapNotNull { serviceIdByName[it] }
                    .map { AlojamientoServicioCrossRef(alojamientoId, it) }
            }

            db.servicioDao().insertCrossRefs(
                linkServices(sitio1Id, listOf("WiFi", "Parqueadero", "Desayuno incluido", "Senderos ecológicos", "Hamacas", "Agua caliente", "Terraza")) +
                    linkServices(sitio2Id, listOf("WiFi", "Baño privado", "Agua caliente", "Desayuno incluido", "Jacuzzi", "Restaurante", "Actividades guiadas")) +
                    linkServices(sitio3Id, listOf("WiFi", "Parqueadero", "Cocina", "Zona BBQ", "Jardín", "Terraza", "Mascotas permitidas (con/sin costo)")) +
                    linkServices(sitio4Id, listOf("Senderos ecológicos", "Actividades guiadas", "Hamacas", "Zona de camping", "Baño privado")) +
                    linkServices(sitio5Id, listOf("Parqueadero", "Cocina", "Ventilador", "Hamacas", "Jardín")) +
                    linkServices(sitio6Id, listOf("WiFi", "Jacuzzi", "Terraza", "Agua caliente"))
            )

            // 6. Seed Reservations (for current month so Agenda & Client Reviews work immediately)
            val monthStart = today.withDayOfMonth(1)
            val res1Inicio = monthStart.plusDays(2).format(fmt)
            val res1Fin = monthStart.plusDays(4).format(fmt)
            val res1Id = db.reservaDao().insert(
                ReservaEntity(
                    alojamientoId = sitio1Id,
                    clienteId = clienteId,
                    fechaInicio = res1Inicio,
                    fechaFin = res1Fin,
                    total = 440000.0,
                    estado = "finalizada",
                    creadoEn = nowTs
                )
            ).toInt()

            val res2Inicio = monthStart.plusDays(6).format(fmt)
            val res2Fin = monthStart.plusDays(8).format(fmt)
            db.reservaDao().insert(
                ReservaEntity(
                    alojamientoId = sitio2Id,
                    clienteId = clienteId,
                    fechaInicio = res2Inicio,
                    fechaFin = res2Fin,
                    total = 570000.0,
                    estado = "finalizada",
                    creadoEn = nowTs
                )
            )

            val res3Inicio = today.plusDays(1).format(fmt)
            val res3Fin = today.plusDays(3).format(fmt)
            db.reservaDao().insert(
                ReservaEntity(
                    alojamientoId = sitio3Id,
                    clienteId = clienteId,
                    fechaInicio = res3Inicio,
                    fechaFin = res3Fin,
                    total = 680000.0,
                    estado = "confirmada",
                    creadoEn = nowTs
                )
            )

            val res4Inicio = today.plusDays(5).format(fmt)
            val res4Fin = today.plusDays(7).format(fmt)
            db.reservaDao().insert(
                ReservaEntity(
                    alojamientoId = sitio4Id,
                    clienteId = clienteId,
                    fechaInicio = res4Inicio,
                    fechaFin = res4Fin,
                    total = 390000.0,
                    estado = "pendiente",
                    creadoEn = nowTs
                )
            )

            // 7. Seed Valoraciones (1 review on sitio1; sitio2 remains finalizada and unrated so cliente@turismosl.com can immediately test submitting a review!)
            db.valoracionDao().insert(
                ValoracionEntity(
                    reservaId = res1Id,
                    alojamientoId = sitio1Id,
                    clienteId = clienteId,
                    estrellas = 5,
                    comentario = "Un lugar mágico rodeado de naturaleza, el sonido del río y la atención de los anfitriones fueron inolvidables.",
                    creadoEn = nowTs
                )
            )

            // 8. Settings (auth background image from settings.json)
            db.settingDao().upsert(
                SettingEntity(
                    key = KEY_BACKGROUND_IMAGE,
                    value = DEFAULT_BACKGROUND_IMAGE
                )
            )
        }
    }
}

package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AssistChip
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.AuthResult
import com.example.data.validation.Validators
import com.example.ui.theme.PillDangerBg
import com.example.ui.theme.PillDangerFg
import com.example.ui.theme.PillSuccessBg
import com.example.ui.theme.PillSuccessFg
import com.example.ui.theme.VerdeAuthPrimary
import com.example.ui.theme.VerdeProfundo

@Composable
fun AuthBackgroundShell(
    backgroundImageUrl: String,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(backgroundImageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Fondo de autenticación",
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize()
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0x8C1A5E2A),
                            Color(0xD9FFFFFF)
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 540.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                border = BorderStroke(1.dp, VerdeAuthPrimary.copy(alpha = 0.1f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun ValidationHint(validation: Validators.FieldValidation?) {
    if (validation == null) return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
    ) {
        Icon(
            imageVector = if (validation.valid) Icons.Default.CheckCircle else Icons.Default.Error,
            contentDescription = null,
            tint = if (validation.valid) PillSuccessFg else PillDangerFg,
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = validation.message,
            style = MaterialTheme.typography.bodySmall,
            color = if (validation.valid) PillSuccessFg else PillDangerFg
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    backgroundImageUrl: String,
    onLogin: (String, String, (AuthResult) -> Unit) -> Unit,
    onLoginSuccess: (String) -> Unit,
    onNavigateAffiliate: () -> Unit,
    onNavigateRegisterClient: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var emailTouched by remember { mutableStateOf(false) }
    var passwordTouched by remember { mutableStateOf(false) }
    var serverError by remember { mutableStateOf<String?>(null) }

    val emailValidation = if (emailTouched) Validators.validateEmail(email) else null
    val passwordValidation = if (passwordTouched) Validators.validatePassword(password) else null

    AuthBackgroundShell(backgroundImageUrl = backgroundImageUrl) {
        Text(
            text = "Iniciar sesión",
            style = MaterialTheme.typography.headlineMedium,
            color = VerdeProfundo,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Accede para gestionar tus reservas o alojamientos.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        serverError?.let { err ->
            Surface(
                color = PillDangerBg,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, PillDangerFg.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = err,
                    color = PillDangerFg,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Column {
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it.replace(Regex("\\s+"), "")
                    emailTouched = true
                },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                isError = emailValidation?.valid == false,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_email_input")
            )
            ValidationHint(emailValidation)
        }

        Column {
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordTouched = true
                },
                label = { Text("Contraseña") },
                singleLine = true,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Mostrar contraseña"
                        )
                    }
                },
                isError = passwordValidation?.valid == false,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_password_input")
            )
            ValidationHint(passwordValidation)
        }

        Button(
            onClick = {
                emailTouched = true
                passwordTouched = true
                serverError = null
                onLogin(email, password) { result ->
                    when (result) {
                        is AuthResult.Success -> onLoginSuccess(result.usuario.rol)
                        is AuthResult.Error -> serverError = result.message
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("login_submit_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VerdeAuthPrimary)
        ) {
            Text("Ingresar", fontWeight = FontWeight.Bold)
        }

        // Demo quick-fill buttons matching README.md demo accounts
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Cuentas demo (contraseña: 123456):",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = {
                        email = "cliente@turismosl.com"
                        password = "123456"
                        serverError = null
                    },
                    label = { Text("Cliente Demo") },
                    modifier = Modifier.testTag("demo_fill_client")
                )
                AssistChip(
                    onClick = {
                        email = "propietario@turismosl.com"
                        password = "123456"
                        serverError = null
                    },
                    label = { Text("Propietario Demo") },
                    modifier = Modifier.testTag("demo_fill_owner")
                )
                AssistChip(
                    onClick = {
                        email = "admin@turismosl.com"
                        password = "123456"
                        serverError = null
                    },
                    label = { Text("Admin Demo") },
                    modifier = Modifier.testTag("demo_fill_admin")
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("¿Eres propietario?", style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onNavigateAffiliate) {
                    Text("Afíliate", color = VerdeAuthPrimary, fontWeight = FontWeight.Bold)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("¿Aún no tienes cuenta?", style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onNavigateRegisterClient) {
                    Text("Regístrate como cliente", color = VerdeAuthPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterAffiliateScreen(
    backgroundImageUrl: String,
    onRegister: (
        nombreNegocio: String,
        tipo: String,
        direccion: String,
        descripcion: String,
        email: String,
        password: String,
        onResult: (Result<String>) -> Unit
    ) -> Unit,
    onNavigateLogin: () -> Unit
) {
    var nombreNegocio by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("") }
    var direccion by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var tipoMenuOpen by remember { mutableStateOf(false) }

    var feedbackMsg by remember { mutableStateOf<String?>(null) }
    var feedbackOk by remember { mutableStateOf(false) }

    AuthBackgroundShell(backgroundImageUrl = backgroundImageUrl) {
        Text(
            text = "Afíliate",
            style = MaterialTheme.typography.headlineMedium,
            color = VerdeProfundo,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Registra tu alojamiento y llega a más viajeros.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        feedbackMsg?.let { msg ->
            Surface(
                color = if (feedbackOk) PillSuccessBg else PillDangerBg,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = msg,
                    color = if (feedbackOk) PillSuccessFg else PillDangerFg,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        OutlinedTextField(
            value = nombreNegocio,
            onValueChange = { nombreNegocio = it.take(30) },
            label = { Text("Nombre del negocio *") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("aff_nombre_input")
        )

        ExposedDropdownMenuBox(
            expanded = tipoMenuOpen,
            onExpandedChange = { tipoMenuOpen = !tipoMenuOpen }
        ) {
            OutlinedTextField(
                value = tipo.ifBlank { "Seleccione..." },
                onValueChange = {},
                readOnly = true,
                label = { Text("Tipo *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tipoMenuOpen) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .testTag("aff_tipo_dropdown")
            )
            ExposedDropdownMenu(
                expanded = tipoMenuOpen,
                onDismissRequest = { tipoMenuOpen = false }
            ) {
                Validators.TIPOS_ALOJAMIENTO.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            tipo = option
                            tipoMenuOpen = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = direccion,
            onValueChange = { direccion = it.take(80) },
            label = { Text("Dirección *") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("aff_direccion_input")
        )

        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it.take(300) },
            label = { Text("Descripción *") },
            minLines = 3,
            maxLines = 5,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("aff_descripcion_input")
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it.replace(Regex("\\s+"), "").take(70) },
            label = { Text("Email *") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("aff_email_input")
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it.take(20) },
            label = { Text("Contraseña *") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("aff_password_input")
        )

        Button(
            onClick = {
                onRegister(nombreNegocio, tipo, direccion, descripcion, email, password) { res ->
                    if (res.isSuccess) {
                        feedbackOk = true
                        feedbackMsg = res.getOrNull()
                    } else {
                        feedbackOk = false
                        feedbackMsg = res.exceptionOrNull()?.message
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("aff_submit_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VerdeAuthPrimary)
        ) {
            Text("Enviar solicitud", fontWeight = FontWeight.Bold)
        }

        TextButton(onClick = onNavigateLogin) {
            Text("¿Ya tienes cuenta? Iniciar sesión", color = VerdeAuthPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RegisterClientScreen(
    backgroundImageUrl: String,
    onRegister: (
        primerNombre: String,
        segundoNombre: String,
        primerApellido: String,
        cedula: String,
        telefonoCodigo: String,
        telefonoNumero: String,
        email: String,
        municipioOrigen: String,
        password: String,
        onResult: (Result<String>) -> Unit
    ) -> Unit,
    onNavigateLogin: () -> Unit
) {
    var primerNombre by remember { mutableStateOf("") }
    var segundoNombre by remember { mutableStateOf("") }
    var primerApellido by remember { mutableStateOf("") }
    var cedula by remember { mutableStateOf("") }
    var telefonoCodigo by remember { mutableStateOf("+57") }
    var telefonoNumero by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var municipioOrigen by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var feedbackMsg by remember { mutableStateOf<String?>(null) }
    var feedbackOk by remember { mutableStateOf(false) }

    AuthBackgroundShell(backgroundImageUrl = backgroundImageUrl) {
        Text(
            text = "Regístrate",
            style = MaterialTheme.typography.headlineMedium,
            color = VerdeProfundo,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Crea tu cuenta para descubrir y reservar experiencias únicas.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        feedbackMsg?.let { msg ->
            Surface(
                color = if (feedbackOk) PillSuccessBg else PillDangerBg,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = msg,
                    color = if (feedbackOk) PillSuccessFg else PillDangerFg,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        OutlinedTextField(
            value = primerNombre,
            onValueChange = { primerNombre = it.replace(Regex("[0-9]"), "").take(30) },
            label = { Text("Primer nombre *") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("cli_primer_nombre_input")
        )
        OutlinedTextField(
            value = segundoNombre,
            onValueChange = { segundoNombre = it.replace(Regex("[0-9]"), "").take(30) },
            label = { Text("Segundo nombre") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = primerApellido,
            onValueChange = { primerApellido = it.replace(Regex("[0-9]"), "").take(30) },
            label = { Text("Primer apellido *") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("cli_primer_apellido_input")
        )
        OutlinedTextField(
            value = cedula,
            onValueChange = { cedula = it.filter(Char::isDigit).take(20) },
            label = { Text("Cédula *") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("cli_cedula_input")
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = telefonoCodigo,
                onValueChange = { raw ->
                    val hasPlus = raw.startsWith("+")
                    val digits = raw.filter(Char::isDigit).take(5)
                    telefonoCodigo = (if (hasPlus) "+" else "") + digits
                },
                label = { Text("Cód.") },
                singleLine = true,
                modifier = Modifier.width(100.dp)
            )
            OutlinedTextField(
                value = telefonoNumero,
                onValueChange = { telefonoNumero = it.filter(Char::isDigit).take(20) },
                label = { Text("Celular *") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .weight(1f)
                    .testTag("cli_telefono_input")
            )
        }
        OutlinedTextField(
            value = email,
            onValueChange = { email = it.replace(Regex("\\s+"), "").take(70) },
            label = { Text("Correo electrónico *") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("cli_email_input")
        )
        OutlinedTextField(
            value = municipioOrigen,
            onValueChange = { municipioOrigen = it.take(100) },
            label = { Text("Municipio de origen") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it.take(20) },
            label = { Text("Contraseña *") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("cli_password_input")
        )

        Button(
            onClick = {
                onRegister(
                    primerNombre, segundoNombre, primerApellido, cedula,
                    telefonoCodigo, telefonoNumero, email, municipioOrigen, password
                ) { res ->
                    if (res.isSuccess) {
                        feedbackOk = true
                        feedbackMsg = res.getOrNull()
                    } else {
                        feedbackOk = false
                        feedbackMsg = res.exceptionOrNull()?.message
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("cli_submit_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = VerdeAuthPrimary)
        ) {
            Text("Registrarse", fontWeight = FontWeight.Bold)
        }

        TextButton(onClick = onNavigateLogin) {
            Text("¿Ya tienes cuenta? Iniciar sesión", color = VerdeAuthPrimary, fontWeight = FontWeight.Bold)
        }
    }
}

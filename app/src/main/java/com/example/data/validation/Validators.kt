package com.example.data.validation

object Validators {
    private val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    private val NAME_REGEX = Regex("^[\\p{L}ñÑáéíóúÁÉÍÓÚ'\\s-]+$")
    private val MUNICIPIO_REGEX = Regex("^[\\p{L}ñÑáéíóúÁÉÍÓÚ\\s.'-]+$")
    private val PHONE_CODE_REGEX = Regex("^\\+?\\d{1,5}$")
    private val DIGITS_REGEX = Regex("^\\d+$")

    val TIPOS_ALOJAMIENTO = listOf("Finca", "Glamping", "Hotel", "Cabaña", "Ecohotel")

    data class FieldValidation(val valid: Boolean, val message: String)

    fun validateEmail(raw: String): FieldValidation {
        val value = raw.replace(Regex("\\s+"), "")
        if (value.isEmpty()) return FieldValidation(false, "El email es obligatorio.")
        if (raw.any { it.isWhitespace() }) return FieldValidation(false, "El formato del correo no es válido.")
        if (value.length < 6 || value.length > 70) {
            return FieldValidation(false, "El correo debe tener entre 6 y 70 caracteres.")
        }
        if (!EMAIL_REGEX.matches(value)) return FieldValidation(false, "El formato del correo no es válido.")
        return FieldValidation(true, "Correo válido.")
    }

    fun validatePassword(raw: String, isRequired: Boolean = true, maxLen: Int = 20): FieldValidation {
        if (raw.isEmpty()) {
            return if (isRequired) FieldValidation(false, "La contraseña es obligatoria.")
            else FieldValidation(true, "Se conservará la contraseña actual.")
        }
        if (raw.length < 6 || raw.length > maxLen) {
            return FieldValidation(false, "Debe tener entre 6 y $maxLen caracteres.")
        }
        return FieldValidation(true, "Contraseña válida.")
    }

    fun validateNombreNegocio(raw: String): FieldValidation {
        val value = raw.trim()
        if (value.length < 3 || value.length > 30) {
            return FieldValidation(false, "El nombre del negocio debe tener entre 3 y 30 caracteres.")
        }
        return FieldValidation(true, "Nombre válido.")
    }

    fun validateTipoNegocio(raw: String): FieldValidation {
        val value = raw.trim()
        if (value.isEmpty()) return FieldValidation(false, "Debe seleccionar un tipo de alojamiento.")
        if (value !in TIPOS_ALOJAMIENTO) return FieldValidation(false, "El tipo de alojamiento seleccionado no es válido.")
        return FieldValidation(true, "Tipo seleccionado.")
    }

    fun validateDireccion(raw: String): FieldValidation {
        val value = raw.trim()
        if (value.length < 3 || value.length > 80) {
            return FieldValidation(false, "La dirección debe tener entre 3 y 80 caracteres.")
        }
        return FieldValidation(true, "Dirección válida.")
    }

    fun validateDescripcionNegocio(raw: String): FieldValidation {
        val value = raw.trim()
        if (value.length < 3 || value.length > 300) {
            return FieldValidation(false, "La descripción debe tener entre 3 y 300 caracteres.")
        }
        return FieldValidation(true, "Descripción válida.")
    }

    fun validateNombrePersona(raw: String, etiqueta: String, minLen: Int = 3, maxLen: Int = 30, optional: Boolean = false): FieldValidation {
        val value = raw.trim()
        if (value.isEmpty()) {
            return if (optional) FieldValidation(true, "Campo opcional.")
            else FieldValidation(false, "$etiqueta es obligatorio.")
        }
        if (value.length < minLen || value.length > maxLen) {
            return FieldValidation(false, "$etiqueta debe tener entre $minLen y $maxLen caracteres.")
        }
        if (!NAME_REGEX.matches(value)) {
            return FieldValidation(false, "Solo se permiten letras.")
        }
        return FieldValidation(true, "Válido.")
    }

    fun validateCedula(raw: String, required: Boolean = true): FieldValidation {
        val value = raw.trim()
        if (value.isEmpty()) {
            return if (required) FieldValidation(false, "La cédula es obligatoria.")
            else FieldValidation(true, "Campo opcional.")
        }
        if (!DIGITS_REGEX.matches(value)) return FieldValidation(false, "La cédula solo puede contener números.")
        if (value.length < 6 || value.length > 20) {
            return FieldValidation(false, "La cédula debe tener entre 6 y 20 caracteres.")
        }
        return FieldValidation(true, "Cédula válida.")
    }

    fun validateTelefonoCodigo(raw: String): FieldValidation {
        val value = raw.trim()
        if (value.isEmpty()) return FieldValidation(false, "El código es obligatorio.")
        if (!PHONE_CODE_REGEX.matches(value)) return FieldValidation(false, "Ingresa un código válido (ej. +57).")
        return FieldValidation(true, "Código válido.")
    }

    fun validateTelefonoNumero(raw: String): FieldValidation {
        val value = raw.trim()
        if (value.isEmpty()) return FieldValidation(false, "El número de celular es obligatorio.")
        if (!DIGITS_REGEX.matches(value)) return FieldValidation(false, "El número de celular solo puede contener números.")
        if (value.length < 7 || value.length > 20) {
            return FieldValidation(false, "El número de celular debe tener entre 7 y 20 caracteres.")
        }
        return FieldValidation(true, "Número válido.")
    }

    fun validateMunicipio(raw: String): FieldValidation {
        val value = raw.trim()
        if (value.isEmpty()) return FieldValidation(true, "Campo opcional.")
        if (value.length < 3 || value.length > 100) {
            return FieldValidation(false, "El municipio debe tener entre 3 y 100 caracteres.")
        }
        if (!MUNICIPIO_REGEX.matches(value)) {
            return FieldValidation(false, "Solo se permiten letras y separadores.")
        }
        return FieldValidation(true, "Municipio válido.")
    }
}

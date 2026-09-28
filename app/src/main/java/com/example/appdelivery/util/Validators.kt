package com.example.appdelivery.util

import android.util.Patterns

object Validators {
    fun name(v: String): String? =
        if (v.trim().length < 3) "Ingresa tu nombre completo (mínimo 3 letras)" else null

    fun email(v: String): String? =
        if (!Patterns.EMAIL_ADDRESS.matcher(v.trim()).matches()) "Correo electrónico no válido" else null

    fun phone(v: String): String? =
        if (!v.trim().matches(Regex("\\d{10}"))) "El teléfono debe tener 10 dígitos" else null

    fun password(v: String): String? =
        if (v.length < 6) "La contraseña debe tener al menos 6 caracteres" else null

    fun addressLabel(v: String): String? =
        if (v.trim().length < 2) "Ingresa un nombre (ej. Casa, Trabajo)" else null

    fun street(v: String): String? =
        if (v.trim().length < 5) "Ingresa la calle y número (mínimo 5 caracteres)" else null

    fun city(v: String): String? =
        if (v.trim().length < 2) "Ingresa la ciudad" else null
}

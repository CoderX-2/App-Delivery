package com.example.appdelivery.data.repository

import com.example.appdelivery.data.dao.CustomerDao
import com.example.appdelivery.data.entity.Customer
import com.example.appdelivery.util.PasswordHasher

class AuthRepository(private val dao: CustomerDao) {

    suspend fun register(name: String, email: String, phone: String, password: String): Result<Long> {
        val cleanEmail = email.trim().lowercase()
        if (dao.getByEmail(cleanEmail) != null) {
            return Result.failure(Exception("Este correo ya está registrado"))
        }
        val salt = PasswordHasher.generateSalt()
        val customer = Customer(
            name = name.trim(),
            email = cleanEmail,
            phone = phone.trim(),
            passwordHash = PasswordHasher.hash(password, salt),
            salt = salt
        )
        return try {
            Result.success(dao.insert(customer))
        } catch (e: Exception) {
            Result.failure(Exception("No se pudo registrar. Intenta de nuevo"))
        }
    }

    suspend fun login(email: String, password: String): Result<Long> {
        val customer = dao.getByEmail(email.trim().lowercase())
        return if (customer != null && PasswordHasher.verify(password, customer.salt, customer.passwordHash)) {
            Result.success(customer.id)
        } else {
            Result.failure(Exception("Correo o contraseña incorrectos"))
        }
    }
}

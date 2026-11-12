package com.example.appdelivery.data.courier

import com.example.appdelivery.data.dao.CourierDao
import com.example.appdelivery.data.entity.Courier
import com.example.appdelivery.data.entity.VehicleType
import com.example.appdelivery.util.PasswordHasher
import kotlinx.coroutines.flow.Flow

interface CourierRepository {
    suspend fun register(
        name: String, email: String, phone: String,
        vehicleType: VehicleType, password: String
    ): Result<Long>

    suspend fun login(email: String, password: String): Result<Courier>
    fun observeCourier(id: Long): Flow<Courier?>
    suspend fun getCourier(id: Long): Courier?
    suspend fun setAvailability(id: Long, isAvailable: Boolean)
}

class CourierRepositoryImpl(private val dao: CourierDao) : CourierRepository {

    override suspend fun register(
        name: String, email: String, phone: String,
        vehicleType: VehicleType, password: String
    ): Result<Long> {
        val cleanEmail = email.trim().lowercase()
        if (dao.findByEmail(cleanEmail) != null) {
            return Result.failure(IllegalArgumentException("El correo ya está registrado"))
        }
        val salt = PasswordHasher.generateSalt()
        val hash = PasswordHasher.hash(password, salt)
        val courier = Courier(
            name = name.trim(),
            email = cleanEmail,
            phone = phone.trim(),
            vehicleType = vehicleType,
            isAvailable = true,
            passwordHash = hash,
            salt = salt
        )
        val id = dao.insert(courier)
        return Result.success(id)
    }

    override suspend fun login(email: String, password: String): Result<Courier> {
        val courier = dao.findByEmail(email.trim().lowercase())
            ?: return Result.failure(IllegalArgumentException("Credenciales incorrectas"))
        val hash = PasswordHasher.hash(password, courier.salt)
        return if (hash == courier.passwordHash) {
            Result.success(courier)
        } else {
            Result.failure(IllegalArgumentException("Credenciales incorrectas"))
        }
    }

    override fun observeCourier(id: Long): Flow<Courier?> = dao.observeById(id)
    override suspend fun getCourier(id: Long): Courier? = dao.getById(id)
    override suspend fun setAvailability(id: Long, isAvailable: Boolean) = dao.setAvailability(id, isAvailable)
}

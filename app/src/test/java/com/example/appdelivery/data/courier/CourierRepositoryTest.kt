package com.example.appdelivery.data.courier

import com.example.appdelivery.data.dao.CourierDao
import com.example.appdelivery.data.entity.Courier
import com.example.appdelivery.data.entity.VehicleType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeCourierDao : CourierDao {
    private val couriers = mutableListOf<Courier>()
    private var nextId = 1L
    private val state = MutableStateFlow<Courier?>(null)

    override suspend fun insert(courier: Courier): Long {
        val created = courier.copy(id = nextId++)
        couriers.add(created)
        state.value = created
        return created.id
    }

    override suspend fun findByEmail(email: String): Courier? =
        couriers.firstOrNull { it.email.equals(email, ignoreCase = true) }

    override suspend fun getById(id: Long): Courier? =
        couriers.firstOrNull { it.id == id }

    override fun observeById(id: Long): Flow<Courier?> = state

    override suspend fun setAvailability(id: Long, isAvailable: Boolean) {
        val idx = couriers.indexOfFirst { it.id == id }
        if (idx != -1) {
            val updated = couriers[idx].copy(isAvailable = isAvailable)
            couriers[idx] = updated
            state.value = updated
        }
    }
}

class CourierRepositoryTest {
    private val dao = FakeCourierDao()
    private val repo: CourierRepository = CourierRepositoryImpl(dao)

    @Test
    fun registrarRepartidor_exitoso() = runTest {
        val res = repo.register(
            name = "Carlos Repartidor",
            email = "carlos@delivery.com",
            phone = "5511223344",
            vehicleType = VehicleType.MOTORCYCLE,
            password = "password123"
        )
        assertTrue(res.isSuccess)
        val id = res.getOrNull()
        assertNotNull(id)

        val courier = dao.getById(id!!)
        assertNotNull(courier)
        assertEquals("Carlos Repartidor", courier!!.name)
        assertEquals(VehicleType.MOTORCYCLE, courier.vehicleType)
        assertTrue(courier.isAvailable)
    }

    @Test
    fun registrarRepartidor_correoDuplicado_falla() = runTest {
        repo.register("A", "duplicado@test.com", "111", VehicleType.BICYCLE, "123456")
        val res2 = repo.register("B", "duplicado@test.com", "222", VehicleType.CAR, "123456")
        assertTrue(res2.isFailure)
    }

    @Test
    fun loginRepartidor_credencialesCorrectas() = runTest {
        repo.register("Luis", "luis@delivery.com", "333", VehicleType.CAR, "mypassword")
        val loginRes = repo.login("luis@delivery.com", "mypassword")
        assertTrue(loginRes.isSuccess)
        assertEquals("Luis", loginRes.getOrNull()?.name)
    }

    @Test
    fun loginRepartidor_passwordIncorrecto_falla() = runTest {
        repo.register("Luis", "luis2@delivery.com", "333", VehicleType.CAR, "mypassword")
        val loginRes = repo.login("luis2@delivery.com", "wrongpass")
        assertTrue(loginRes.isFailure)
    }

    @Test
    fun cambiarDisponibilidad_actualizaEstado() = runTest {
        val id = repo.register("Ana", "ana@delivery.com", "444", VehicleType.MOTORCYCLE, "123456").getOrThrow()
        repo.setAvailability(id, false)
        val courier = dao.getById(id)
        assertFalse(courier!!.isAvailable)

        repo.setAvailability(id, true)
        val courier2 = dao.getById(id)
        assertTrue(courier2!!.isAvailable)
    }
}

package com.example.appdelivery.data.repository

import com.example.appdelivery.data.dao.CartDao
import com.example.appdelivery.data.entity.CartItem
import com.example.appdelivery.data.entity.Dish
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeCartDao : CartDao {
    val state = MutableStateFlow<List<CartItem>>(emptyList())
    private var nextId = 1L
    override fun observeAll(): Flow<List<CartItem>> = state
    override suspend fun getAll() = state.value
    override suspend fun findByDish(dishId: Long) = state.value.firstOrNull { it.dishId == dishId }
    override suspend fun upsert(item: CartItem) {
        state.value = state.value.filterNot { it.dishId == item.dishId } +
            item.copy(id = if (item.id == 0L) nextId++ else item.id)
    }
    override suspend fun setQuantity(dishId: Long, quantity: Int) {
        state.value = state.value.map { if (it.dishId == dishId) it.copy(quantity = quantity) else it }
    }
    override suspend fun delete(dishId: Long) { state.value = state.value.filterNot { it.dishId == dishId } }
    override suspend fun clear() { state.value = emptyList() }
}

class CartRepositoryTest {
    private fun dish(id: Long, restaurantId: Long = 1, available: Boolean = true) = Dish(
        id = id, restaurantId = restaurantId, name = "Platillo $id", description = "d",
        price = 50.0, category = "c", available = available
    )

    private val dao = FakeCartDao()
    private val repo = CartRepositoryImpl(dao)

    @Test fun agregarNuevo_creaItemConCantidadUno() = runTest {
        assertEquals(AddResult.Added, repo.add(dish(1), "R1"))
        assertEquals(1, dao.state.value.single().quantity)
    }

    @Test fun agregarMismoPlatillo_incrementaCantidad() = runTest {
        repo.add(dish(1), "R1"); repo.add(dish(1), "R1")
        assertEquals(2, dao.state.value.single().quantity)
    }

    @Test fun platilloNoDisponible_noSeAgrega() = runTest {
        assertEquals(AddResult.Unavailable, repo.add(dish(1, available = false), "R1"))
        assertTrue(dao.state.value.isEmpty())
    }

    @Test fun otroRestaurante_devuelveConflictoYNoCambiaCarrito() = runTest {
        repo.add(dish(1, restaurantId = 1), "R1")
        val r = repo.add(dish(2, restaurantId = 2), "R2")
        assertEquals(AddResult.DifferentRestaurant("R1"), r)
        assertEquals(1, dao.state.value.size)
    }

    @Test fun reemplazarCarrito_dejaSoloElNuevo() = runTest {
        repo.add(dish(1, restaurantId = 1), "R1")
        repo.replaceCartWith(dish(2, restaurantId = 2), "R2")
        assertEquals(2L, dao.state.value.single().dishId)
    }

    @Test fun cantidadCero_eliminaItem() = runTest {
        repo.add(dish(1), "R1")
        repo.setQuantity(1, 0)
        assertTrue(dao.state.value.isEmpty())
    }
}

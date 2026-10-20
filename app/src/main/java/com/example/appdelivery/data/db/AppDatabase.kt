package com.example.appdelivery.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.appdelivery.data.dao.AddressDao
import com.example.appdelivery.data.dao.CustomerDao
import com.example.appdelivery.data.dao.DishDao
import com.example.appdelivery.data.dao.RestaurantDao
import com.example.appdelivery.data.entity.Address
import com.example.appdelivery.data.entity.Customer
import com.example.appdelivery.data.entity.Dish
import com.example.appdelivery.data.entity.Restaurant
import com.example.appdelivery.util.PasswordHasher

@Database(
    entities = [Customer::class, Address::class, Restaurant::class, Dish::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun addressDao(): AddressDao
    abstract fun restaurantDao(): RestaurantDao
    abstract fun dishDao(): DishDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "delivery.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .addCallback(seedCallback)
                    .build().also { INSTANCE = it }
            }

        private class SeedDish(
            val name: String, val description: String, val price: Double,
            val category: String, val available: Boolean = true
        )

        private class SeedRestaurant(
            val name: String, val description: String, val cuisine: String,
            val address: String, val latitude: Double, val longitude: Double,
            val dishes: List<SeedDish>
        )

        // Cuatro restaurantes cercanos y uno lejano (para probar el radio de 5 km en AD-10)
        private val seedRestaurants = listOf(
            SeedRestaurant(
                "Tacos El Güero", "Tacos al pastor y de suadero desde 1998", "Mexicana",
                "Av. Hidalgo 12", 19.6490, -99.2130,
                listOf(
                    SeedDish("Orden de tacos al pastor", "5 tacos con piña y cilantro", 85.0, "Tacos"),
                    SeedDish("Orden de tacos de suadero", "5 tacos con cebolla y salsa", 90.0, "Tacos"),
                    SeedDish("Gringa de pastor", "Tortilla de harina con queso", 65.0, "Tacos", false),
                    SeedDish("Agua de horchata", "Vaso grande de 500 ml", 25.0, "Bebidas"),
                    SeedDish("Flan casero", "Con caramelo", 35.0, "Postres")
                )
            ),
            SeedRestaurant(
                "Pizzería Don Marco", "Pizzas artesanales en horno de leña", "Italiana",
                "Calle Morelos 45", 19.6420, -99.2200,
                listOf(
                    SeedDish("Pizza Margarita", "Jitomate, mozzarella y albahaca", 140.0, "Pizzas"),
                    SeedDish("Pizza Pepperoni", "Mozzarella y pepperoni", 160.0, "Pizzas"),
                    SeedDish("Pizza Cuatro Quesos", "Mozzarella, gorgonzola, parmesano y provolone", 185.0, "Pizzas", false),
                    SeedDish("Pan de ajo", "Con mantequilla y perejil", 55.0, "Entradas"),
                    SeedDish("Refresco", "Lata de 355 ml", 25.0, "Bebidas")
                )
            ),
            SeedRestaurant(
                "Sushi Kaori", "Rollos clásicos y especiales", "Japonesa",
                "Av. Las Torres 220", 19.6560, -99.2050,
                listOf(
                    SeedDish("Rollo California", "Surimi, aguacate y pepino", 110.0, "Sushi"),
                    SeedDish("Rollo Philadelphia", "Salmón y queso crema", 130.0, "Sushi"),
                    SeedDish("Rollo Tempura", "Empanizado con anguila", 150.0, "Sushi"),
                    SeedDish("Edamames", "Con sal de mar", 60.0, "Entradas"),
                    SeedDish("Té helado", "Té verde con limón", 30.0, "Bebidas")
                )
            ),
            SeedRestaurant(
                "Hamburguesas La Esquina", "Carne 100% res con papas a la francesa", "Americana",
                "Calle Juárez 8", 19.6380, -99.2300,
                listOf(
                    SeedDish("Hamburguesa clásica", "Carne, queso, lechuga y jitomate", 95.0, "Hamburguesas"),
                    SeedDish("Hamburguesa BBQ", "Carne, tocino y salsa BBQ", 120.0, "Hamburguesas"),
                    SeedDish("Papas a la francesa", "Porción grande", 45.0, "Entradas"),
                    SeedDish("Malteada de vainilla", "400 ml", 55.0, "Bebidas"),
                    SeedDish("Brownie con helado", "Chocolate caliente", 60.0, "Postres", false)
                )
            ),
            SeedRestaurant(
                "Cocina del Centro", "Comida corrida tradicional", "Mexicana",
                "Calle 5 de Mayo 30, Centro", 19.4326, -99.1332,
                listOf(
                    SeedDish("Comida corrida", "Sopa, guisado, arroz y postre", 110.0, "Comidas"),
                    SeedDish("Enchiladas verdes", "Con pollo y crema", 95.0, "Comidas"),
                    SeedDish("Agua fresca", "Jamaica o limón", 25.0, "Bebidas")
                )
            )
        )

        // Datos de prueba: usuario demo@delivery.com / contraseña 123456 + catálogo
        private val seedCallback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                val salt = PasswordHasher.generateSalt()
                val hash = PasswordHasher.hash("123456", salt)
                db.execSQL(
                    "INSERT INTO customers (name, email, phone, passwordHash, salt) VALUES (?, ?, ?, ?, ?)",
                    arrayOf("Cliente Demo", "demo@delivery.com", "5512345678", hash, salt)
                )
                db.execSQL(
                    "INSERT INTO addresses (customerId, label, street, city, reference) VALUES (1, 'Casa', 'Av. Reforma 123', 'Cuautitlán Izcalli', 'Portón negro')"
                )
                db.execSQL(
                    "INSERT INTO addresses (customerId, label, street, city, reference) VALUES (1, 'Trabajo', 'Calle Hidalgo 45', 'Cuautitlán Izcalli', 'Piso 2')"
                )
                seedCatalog(db)
            }
        }

        private fun seedCatalog(db: SupportSQLiteDatabase) {
            seedRestaurants.forEachIndexed { index, r ->
                db.execSQL(
                    "INSERT INTO restaurants (name, description, cuisine, address, latitude, longitude) VALUES (?, ?, ?, ?, ?, ?)",
                    arrayOf<Any?>(r.name, r.description, r.cuisine, r.address, r.latitude, r.longitude)
                )
                val restaurantId = (index + 1).toLong()
                r.dishes.forEach { d ->
                    db.execSQL(
                        "INSERT INTO dishes (restaurantId, name, description, price, category, available) VALUES (?, ?, ?, ?, ?, ?)",
                        arrayOf<Any?>(restaurantId, d.name, d.description, d.price, d.category, if (d.available) 1 else 0)
                    )
                }
            }
        }
    }
}

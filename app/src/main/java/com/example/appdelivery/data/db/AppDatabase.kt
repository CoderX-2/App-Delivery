package com.example.appdelivery.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.appdelivery.data.dao.AddressDao
import com.example.appdelivery.data.dao.CustomerDao
import com.example.appdelivery.data.entity.Address
import com.example.appdelivery.data.entity.Customer
import com.example.appdelivery.util.PasswordHasher

@Database(entities = [Customer::class, Address::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customerDao(): CustomerDao
    abstract fun addressDao(): AddressDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "delivery.db"
                ).addCallback(seedCallback).build().also { INSTANCE = it }
            }

        // Datos de prueba: usuario demo@delivery.com / contraseña 123456
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
            }
        }
    }
}

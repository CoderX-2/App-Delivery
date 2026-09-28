package com.example.appdelivery.data.repository

import com.example.appdelivery.data.dao.AddressDao
import com.example.appdelivery.data.dao.CustomerDao
import com.example.appdelivery.data.entity.Address
import com.example.appdelivery.data.entity.Customer
import kotlinx.coroutines.flow.Flow

class ProfileRepository(
    private val customerDao: CustomerDao,
    private val addressDao: AddressDao
) {
    fun observeCustomer(id: Long): Flow<Customer?> = customerDao.observeById(id)

    fun observeAddresses(customerId: Long): Flow<List<Address>> =
        addressDao.observeByCustomer(customerId)

    suspend fun updateProfile(customer: Customer, name: String, phone: String) {
        customerDao.update(customer.copy(name = name.trim(), phone = phone.trim()))
    }

    suspend fun getAddress(id: Long): Address? = addressDao.getById(id)

    suspend fun addAddress(address: Address) {
        addressDao.insert(address)
    }

    suspend fun updateAddress(address: Address) {
        addressDao.update(address)
    }

    suspend fun deleteAddress(address: Address) {
        addressDao.delete(address)
    }
}

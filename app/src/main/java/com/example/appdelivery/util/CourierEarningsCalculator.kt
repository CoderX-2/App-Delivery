package com.example.appdelivery.util

import com.example.appdelivery.data.entity.Order
import com.example.appdelivery.data.entity.OrderStatus
import java.util.Calendar

data class CourierEarningsSummary(
    val todayTotal: Double,
    val weekTotal: Double,
    val allTimeTotal: Double,
    val completedCount: Int
)

object CourierEarningsCalculator {
    fun calculate(orders: List<Order>, nowMillis: Long = System.currentTimeMillis()): CourierEarningsSummary {
        val delivered = orders.filter { it.status == OrderStatus.DELIVERED }

        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfToday = cal.timeInMillis

        val startOfWeek = startOfToday - (6L * 24 * 60 * 60 * 1000)

        val todayTotal = delivered.filter { it.createdAt >= startOfToday }.sumOf { it.deliveryFee }
        val weekTotal = delivered.filter { it.createdAt >= startOfWeek }.sumOf { it.deliveryFee }
        val allTimeTotal = delivered.sumOf { it.deliveryFee }

        return CourierEarningsSummary(
            todayTotal = todayTotal,
            weekTotal = weekTotal,
            allTimeTotal = allTimeTotal,
            completedCount = delivered.size
        )
    }
}

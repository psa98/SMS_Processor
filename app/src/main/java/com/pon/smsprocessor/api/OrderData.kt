package com.pon.smsprocessor.api

import com.google.gson.Gson



data class Order(
    val token: String,
    val uHash: String,
    val uaId: String,
    val data: OrderData,
    val uaRole: String="1",
    val uRole: String="2",
)

data class OrderData(
    var b_start_address: String,
    var b_destination_address: String,
    var b_start_datetime: String,
    var b_max_waiting: Int,
    var b_passengers_count: Int,
    var b_services: Int=1,
    var b_payment_way: String = "1",
)

fun OrderData.toGson() = Gson().toJson(this)

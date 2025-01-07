package com.pon.smsprocessor.api

import com.google.gson.Gson



data class Order(
    val token: String,
    val uHash: String,
    val uaId: String,
    val data: OrderDataRequest,
    val uaRole: String="1",
    val uRole: String="2",
)

data class OrderDataRequest(
    var b_start_address: String,
    var b_destination_address: String,
    var b_start_datetime: String,
    var b_max_waiting: Int,
    var b_passengers_count: Int,
    var b_services: List<Int>,
    val b_options: Map<String,Boolean> = mapOf(Pair("sms",true)),
    var b_payment_way: String = "1",
)

fun OrderDataRequest.toGson(): String = Gson().toJson(this)

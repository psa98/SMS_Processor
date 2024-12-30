package com.pon.smsprocessor.api


class GetAdminTokenResponse (
    val code: String,
    val status: String,
    val message: String,
    val data: ArrayList<Any>? = null
)

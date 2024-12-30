package com.pon.smsprocessor.api

data class RegistrationResponse (
    var code: String? = null,
    var status: String? = null,
    var data: RegistrationData? = null
)

data class RegistrationData (
    var u_id: Int = 0,
    var string: String? = null,
    var token: String? = null,
    var u_hash: String? = null,
)

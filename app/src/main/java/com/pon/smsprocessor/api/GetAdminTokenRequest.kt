package com.pon.smsprocessor.api


data class GetAdminTokenRequest(

    var login: String = bot_admin_login,
    var password: String = bot_admin_password,
    var type: String= bot_admin_type
)

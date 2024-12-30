package com.pon.smsprocessor.api

class User {
    var u_id: String? = null
    var u_name: String? = null
    var u_family: String? = null
    var u_middle: String? = null
    var u_email: String? = null
    var u_phone: Any? = null
    var u_role: String? = null
    var u_a_role: Any? = null
    var u_check_state: Any? = null
    var u_ban: UBans? = null
    var u_active: Int = 0
    var u_photo: String? = null
    var u_birthday: Any? = null
    var u_lang: String? = null
    var u_currency: Any? = null
    var u_gps_software: Any? = null
}

class Data {
    var token: String? = null
    var u_hash: String? = null
}

class TokenResponse {
    var code: String? = null
    var status: String? = null
    var data: Data? = null
    var auth_user: User? = null
}

class UBans {
    var auth: Any? = null
    var order: Any? = null
    var blog_topic: Any? = null
    var blog_post: Any? = null
}





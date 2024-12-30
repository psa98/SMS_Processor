package com.pon.smsprocessor.api


class AuthUser {
    var u_id: String? = null
    var u_name: String? = null
    var u_family: String? = null
    var u_middle: String? = null
    var u_email: String? = null
    var u_phone: Any? = null
    var u_role: String? = null
    var u_a_role: Any? = null
    var u_check_state: String? = null
    var u_ban: UBan? = null
    var u_active: Int = 0
    var u_photo: String? = null
    var u_birthday: Any? = null
    var u_phone_checked: Int = 0
    var u_lang: String? = null
    var u_currency: Any? = null
    var u_city: Any? = null
    var u_tips: String? = null
    var u_lang_skills: String? = null
    var u_description: String? = null
    var u_gps_software: Any? = null
}

class AuthResp {
    var code: String? = null
    var status: String? = null
    var message: String? = null
    var auth_user: AuthUser? = null
    var auth_hash: String? = null
}

class UBan {
    var auth: Any? = null
    var order: Any? = null
    var blog_topic: Any? = null
    var blog_post: Any? = null
}

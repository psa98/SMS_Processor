package com.pon.smsprocessor.api


data class IsRegisteredResponse(
    var code: String? = null,
    var status: String? = null,
    var data: Any? = null,
    var auth_user: CheckedUser? = null
)

class CheckedUser {
    var u_id: String? = null
    var u_name: String? = null
    var u_family: String? = null
    var u_middle: String? = null
    var u_email: String? = null
    var u_phone: Any? = null
    var u_role: String? = null
    var u_a_role: String? = null
    var u_check_state: Any? = null
    var u_ban: CheckedUserBan? = null
    var u_active: Int = 0
    var u_photo: String? = null
    var u_birthday: Any? = null
    var u_lang: String? = null
    var u_currency: Any? = null
    var u_gps_software: Any? = null
}


class CheckedUserBan {
    var auth: Any? = null
    var order: Any? = null
    var blog_topic: Any? = null
    var blog_post: Any? = null
}


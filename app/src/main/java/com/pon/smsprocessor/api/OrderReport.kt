package com.pon.smsprocessor.api

import com.google.gson.annotations.SerializedName

class OrderReport {
    var code: String? = null
    var status: String? = null
    @SerializedName("data")
    var data: DataForOrder? = null
    var auth_user: AuthUser? = null

}


class OrderData {
    var b_id: String? = null
    var u_id: String? = null
    var b_start_address: String? = null
    var b_start_latitude: Any? = null
    var b_start_longitude: Any? = null
    var b_destination_address: String? = null
    var b_destination_latitude: Any? = null
    var b_destination_longitude: Any? = null
    var b_start_datetime: String? = null
    var b_custom_comment: String? = null
    var b_flight_number: String? = null
    var b_terminal: String? = null
    var b_passengers_count: String? = null
    var b_luggage_count: String? = null
    var b_placard: String? = null
    var b_car_class: Any? = null
    var b_payment_way: String? = null
    var b_payment_card: Any? = null
    var b_confirmation_limit: Any? = null
    var b_confirmation_datetime: Any? = null
    var b_payment_sum: Any? = null
    var b_payment_datetime: Any? = null
    var b_driver_code: String? = null
    var b_attempts: Any? = null
    var b_tips: Any? = null
    var b_state: String? = null
    var b_rating: Any? = null
    var b_created: String? = null
    var b_confirm_state: Int = 0
    var b_cars_count: String? = null
    var b_cancel_reason: String? = null
    var b_cancel_states: Any? = null
    var b_approved: Any? = null
    var b_canceled: String? = null
    var b_completed: Any? = null
    var b_max_waiting: Int = 0
    var b_max_waiting_list: BMaxWaitingList? = null
    var b_estimate_waiting: Any? = null
    var b_options: Any? = null
    var b_contact: Any? = null
    var b_location_class: String? = null
    var b_distance_estimate: Any? = null
    var b_price_estimate: Any? = null
    var b_currency: Any? = null
    var b_night: Any? = null
    var drivers: Any? = null
    var b_comments: Any? = null
    var b_services: ArrayList<String>? = null
    var b_voting: Int = 0

}


class BMaxWaitingList {
    var data: Any? = null
}

class DataForOrder {
    var booking: HashMap<String, OrderData>? = null
    var message: HashMap<String, String>? = null
}




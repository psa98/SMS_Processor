package com.pon.smsprocessor

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.sign

object DefaultsRepository {

    val failMessage: String = "*Заказ такси не удался*"
    val okMessage: String = "*Заказ такси выполнен*"

    val retryCount = 3

    val retryTime = 20*1000L

    private val preferences = App.appContext.getSharedPreferences("main", MODE_PRIVATE)
    var divider: String = preferences.getString("divider", "/").toString()
        set(value) {
            field = value
            preferences.edit().putString("divider", field).apply()
        }
    var time: Int = preferences.getInt("timeDef", 0)
        set(value) {
            field = value
            preferences.edit().putInt("timeDef", field ).apply()
        }
    var orderType: Int = preferences.getInt("type", 1)
        set(value) {
            field = value
            preferences.edit().putInt("type", field ).apply()
        }
    var cancelTime: Int = preferences.getInt("time", 3)
        set(value) {
            field = value
            preferences.edit().putInt("time", field ).apply()
        }
    var stopCodes: String = preferences.getString("codes", "3").toString()
        set(value) {
            field = value
            stopCodesList = stopCodes.split(",").mapNotNull { it.trim().toIntOrNull() }
            Log.i(TAG, "Stop codes: $stopCodesList ")
            preferences.edit().putString("codes", field).apply()
        }

    var stopCodesList = stopCodes.split(",").mapNotNull { it.trim().toIntOrNull() }


    @SuppressLint("SimpleDateFormat")
    fun formattedDate(withTime:String /* время в формате 00:00:00 */): String {

            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            val zoneOffsetHr = TimeZone.getDefault().getOffset(Date().time) / 3600000
            val plusMinus = sign(zoneOffsetHr.toDouble())
            val sign = if (plusMinus > 0) "+" else "-"
            val amount = abs(zoneOffsetHr)
            val textZ = "$sign${if (amount< 10) "0$amount" else amount}:00"
            return dateFormat.format(Date())+ " $withTime" + textZ

    }

}
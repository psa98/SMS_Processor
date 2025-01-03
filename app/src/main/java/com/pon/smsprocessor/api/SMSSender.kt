package com.pon.smsprocessor.api

import android.telephony.SmsManager
import com.pon.smsprocessor.LogRepository

object SMSSender {
    private var smsManager: SmsManager = SmsManager.getDefault()

    fun sendSMS(number: String, message: String) {
        if (number.isEmpty() || message.isEmpty()) return

        try {
            smsManager.sendTextMessage(number, null, message, null, null)
        } catch (exception: SecurityException) {
            LogRepository.addToLog("Ошибка отправки смс\n")
            exception.printStackTrace()
        }
    }


}

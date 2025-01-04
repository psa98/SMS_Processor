package com.pon.smsprocessor.api

import android.telephony.SmsManager
import com.pon.smsprocessor.App
import com.pon.smsprocessor.Logger

object SMSSender {
    private var smsManager: SmsManager? = try {
        App.appContext.getSystemService(SmsManager::class.java)
    } catch (e: Exception) {
        null
    }

    fun sendSMS(number: String, message: String) {
        if (number.isEmpty() || message.isEmpty()) return
        if (smsManager==null) {
            Logger.addToLog("Отправка смс не поддерживается")
            return
        }
        Logger.addToLog("Отправлено sms $number\n $message   ")
        try {
            smsManager?.sendTextMessage(number, null, message, null, null)
        } catch (exception: SecurityException) {
            Logger.addToLog("Ошибка отправки смс\n")
            exception.printStackTrace()
        }
    }


}

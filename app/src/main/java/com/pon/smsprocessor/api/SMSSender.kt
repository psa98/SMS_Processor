package com.pon.smsprocessor.api

import android.os.Build
import android.telephony.SmsManager
import com.pon.smsprocessor.App
import com.pon.smsprocessor.LogRepository

object SMSSender {
    private var smsManager: SmsManager? = try {
        App.appContext.getSystemService(SmsManager::class.java)
    } catch (e: Exception) {
        null
    }

    fun sendSMS(number: String, message: String) {
        if (number.isEmpty() || message.isEmpty()) return
        if (smsManager==null) {
            LogRepository.addToLog("Отправка смс не поддерживается")
            return
        }
        LogRepository.addToLog("Отправлено sms $number\n $message   ")
        try {
            smsManager?.sendTextMessage(number, null, message, null, null)
        } catch (exception: SecurityException) {
            LogRepository.addToLog("Ошибка отправки смс\n")
            exception.printStackTrace()
        }
    }


}

package com.pon.smsprocessor

import android.telephony.SmsManager

object SMSSender {
    private var smsManager: SmsManager? = try {
        App.appContext.getSystemService(SmsManager::class.java)
    } catch (e: Exception) {
        null
    }

    fun sendSMS(number: String, message: String) {
        if (number.isEmpty() || message.isEmpty()) return

        if (smsManager ==null) {
            Logger.addToLog("Отправка смс не поддерживается устройством")
            return
        }

        try {
            if (!DefaultsRepository.realSMS )
                Logger.addToLog("ИМИТИРОВАНА ОТПРАВКА СМС $number\n $message ")
            else{
            smsManager?.sendTextMessage(number, null, message, null, null)
                Logger.addToLog("ОТПРАВКА СМС $number\n $message ")
            }
        } catch (exception: Exception) {
            Logger.addToLog("Ошибка отправки смс\n $exception")
            exception.printStackTrace()
        }
    }


}

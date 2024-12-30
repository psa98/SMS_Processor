package com.pon.smsprocessor

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Telephony
import androidx.core.content.ContextCompat

const val TAG = "SMS Processor"
class App: Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = this
        ContextCompat.startForegroundService(
            this, Intent(this, ForegroundService::class.java)
        )
        val smsReceiver = SmsReceiver()
        this.registerReceiver(smsReceiver, IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION))
    }


    companion object {
        lateinit var appContext: Context

    }
}


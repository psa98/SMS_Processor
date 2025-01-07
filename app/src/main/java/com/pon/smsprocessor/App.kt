package com.pon.smsprocessor

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Telephony
import androidx.core.content.ContextCompat
import com.pon.smsprocessor.room.AppDatabase
import com.pon.smsprocessor.room.LogsDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

const val TAG = "SMS Processor"
class App: Application() {


    override fun onCreate() {
        super.onCreate()
        appContext = this
        roomDao  = AppDatabase.getInstance(appContext).logDao()
        ContextCompat.startForegroundService(
            this, Intent(this, ForegroundService::class.java)
        )
        val smsReceiver = SmsReceiver()
        this.registerReceiver(smsReceiver, IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION))
        CoroutineScope(IO).launch { TaxiRepository.authAdmin() }
    }


    companion object {
        lateinit var appContext: Context

        lateinit var  roomDao: LogsDao
    }
}


package com.pon.smsprocessor

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat


class KeepAliveReceiver : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {

        Log.i(TAG, "Started on boot!")
        try {
          ContextCompat.startForegroundService(context, Intent(context, ForegroundService::class.java))
        } catch (e:Exception){
            e.printStackTrace()
        }

    }
}
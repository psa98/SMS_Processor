package com.pon.smsprocessor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat


class ForegroundService : Service() {


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: ")
        val notifyIntent:Intent = Intent(this, StartActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val notifyPendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val name: CharSequence = "Канал уведомлений"
        val description = "Уведомления приложения"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        channel.description = description
        channel.setShowBadge(false)
        val notificationManager = getSystemService(
            NotificationManager::class.java
        )
        notificationManager.createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentText( "SMS Processor" )
            .setPriority(NotificationManager.IMPORTANCE_LOW)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setLargeIcon(getScaledBitmap(R.drawable.ic__646817))
            .setChannelId(CHANNEL_ID)
            .setSmallIcon(R.drawable.ic__646817)
            .setContentIntent(notifyPendingIntent)
            .build()
        startForeground(notificationId, notification)
        return START_STICKY
    }


    override fun onBind(intent: Intent?): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    // получаем битмап разумного размера из xml
    private fun getScaledBitmap(drawableRes: Int): Bitmap? {
        val drawable = ResourcesCompat.getDrawable(resources, drawableRes, null) ?: return null
        val bitmap = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas()
        canvas.setBitmap(bitmap)
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        drawable.draw(canvas)
        return bitmap
    }


    companion object {
        private const val CHANNEL_ID = "MY_CHANNEL_1"
        private const val notificationId = 1
    }
}
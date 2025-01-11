package com.example.qrtimer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

class RingtoneService : Service() {

    private var ringtone: Ringtone? = null
    private var isRingtonePlaying = false

    companion object {
        const val CHANNEL_ID = "RingtoneServiceChannel"
        const val ACTION_START_RINGTONE = "com.example.qrtimer.ACTION_START_RINGTONE"
        const val ACTION_STOP_RINGTONE = "com.example.qrtimer.ACTION_STOP_RINGTONE"
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_RINGTONE -> {
                startForegroundService()
                playRingtone()
            }
            ACTION_STOP_RINGTONE -> {
                stopRingtone()
                stopForegroundService()
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startForegroundService() {
        createNotificationChannel()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Timer Ringing")
            .setContentText("Tap to return to the app")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your icon
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
    }

    private fun stopForegroundService() {
        stopForeground(true)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Ringtone Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun playRingtone() {
        try {
            val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ringtone = RingtoneManager.getRingtone(applicationContext, notification)
            ringtone?.isLooping = true
            ringtone?.play()
            isRingtonePlaying = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopRingtone() {
        ringtone?.stop()
        ringtone = null
        isRingtonePlaying = false
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRingtone()
    }
}
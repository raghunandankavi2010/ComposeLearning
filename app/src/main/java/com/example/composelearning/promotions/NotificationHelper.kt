package com.example.composelearning.promotions

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.composelearning.R

object NotificationHelper {
    const val CHANNEL_ID = "promotional_deals"
    private const val NOTIFICATION_ID = 1001

    /**
     * Checks if notifications are globally enabled and the channel is not blocked.
     */
    fun areNotificationsEnabled(context: Context): Boolean {
        val notificationManager = NotificationManagerCompat.from(context)
        if (!notificationManager.areNotificationsEnabled()) return false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = notificationManager.getNotificationChannel(CHANNEL_ID)
            // If channel exists and is importance none, it's blocked
            return channel == null || channel.importance != NotificationManager.IMPORTANCE_NONE
        }
        return true
    }

    /**
     * Opens the system settings for this app's notification channel.
     */
    fun openNotificationSettings(context: Context) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
                putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                putExtra(Settings.EXTRA_CHANNEL_ID, CHANNEL_ID)
            }
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
        }
        context.startActivity(intent)
    }

    /**
     * Shows or updates a notification with a real-time countdown.
     * Uses setChronometer to let the OS handle the countdown efficiently.
     *
     * @param context Application context.
     * @param targetEndTimestamp Epoch milliseconds when the deal ends.
     */
    fun showDealCountdownNotification(
        context: Context,
        dealTitle: String,
        targetEndTimestamp: Long
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Promotional Deals"
            val descriptionText = "Notifications for limited time offers"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Calculate when the deal ends relative to SystemClock.elapsedRealtime()
        val remainingMillis = targetEndTimestamp - System.currentTimeMillis()
        val endTimestampElapsed = SystemClock.elapsedRealtime() + remainingMillis

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(dealTitle)
            .setContentText("Limited time offer!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setWhen(endTimestampElapsed) // This is the 'base' time for the chronometer
            .setShowWhen(true)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}

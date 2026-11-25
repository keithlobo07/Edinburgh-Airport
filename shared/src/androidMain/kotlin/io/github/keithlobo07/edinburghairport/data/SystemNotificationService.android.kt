package io.github.keithlobo07.edinburghairport.data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

actual object SystemNotificationService {

    private const val CHANNEL_FLIGHTS = "edi_flight_alerts_channel"
    private const val CHANNEL_SECURITY = "edi_security_channel"
    private const val CHANNEL_SERVICES = "edi_services_channel"

    private var channelsInitialized = false

    private fun ensureChannels(context: Context) {
        if (channelsInitialized || Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        // 1. Flight & Gate Alerts (Urgent, Heads-up banner, Sound & Vibration)
        val flightChannel = NotificationChannel(
            CHANNEL_FLIGHTS,
            "Flight & Gate Alerts",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Live gate announcements, boarding notices, and departure time updates."
            enableLights(true)
            lightColor = Color.parseColor("#752969") // EdinburghPlum
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 300, 150, 300)
            setShowBadge(true)
        }

        // 2. Security Hall Wait Times
        val securityChannel = NotificationChannel(
            CHANNEL_SECURITY,
            "Security Queue Times",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Live security checkpoint queue times and FastTrack updates."
            enableLights(true)
            lightColor = Color.parseColor("#052145") // EdinburghNavy
        }

        // 3. Airport Services & Bookings
        val servicesChannel = NotificationChannel(
            CHANNEL_SERVICES,
            "Airport Bookings & Passes",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Parking reservations, tram passes, and lounge access vouchers."
        }

        manager.createNotificationChannels(listOf(flightChannel, securityChannel, servicesChannel))
        channelsInitialized = true
    }

    actual fun showNotification(
        id: Int,
        title: String,
        message: String,
        isUrgent: Boolean,
        channelType: NotificationChannelType
    ) {
        val context = AndroidContextHolder.context ?: return
        ensureChannels(context)

        // Permission check on Android 13+ (API 33+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                return
            }
        }

        val channelId = when (channelType) {
            NotificationChannelType.FLIGHT_ALERTS -> CHANNEL_FLIGHTS
            NotificationChannelType.SECURITY_UPDATES -> CHANNEL_SECURITY
            NotificationChannelType.BOOKING_SERVICES -> CHANNEL_SERVICES
        }

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = launchIntent?.let {
            PendingIntent.getActivity(
                context,
                id,
                it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val smallIcon = context.applicationInfo.icon.takeIf { it != 0 } ?: android.R.drawable.ic_dialog_info

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(smallIcon)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setColor(Color.parseColor("#752969")) // EdinburghPlum
            .setAutoCancel(true)
            .setPriority(
                if (isUrgent) NotificationCompat.PRIORITY_MAX
                else NotificationCompat.PRIORITY_HIGH
            )
            .setCategory(
                if (isUrgent) NotificationCompat.CATEGORY_ALARM
                else NotificationCompat.CATEGORY_EVENT
            )

        pendingIntent?.let { builder.setContentIntent(it) }

        if (isUrgent) {
            builder.setVibrate(longArrayOf(0, 300, 150, 300))
        }

        try {
            NotificationManagerCompat.from(context).notify(id, builder.build())
        } catch (_: SecurityException) {
            // Permission revoked
        }
    }

    actual fun hasPermission(): Boolean {
        val context = AndroidContextHolder.context ?: return false
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    actual fun requestPermission() {
        val activity = AndroidContextHolder.activity ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasPermission()) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1002
                )
            }
        }
    }
}

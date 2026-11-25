package io.github.keithlobo07.edinburghairport.data

enum class NotificationChannelType {
    FLIGHT_ALERTS,
    SECURITY_UPDATES,
    BOOKING_SERVICES
}

expect object SystemNotificationService {
    fun showNotification(
        id: Int,
        title: String,
        message: String,
        isUrgent: Boolean = false,
        channelType: NotificationChannelType = NotificationChannelType.FLIGHT_ALERTS
    )

    fun hasPermission(): Boolean
    fun requestPermission()
}

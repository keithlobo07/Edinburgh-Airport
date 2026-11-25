package io.github.keithlobo07.edinburghairport.data

actual object SystemNotificationService {
    actual fun showNotification(
        id: Int,
        title: String,
        message: String,
        isUrgent: Boolean,
        channelType: NotificationChannelType
    ) {
        if (!hasPermission()) return
        try {
            val t = title
            val m = message
            js("new Notification(t, { body: m })")
        } catch (_: Throwable) {}
    }

    actual fun hasPermission(): Boolean {
        return try {
            js("typeof Notification !== 'undefined' && Notification.permission === 'granted'") as Boolean
        } catch (_: Throwable) {
            false
        }
    }

    actual fun requestPermission() {
        try {
            js("if (typeof Notification !== 'undefined' && Notification.permission === 'default') Notification.requestPermission()")
        } catch (_: Throwable) {}
    }
}

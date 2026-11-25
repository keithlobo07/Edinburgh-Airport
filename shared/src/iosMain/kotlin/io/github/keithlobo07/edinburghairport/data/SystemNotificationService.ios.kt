package io.github.keithlobo07.edinburghairport.data

import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNAuthorizationStatusAuthorized
import platform.UserNotifications.UNAuthorizationStatusProvisional
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter

actual object SystemNotificationService {
    private var cachedPermission: Boolean = false

    init {
        refreshPermissionStatus()
    }

    private fun refreshPermissionStatus() {
        UNUserNotificationCenter.currentNotificationCenter()
            .getNotificationSettingsWithCompletionHandler { settings ->
                val status = settings?.authorizationStatus
                cachedPermission = status == UNAuthorizationStatusAuthorized ||
                    status == UNAuthorizationStatusProvisional
            }
    }

    actual fun showNotification(
        id: Int,
        title: String,
        message: String,
        isUrgent: Boolean,
        channelType: NotificationChannelType
    ) {
        val center = UNUserNotificationCenter.currentNotificationCenter()
        val content = UNMutableNotificationContent().apply {
            setTitle(title)
            setBody(message)
            setSound(UNNotificationSound.defaultSound)
            setThreadIdentifier(channelType.name)
        }
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "edi_airport_notif_$id",
            content = content,
            trigger = null
        )
        center.addNotificationRequest(request, withCompletionHandler = null)
    }

    actual fun hasPermission(): Boolean {
        refreshPermissionStatus()
        return cachedPermission
    }

    actual fun requestPermission() {
        val options = UNAuthorizationOptionAlert or
            UNAuthorizationOptionSound or
            UNAuthorizationOptionBadge
        UNUserNotificationCenter.currentNotificationCenter()
            .requestAuthorizationWithOptions(options) { granted, _ ->
                cachedPermission = granted
            }
    }
}

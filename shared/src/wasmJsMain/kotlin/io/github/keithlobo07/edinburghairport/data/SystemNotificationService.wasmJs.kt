@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package io.github.keithlobo07.edinburghairport.data

@JsFun("(title, body) => { if (typeof Notification !== 'undefined' && Notification.permission === 'granted') { new Notification(title, { body }); } }")
private external fun wasmShowNotification(title: String, body: String)

@JsFun("() => typeof Notification !== 'undefined' && Notification.permission === 'granted'")
private external fun wasmHasNotificationPermission(): Boolean

@JsFun("() => { if (typeof Notification !== 'undefined' && Notification.permission === 'default') Notification.requestPermission(); }")
private external fun wasmRequestNotificationPermission()

actual object SystemNotificationService {
    actual fun showNotification(
        id: Int,
        title: String,
        message: String,
        isUrgent: Boolean,
        channelType: NotificationChannelType
    ) {
        wasmShowNotification(title, message)
    }

    actual fun hasPermission(): Boolean = wasmHasNotificationPermission()
    actual fun requestPermission() = wasmRequestNotificationPermission()
}

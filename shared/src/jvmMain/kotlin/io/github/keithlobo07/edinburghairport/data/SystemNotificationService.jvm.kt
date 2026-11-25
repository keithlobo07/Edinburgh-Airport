package io.github.keithlobo07.edinburghairport.data

import java.awt.Color
import java.awt.SystemTray
import java.awt.TrayIcon
import java.awt.image.BufferedImage

actual object SystemNotificationService {
    private var trayIcon: TrayIcon? = null

    actual fun showNotification(
        id: Int,
        title: String,
        message: String,
        isUrgent: Boolean,
        channelType: NotificationChannelType
    ) {
        if (!SystemTray.isSupported()) return
        try {
            val icon = getOrCreateTrayIcon() ?: return
            val messageType = if (isUrgent) TrayIcon.MessageType.WARNING else TrayIcon.MessageType.INFO
            icon.displayMessage(title, message, messageType)
        } catch (_: Exception) {
            // Headless or tray unavailable
        }
    }

    actual fun hasPermission(): Boolean = true
    actual fun requestPermission() {}

    private fun getOrCreateTrayIcon(): TrayIcon? {
        trayIcon?.let { return it }
        val tray = SystemTray.getSystemTray()
        val img = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB).apply {
            val g = createGraphics()
            g.color = Color(0x75, 0x29, 0x69)
            g.fillOval(1, 1, 14, 14)
            g.color = Color.WHITE
            g.fillOval(5, 5, 6, 6)
            g.dispose()
        }
        val created = TrayIcon(img, "Edinburgh Airport").apply {
            isImageAutoSize = true
        }
        tray.add(created)
        trayIcon = created
        return created
    }
}

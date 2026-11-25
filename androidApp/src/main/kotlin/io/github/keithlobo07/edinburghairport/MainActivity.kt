package io.github.keithlobo07.edinburghairport

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.keithlobo07.edinburghairport.data.AndroidContextHolder
import io.github.keithlobo07.edinburghairport.data.SystemNotificationService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        AndroidContextHolder.activity = this
        AndroidContextHolder.context = applicationContext
        SystemNotificationService.requestPermission()

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
package io.github.keithlobo07.edinburghairport.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.keithlobo07.edinburghairport.theme.EdinburghPlum
import io.github.keithlobo07.edinburghairport.theme.TextWhite
import kotlinx.browser.document
import org.w3c.dom.HTMLInputElement
import org.w3c.files.FileReader

@Composable
actual fun CameraBarcodeScanner(
    modifier: Modifier,
    isTorchOn: Boolean,
    onBarcodeScanned: (String) -> Unit
) {
    Column(
        modifier = modifier
            .background(Color(0xFF0F172A))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Web Mode • Upload a .txt/.bcbp pass file or use 1-tap demo below",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                val input = document.createElement("input") as HTMLInputElement
                input.type = "file"
                input.accept = ".txt,.bcbp"
                input.onchange = {
                    val file = input.files?.item(0)
                    if (file != null) {
                        val reader = FileReader()
                        reader.onload = {
                            val text = reader.result?.toString()?.trim().orEmpty()
                            if (text.isNotBlank()) onBarcodeScanned(text)
                        }
                        reader.readAsText(file)
                    }
                }
                input.click()
            },
            colors = ButtonDefaults.buttonColors(containerColor = EdinburghPlum),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Upload Pass File", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

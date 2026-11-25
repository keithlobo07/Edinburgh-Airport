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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.keithlobo07.edinburghairport.theme.EdinburghPlum
import io.github.keithlobo07.edinburghairport.theme.TextWhite
import java.awt.FileDialog
import java.awt.Frame
import java.io.File

@Composable
actual fun CameraBarcodeScanner(
    modifier: Modifier,
    isTorchOn: Boolean,
    onBarcodeScanned: (String) -> Unit
) {
    var statusText by remember { mutableStateOf("Desktop Mode • Select a .txt/.bcbp pass file or use 1-tap demo below") }

    Column(
        modifier = modifier
            .background(Color(0xFF0F172A))
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = statusText,
            color = Color(0xFF94A3B8),
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Button(
            onClick = {
                val dialog = FileDialog(null as Frame?, "Select Boarding Pass BCBP File", FileDialog.LOAD)
                dialog.isVisible = true
                val fileName = dialog.file
                val dir = dialog.directory
                if (fileName != null && dir != null) {
                    val content = runCatching { File(dir, fileName).readText().trim() }.getOrNull()
                    if (!content.isNullOrBlank()) {
                        onBarcodeScanned(content)
                    } else {
                        statusText = "Could not read barcode payload from $fileName"
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = EdinburghPlum),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Load Pass File", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
    }
}

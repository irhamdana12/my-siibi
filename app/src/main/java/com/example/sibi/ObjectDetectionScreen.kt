package com.example.sibi

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.ImageProxy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@androidx.camera.core.ExperimentalGetImage
@Composable
fun ObjectDetectionScreen(objectDetectionHelper: ObjectDetectionHelper) {
    val context = LocalContext.current
    var detectionResult by remember { mutableStateOf<List<ObjectDetectionHelper.DetectionResult>>(emptyList()) }

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            onDetect = { imageProxy ->
                val bitmap = imageProxyToBitmap(imageProxy)
                imageProxy.close()

                // Jalankan deteksi objek dan perbarui hasil
                val results = objectDetectionHelper.detectObjects(bitmap)
                detectionResult = results
            }
        )

        // Overlay hasil deteksi di atas pratinjau kamera
        DetectionOverlay(detectionResult)
    }
}

@Composable
fun DetectionOverlay(detectionResults: List<ObjectDetectionHelper.DetectionResult>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.Start
    ) {
        detectionResults.forEach { result ->
            Text(
                text = "Detected: ${result.category} (${(result.score * 100).toInt()}%)",
                color = Color.White,
                style = MaterialTheme.typography.h6
            )
        }
    }
}
fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {
    val buffer = imageProxy.planes[0].buffer
    val bytes = ByteArray(buffer.capacity())
    buffer.get(bytes)
    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
}


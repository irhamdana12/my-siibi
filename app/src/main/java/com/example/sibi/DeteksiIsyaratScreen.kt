package com.example.sibi

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import androidx.annotation.OptIn
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.sibi.ml.MetadataSibi
import org.tensorflow.lite.support.image.TensorImage
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

class IsyaratRecognizer(private val context: Context) {
    private val model: MetadataSibi = MetadataSibi.newInstance(context)
    private val labels: List<String> = context.assets.open("labelmap.txt").bufferedReader().readLines()

    fun recognize(bitmap: Bitmap): String {
        val image = TensorImage.fromBitmap(bitmap)
        val outputs = model.process(image)
        val detectionResult = outputs.detectionResultList[0]

        val score = detectionResult.scoreAsFloat
        val category = detectionResult.categoryAsString

        return if (score > 0.5) { // Sesuaikan threshold ini sesuai kebutuhan
            category
        } else {
            "Tidak dikenal"
        }
    }

    fun close() {
        model.close()
    }
}

@OptIn(ExperimentalGetImage::class)
@Composable
fun CameraPreview(
    recognizer: IsyaratRecognizer,
    onIsyaratRecognized: (String) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var preview by remember { mutableStateOf<Preview?>(null) }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
        },
        modifier = Modifier.fillMaxSize(),
        update = { previewView ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .apply {
                        setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                            val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                            val image = imageProxy.image
                            if (image != null) {
                                val bitmap = imageProxy.toBitmap()
                                val isyarat = recognizer.recognize(bitmap)
                                onIsyaratRecognized(isyarat)
                            }
                            imageProxy.close()
                        }
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalyzer
                    )
                } catch (exc: Exception) {
                    // Tangani error jika ada
                }
            }, ContextCompat.getMainExecutor(context))
        }
    )
}

@Composable
fun DeteksiIsyaratScreen() {
    val context = LocalContext.current
    var recognizedIsyarat by remember { mutableStateOf<String?>(null) }
    val recognizer = remember { IsyaratRecognizer(context) }

    DisposableEffect(Unit) {
        onDispose {
            recognizer.close()
        }
    }

    Column {
        CameraPreview(
            recognizer = recognizer,
            onIsyaratRecognized = { isyarat ->
                recognizedIsyarat = isyarat
            }
        )

        recognizedIsyarat?.let {
            Text("Isyarat Terdeteksi: $it")
        }
    }
}

// Fungsi ekstensi untuk mengonversi ImageProxy ke Bitmap
fun ImageProxy.toBitmap(): Bitmap {
    val yBuffer = planes[0].buffer
    val uBuffer = planes[1].buffer
    val vBuffer = planes[2].buffer

    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()

    val nv21 = ByteArray(ySize + uSize + vSize)

    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 100, out)
    val imageBytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}
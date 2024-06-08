package co.id.fadlurahmanfdev.kotlin_feature_face_recognition.domain.analyzer

import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage

class FaceDetectionAnalyzer(private val listener: Listener) : ImageAnalysis.Analyzer {
    private val handler = Handler(Looper.getMainLooper())

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        handler.postDelayed({
            process(imageProxy)
        }, 1000)
    }

    @ExperimentalGetImage
    private fun process(imageProxy: ImageProxy) {
        imageProxy.image?.let { image ->
            val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
            listener.onDetectedFace(inputImage)
        }

        imageProxy.close()
    }

    interface Listener {
        fun onDetectedFace(inputImage: InputImage)
    }
}
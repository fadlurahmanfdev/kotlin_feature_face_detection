package co.id.fadlurahmanfdev.kotlin_feature_face_recognition.domain.plugin

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import co.id.fadlurahmanfdev.kotlin_feature_face_recognition.data.exception.FeatureFaceDetectionException
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceLandmark

class FaceDetectionManager {
    private lateinit var faceDetector: FaceDetector
    private var listener: Listener? = null
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var currentImageProxy: ImageProxy

    fun initialize() {
        initialize(null)
    }

    fun initialize(listener: Listener?) {
        this.listener = listener
        val option = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
        faceDetector = FaceDetection.getClient(option)
    }

    private var processCountImage = 0

    @ExperimentalGetImage
    private val runnableProcessImage = Runnable {
        val image = currentImageProxy.image
        if (image != null) {
            val inputImage =
                InputImage.fromMediaImage(image, currentImageProxy.imageInfo.rotationDegrees)
            faceDetector.process(inputImage).addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    if (faces.size == 1) {
                        val face = faces.first()
                        listener?.onFaceDetected(face)
                    } else {
                        val exception = FeatureFaceDetectionException(
                            code = "ERR_MULTIPLE_FACES",
                            message = "Multiple faces detected in the image."
                        )
                        listener?.onFailureDetectedFace(exception)
                    }
                } else {
                    listener?.onEmptyFaceDetected()
                }
            }.addOnFailureListener {
                val exception = FeatureFaceDetectionException(
                    code = "ERR_GENERAL",
                    message = it.message
                )
                listener?.onFailureDetectedFace(exception)
                currentImageProxy.close()
            }.addOnCompleteListener {
                currentImageProxy.close()
            }
        } else {
            Log.d(
                FaceDetectionManager::class.java.simpleName,
                "image inside imageProxy didn't detected"
            )
        }
    }

    @ExperimentalGetImage
    fun processImage(imageProxy: ImageProxy) {
        currentImageProxy = imageProxy
        handler.postDelayed(runnableProcessImage, 3000)
    }

    private var livenessListener: LivenessListener? = null

    @ExperimentalGetImage
    fun processLivenessImage(imageProxy: ImageProxy, listener: LivenessListener) {
        if(livenessListener == null){
            livenessListener = listener
        }
        currentImageProxy = imageProxy
        handler.postDelayed(runnableProcessImage, 3000)
    }

    @ExperimentalGetImage
    fun destroy() {
        handler.removeCallbacks(runnableProcessImage)
    }

    interface Listener {
        fun onFaceDetected(face: Face)
        fun onEmptyFaceDetected()
        fun onFailureDetectedFace(exception: FeatureFaceDetectionException)
    }

    interface LivenessListener : Listener {
        fun onShouldCloseLeftEye()
    }
}
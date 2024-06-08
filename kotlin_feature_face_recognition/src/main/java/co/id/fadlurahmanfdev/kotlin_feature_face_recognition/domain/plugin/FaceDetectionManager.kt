package co.id.fadlurahmanfdev.kotlin_feature_face_recognition.domain.plugin

import co.id.fadlurahmanfdev.kotlin_feature_face_recognition.data.exception.FeatureFaceDetectionException
import co.id.fadlurahmanfdev.kotlin_feature_face_recognition.domain.analyzer.FaceDetectionAnalyzer
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions

class FaceDetectionManager : FaceDetectionAnalyzer.Listener {
    private lateinit var faceDetector: FaceDetector
    private var listener: Listener? = null
    lateinit var analyzer: FaceDetectionAnalyzer

    fun initialize() {
        initialize(null)
    }

    fun initialize(listener: Listener?) {
        val option = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()
        faceDetector = FaceDetection.getClient(option)
        analyzer = FaceDetectionAnalyzer(this)
    }


    fun processImage(image: InputImage) {
        faceDetector.process(image).addOnSuccessListener { faces ->
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
        }
    }

    interface Listener {
        fun onFaceDetected(face: Face)
        fun onEmptyFaceDetected()
        fun onFailureDetectedFace(exception: FeatureFaceDetectionException)
    }

    override fun onDetectedFace(inputImage: InputImage) {
        processImage(inputImage)
    }
}
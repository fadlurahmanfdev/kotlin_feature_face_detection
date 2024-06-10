package co.id.fadlurahmanfdev.kotlin_feature_face_recognition.domain.plugin

import android.media.Image
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import co.id.fadlurahmanfdev.kotlin_feature_face_recognition.data.exception.FeatureFaceDetectionException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.lang.Exception

class FaceDetectionManager : OnCompleteListener<MutableList<Face>>, OnFailureListener,
    OnSuccessListener<MutableList<Face>> {
    private lateinit var faceDetector: FaceDetector
    private var listener: Listener? = null
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var currentImageProxy: ImageProxy

    fun initialize() {
        val option = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
        faceDetector = FaceDetection.getClient(option)
    }

    private fun getInputImageFromImageProxy(image: Image, imageProxy: ImageProxy): InputImage {
        return InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
    }

    private var processCountImage = 0
    private var successCountImage = 0
    private var isLeftEyeAlreadyClose: Boolean = false
    private var isRightEyeAlreadyClose: Boolean = false
    private var isBothEyesAlreadyOpen: Boolean = false

    @ExperimentalGetImage
    private val runnableProcessImage = Runnable {
        val image = currentImageProxy.image
        if (image != null) {
            val inputImage =
                getInputImageFromImageProxy(image = image, imageProxy = currentImageProxy)
            faceDetector.process(inputImage).addOnSuccessListener(this).addOnFailureListener(this)
                .addOnCompleteListener(this)
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
        handler.postDelayed(runnableProcessImage, 1500)
    }

    private var livenessListener: LivenessListener? = null

    @ExperimentalGetImage
    fun processLivenessImage(imageProxy: ImageProxy, listener: LivenessListener) {
        if (livenessListener == null) {
            livenessListener = listener
        }
        currentImageProxy = imageProxy
        handler.postDelayed(runnableProcessImage, 1500)
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

    interface LivenessListener {
        fun onShouldCloseLeftEye(eyeIsClosed: Boolean)
        fun onClosedLeftEyeSucceed()
        fun onShouldCloseRightEye(eyeIsClosed: Boolean)

        fun onClosedRightEyeSucceed()
        fun onShouldBothEyesOpen(isRightEyeOpen: Boolean, isLeftEyeOpen: Boolean)
        fun onBothEyesOpenSucceed()
    }

    override fun onSuccess(p0: MutableList<Face>?) {
        val faces = p0 ?: listOf()
        if (faces.isNotEmpty()) {
            if (faces.size == 1) {
                val face = faces.first()
                when {
                    livenessListener != null -> {
                        when {
                            !isLeftEyeAlreadyClose -> {
                                // ini mirroring, rightEyeOpenProbability artinya peluang mata kiri user terbuka
                                val isLeftEyeClose =
                                    (face.rightEyeOpenProbability ?: 1.0f) < 0.1
                                livenessListener?.onShouldCloseLeftEye(isLeftEyeClose)
                                if (isLeftEyeClose) {
                                    successCountImage++
                                    processCountImage = 0
                                    if (successCountImage >= 2) {
                                        successCountImage = 0
                                        isLeftEyeAlreadyClose = true
                                        livenessListener?.onClosedLeftEyeSucceed()
                                    }
                                } else {
                                    processCountImage++
                                }
                            }

                            !isRightEyeAlreadyClose -> {
                                // ini mirroring, leftEyeOpenProbability artinya peluang mata kanan user terbuka
                                val isRightEyeClose =
                                    (face.leftEyeOpenProbability ?: 1.0f) < 0.1
                                livenessListener?.onShouldCloseRightEye(isRightEyeClose)
                                if (isRightEyeClose) {
                                    successCountImage++
                                    processCountImage = 0
                                    if (successCountImage >= 2) {
                                        successCountImage = 0
                                        isRightEyeAlreadyClose = true
                                        livenessListener?.onClosedRightEyeSucceed()
                                    }
                                } else {
                                    processCountImage++
                                }
                            }

                            !isBothEyesAlreadyOpen -> {
                                val isRightEyeOpen = (face.leftEyeOpenProbability ?: 1.0f) > 0.9
                                val isLeftEyeOpen = (face.rightEyeOpenProbability ?: 1.0f) > 0.9
                                livenessListener?.onShouldBothEyesOpen(
                                    isRightEyeOpen = isRightEyeOpen,
                                    isLeftEyeOpen = isLeftEyeOpen
                                )
                                if (isRightEyeOpen && isLeftEyeOpen) {
                                    successCountImage++
                                    processCountImage = 0
                                }
                                if (successCountImage >= 2) {
                                    successCountImage = 0
                                    isBothEyesAlreadyOpen = true
                                    livenessListener?.onBothEyesOpenSucceed()

                                } else {
                                    processCountImage++
                                }
                            }
                        }
                    }

                    listener != null -> {
                        listener?.onFaceDetected(face)
                    }
                }
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

    override fun onFailure(p0: Exception) {
        val exception = FeatureFaceDetectionException(
            code = "ERR_GENERAL",
            message = p0.message
        )
        listener?.onFailureDetectedFace(exception)
    }

    override fun onComplete(p0: Task<MutableList<Face>>) {
        currentImageProxy.close()
    }
}
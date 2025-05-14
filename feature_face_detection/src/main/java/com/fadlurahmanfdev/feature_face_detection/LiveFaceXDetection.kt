package com.fadlurahmanfdev.feature_face_detection

import android.media.Image
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.fadlurahmanfdev.feature_face_detection.core.constant.LiveFaceXExceptionConstant
import com.fadlurahmanfdev.feature_face_detection.core.enums.LiveFaceXDetectionType
import com.fadlurahmanfdev.feature_face_detection.core.enums.LiveFaceXDetectionType.*
import com.fadlurahmanfdev.feature_face_detection.exception.LiveFaceXException
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

class LiveFaceXDetection : OnCompleteListener<MutableList<Face>>, OnFailureListener,
    OnSuccessListener<MutableList<Face>> {
    private lateinit var faceDetector: FaceDetector
    private var captureListener: CaptureListener? = null
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var currentImageProxy: ImageProxy
    private lateinit var processFaceType: LiveFaceXDetectionType

    /**
     * Initialize face detector instance using default performance mode accurate
     * */
    fun initialize() {
        val option = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
            .build()
        initialize(option)
    }

    /**
     * Initialize face detector option as a param
     * @param option face detector options to set performance mode, classification
     * */
    fun initialize(option: FaceDetectorOptions) {
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
            faceDetector.process(inputImage)
                .addOnSuccessListener(this)
                .addOnFailureListener(this)
                .addOnCompleteListener(this)
        } else {
            Log.w(
                LiveFaceXDetection::class.java.simpleName,
                "LiveFaceX-LOG %%% image inside imageProxy is null"
            )
        }
    }

    /**
     * Process single capture face detection. If there is face detected, it showed in [CaptureListener.onFaceDetected]
     * @param imageProxy image proxy get from capture camera result
     * @param callback capture listener
     * */
    fun processImage(imageProxy: ImageProxy, callback: CaptureListener) {
        processFaceType = ONE_SHOT
        if (captureListener == null) {
            this.captureListener = callback
        }
        currentImageProxy = imageProxy
        handler.postDelayed(runnableProcessImage, 500)
    }

    private var livenessListener: LivenessListener? = null

    /**
     * Process single capture face detection. If there is face detected, it showed in [CaptureListener.onFaceDetected]
     * @param imageProxy image proxy get from capture camera result
     * @param listener capture listener
     * */
    fun processLivenessImage(imageProxy: ImageProxy, listener: LivenessListener) {
        processFaceType = LIVENESS
        if (livenessListener == null) {
            livenessListener = listener
        }
        currentImageProxy = imageProxy
        handler.postDelayed(runnableProcessImage, 1000)
    }

    @ExperimentalGetImage
    fun destroy() {
        handler.removeCallbacks(runnableProcessImage)
    }

    interface FaceDetectorListener {
        fun onFailureFaceDetection(imageProxy: ImageProxy, exception: LiveFaceXException)
    }

    interface CaptureListener : FaceDetectorListener {
        /**
         * make sure to close imageProxy
         * */
        fun onFaceDetected(imageProxy: ImageProxy, faces: List<Face>)
    }

    interface LivenessListener : FaceDetectorListener {
        fun onShouldCloseLeftEye(eyeIsClosed: Boolean)
        fun onClosedLeftEyeSucceed()
        fun onShouldCloseRightEye(eyeIsClosed: Boolean)

        fun onClosedRightEyeSucceed()
        fun onShouldBothEyesOpen(isRightEyeOpen: Boolean, isLeftEyeOpen: Boolean)
        fun onBothEyesOpenSucceed(imageProxy: ImageProxy)

        fun onEmptyFaceDetected(imageProxy: ImageProxy)
    }

    private fun processOneShotImage(imageProxy: ImageProxy, faces: List<Face>) {
        captureListener!!.onFaceDetected(imageProxy, faces)
    }

    @ExperimentalGetImage
    private fun processLivenessImage(imageProxy: ImageProxy, faces: List<Face>) {
        val face = faces.firstOrNull()
        if (face == null) {
            livenessListener?.onEmptyFaceDetected(imageProxy)
            return
        }

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
                    livenessListener?.onBothEyesOpenSucceed(imageProxy)
                    handler.removeCallbacks(runnableProcessImage)
                } else {
                    processCountImage++
                }
            }
        }
    }

    override fun onSuccess(p0: MutableList<Face>?) {
        val faces = p0 ?: listOf()
        when (processFaceType) {
            ONE_SHOT -> {
                if (captureListener == null) {
                    Log.w(
                        this::class.java.simpleName,
                        "LiveFaceX-LOG %%% cannot capture listener, captureListener is null"
                    )
                }

                if (captureListener != null) {
                    processOneShotImage(currentImageProxy, faces)
                }
            }

            LIVENESS -> {
                if (livenessListener == null) {
                    Log.w(
                        this::class.java.simpleName,
                        "LiveFaceX-LOG %%% cannot listen liveness, livenessListener is null"
                    )
                }
                if (livenessListener != null) {
                    processLivenessImage(currentImageProxy, faces)
                }
            }
        }
    }

    override fun onFailure(p0: Exception) {
        Log.e(this::class.java.simpleName, "LiveFaceX-LOG %%% failed to face detection: ${p0.message}", p0)
        val exception = LiveFaceXExceptionConstant.UNKNOWN.copy(message = p0.message)
        when (processFaceType) {
            ONE_SHOT -> {
                captureListener?.onFailureFaceDetection(currentImageProxy, exception)
            }

            LIVENESS -> {
                livenessListener?.onFailureFaceDetection(currentImageProxy, exception)
            }
        }
    }

    override fun onComplete(p0: Task<MutableList<Face>>) {
        currentImageProxy.close()
    }
}
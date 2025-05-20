package com.fadlurahmanfdev.livefacex

import android.media.Image
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.fadlurahmanfdev.livefacex.core.constant.LiveFaceXExceptionConstant
import com.fadlurahmanfdev.livefacex.core.enums.LiveFaceXDetectionType
import com.fadlurahmanfdev.livefacex.core.enums.LiveFaceXDetectionType.*
import com.fadlurahmanfdev.livefacex.core.enums.LiveFaceXGesture
import com.fadlurahmanfdev.livefacex.core.enums.LiveFaceXGesture.*
import com.fadlurahmanfdev.livefacex.dto.LiveFaceXEyeBlinked
import com.fadlurahmanfdev.livefacex.dto.LiveFaceXGestureModel
import com.fadlurahmanfdev.livefacex.exception.LiveFaceXException
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
import java.util.Calendar
import java.util.Date

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

    private var listTimeLeftEyeOpened: ArrayList<Date> = arrayListOf()
    private var lastTimeLeftEyeOpened: Date? = null
    private var lastTimeLeftEyeBlinked: Date? = null
    private var leftEyeBlinkCount: Int = 0
    private var listTimeRightEyeOpened: ArrayList<Date> = arrayListOf()
    private var lastTimeRightEyeOpened: Date? = null
    private var lastTimeRightEyeBlinked: Date? = null
    private var rightEyeBlinkCount: Int = 0
    private var listTimeBothEyeOpened: ArrayList<Date> = arrayListOf()
    private var lastTimeBothEyeOpened: Date? = null
    private var lastTimeBothEyeBlinked: Date? = null
    private var bothEyeBlinkCount: Int = 0
    private lateinit var gestures: ArrayList<LiveFaceXGestureModel>
    private var completedGesture: ArrayList<LiveFaceXGesture> = arrayListOf()

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

    private val runnableResetLiveness = Runnable {
        isProcessToReset = false
        livenessListener?.onResetLivenessVerification()


        // reset left eye blinked process
        listTimeLeftEyeOpened.clear()
        lastTimeLeftEyeOpened = null
        lastTimeLeftEyeBlinked = null
        leftEyeBlinkCount = 0

        // reset right eye blinked process
        listTimeRightEyeOpened.clear()
        lastTimeRightEyeOpened = null
        lastTimeRightEyeBlinked = null
        rightEyeBlinkCount = 0

        // reset both eye blinked process
        listTimeBothEyeOpened.clear()
        lastTimeBothEyeOpened = null
        lastTimeBothEyeBlinked = null
        bothEyeBlinkCount = 0

        completedGesture.clear()
    }

    /**
     * Process single capture face detection. If there is face detected, it showed in [CaptureListener.onFaceDetected]
     * @param imageProxy image proxy get from capture camera result
     * @param callback capture listener
     * */
    @ExperimentalGetImage
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
     * @param gestures the gesture allowed to check liveness, at least there is one gesture provided
     * @param listener liveness listener of face detection image
     * */
    @ExperimentalGetImage
    fun processLivenessImage(
        imageProxy: ImageProxy,
        gestures: List<LiveFaceXGestureModel>,
        listener: LivenessListener,
    ) {
        try {
            processFaceType = LIVENESS

            if (gestures.isEmpty()) {
                throw LiveFaceXExceptionConstant.LIVENESS_GESTURE_EMPTY
            }

            this.gestures = arrayListOf<LiveFaceXGestureModel>().apply {
                addAll(gestures)
            }

            if (livenessListener == null) {
                livenessListener = listener
            }
            currentImageProxy = imageProxy
            handler.postDelayed(runnableProcessImage, 300)
        } catch (e: Throwable) {
            Log.wtf(
                this::class.java.simpleName,
                "LiveFaceX-LOG %%% failed to process liveness image",
                e
            )
            if (e is LiveFaceXException) {
                throw e
            }
            throw e
        }
    }

    @ExperimentalGetImage
    fun destroy() {
        try {
            handler.removeCallbacks(runnableProcessImage)
        } catch (e: Throwable) {
            Log.w(
                this::class.java.simpleName,
                "LiveFaceX-LOG %%% failed to remove callback process image"
            )
        }
        try {
            handler.removeCallbacks(runnableResetLiveness)
        } catch (e: Throwable) {
            Log.w(
                this::class.java.simpleName,
                "LiveFaceX-LOG %%% failed to remove callback reset liveness"
            )
        }
    }

    /**
     * Listener to listen face detection
     * */
    interface FaceDetectorListener {
        /**
         * Triggered when the face detector failed to detect image into face
         * */
        fun onFailureFaceDetection(imageProxy: ImageProxy, exception: LiveFaceXException)
    }

    /**
     * Listener for capture camera
     * */
    interface CaptureListener : FaceDetectorListener {
        /**
         * make sure to close imageProxy
         * */
        fun onFaceDetected(imageProxy: ImageProxy, faces: List<Face>)
    }

    /**
     * Listener for detect liveness, action stream, etc
     * */
    interface LivenessListener : FaceDetectorListener {
        /**
         * Triggered when empty face detected
         * @param imageProxy image that being detected by face detector
         * */
        fun onEmptyFaceDetected(imageProxy: ImageProxy)
        /**
         * Triggered when being asked about gesture liveness verification.
         * @param gesture gesture related liveness
         * */
        fun onAskedLivenessVerification(gesture: LiveFaceXGesture)
        /**
         * Triggered when gesture successfully verified
         * @param gesture gesture related liveness
         * @param imageProxy image that being detected by face detector
         * */
        fun onAskedLivenessVerificationSucceed(gesture: LiveFaceXGesture, imageProxy: ImageProxy)
        /**
         * Triggered when gesture successfully verified
         * @param imageProxy image that being detected by face detector
         * */
        fun onSuccessfullyLivenessVerification(imageProxy: ImageProxy)
        /**
         * Triggered when liveness verification must be reset
         * */
        fun onResetLivenessVerification()
    }

    private fun processOneShotImage(imageProxy: ImageProxy, faces: List<Face>) {
        captureListener!!.onFaceDetected(imageProxy, faces)
    }

    @ExperimentalGetImage
    private fun processLivenessImageResult(
        imageProxy: ImageProxy,
        gestures: List<LiveFaceXGestureModel>,
        faces: List<Face>,
    ) {
        resetLiveness(6000)
        val face = faces.firstOrNull()
        if (face == null) {
            livenessListener?.onEmptyFaceDetected(imageProxy)
            return
        }

        var gesture = gestures.firstOrNull { gesture ->
            !completedGesture.contains(gesture.type)
        }

        when (gesture?.type) {
            LEFT_EYE_BLINK -> {
                val eyeBlinkGesture = gesture as LiveFaceXEyeBlinked
                livenessListener?.onAskedLivenessVerification(eyeBlinkGesture.type)

                // check threshold whether eyes is blinked or not
                val blinkedThreshold = gesture.blinkedThreshold
                // convert into eye open threshold
                // e.g., if the blinked threshold is 0.8, then eye open probability threshold is 0.2
                val eyeOpenThreshold = 1.0 - blinkedThreshold
                val leftEyeBlinked =
                    (face.rightEyeOpenProbability ?: 1.0f) < eyeOpenThreshold
                val leftEyeOpened = !leftEyeBlinked
                val rightEyeBlinked =
                    (face.leftEyeOpenProbability ?: 1.0f) < eyeOpenThreshold
                val rightEyeOpened = !rightEyeBlinked


                if (leftEyeOpened && rightEyeOpened && lastTimeLeftEyeBlinked != null && lastTimeLeftEyeOpened != null) {
                    // check at the final step, whether the left eye already open again
                    val currentTime = Calendar.getInstance().time
                    if ((currentTime.time - lastTimeLeftEyeOpened!!.time) < 1500) {
                        stopResetLiveness()
                        livenessListener?.onAskedLivenessVerificationSucceed(
                            gesture.type,
                            imageProxy
                        )
                        leftEyeBlinkCount++
                        listTimeLeftEyeOpened.clear()
                        lastTimeLeftEyeOpened = null
                        lastTimeLeftEyeBlinked = null
                    }
                } else if (leftEyeBlinked && rightEyeOpened && listTimeLeftEyeOpened.isNotEmpty()) {
                    // check at second step, left eye should be closed, and the right eye should be opened
                    lastTimeLeftEyeOpened = listTimeLeftEyeOpened.last()
                    lastTimeLeftEyeBlinked = Calendar.getInstance().time
                } else if (leftEyeOpened && rightEyeOpened) {
                    // check at first step, both eye should be open
                    listTimeLeftEyeOpened.add(Calendar.getInstance().time)
                }

                if (leftEyeBlinkCount >= gesture.blinkedThresholdCount) {
                    completedGesture.add(gesture.type)
                    livenessListener?.onAskedLivenessVerificationSucceed(gesture.type, imageProxy)

                    leftEyeBlinkCount = 0
                    lastTimeLeftEyeOpened = null
                    lastTimeLeftEyeBlinked = null
                }
            }

            RIGHT_EYE_BLINK -> {
                val eyeBlinkGesture = gesture as LiveFaceXEyeBlinked
                livenessListener?.onAskedLivenessVerification(eyeBlinkGesture.type)

                // check threshold whether eyes called blinked or not
                val blinkedThreshold = gesture.blinkedThreshold
                // convert into eye open threshold, if blinked threshold is 0.8
                // then eye open probability threshold is 0.2
                val eyeOpenThreshold = 1.0 - blinkedThreshold
                val rightEyeBlinked =
                    (face.leftEyeOpenProbability ?: 1.0f) < eyeOpenThreshold
                val rightEyeOpened = !rightEyeBlinked
                val leftEyeBlinked =
                    (face.rightEyeOpenProbability ?: 1.0f) < eyeOpenThreshold
                val leftEyeOpened = !leftEyeBlinked


                if (rightEyeOpened && leftEyeOpened && lastTimeRightEyeBlinked != null && lastTimeRightEyeOpened != null) {
                    val currentTime = Calendar.getInstance().time
                    if ((currentTime.time - lastTimeRightEyeOpened!!.time) < 1500) {
                        stopResetLiveness()
                        livenessListener?.onAskedLivenessVerificationSucceed(
                            gesture.type,
                            imageProxy
                        )
                        rightEyeBlinkCount++
                        listTimeRightEyeOpened.clear()
                        lastTimeRightEyeOpened = null
                        lastTimeRightEyeBlinked = null
                    }
                } else if (rightEyeBlinked && leftEyeOpened && listTimeRightEyeOpened.isNotEmpty()) {
                    lastTimeRightEyeOpened = listTimeRightEyeOpened.last()
                    lastTimeRightEyeBlinked = Calendar.getInstance().time
                } else if (rightEyeOpened && leftEyeOpened) {
                    listTimeRightEyeOpened.add(Calendar.getInstance().time)
                }

                if (rightEyeBlinkCount >= gesture.blinkedThresholdCount) {
                    completedGesture.add(gesture.type)
                    livenessListener?.onAskedLivenessVerificationSucceed(gesture.type, imageProxy)

                    rightEyeBlinkCount = 0
                    lastTimeRightEyeOpened = null
                    lastTimeRightEyeBlinked = null
                }
            }

            BLINK -> {
                val eyeBlinkGesture = gesture as LiveFaceXEyeBlinked
                livenessListener?.onAskedLivenessVerification(eyeBlinkGesture.type)

                // check threshold whether eyes called blinked or not
                val blinkedThreshold = gesture.blinkedThreshold
                // convert into eye open threshold, if blinked threshold is 0.8
                // then eye open probability threshold is 0.2
                val eyeOpenThreshold = 1.0 - blinkedThreshold
                val rightEyeBlinked =
                    (face.leftEyeOpenProbability ?: 1.0f) < eyeOpenThreshold
                val rightEyeOpened = !rightEyeBlinked
                val leftEyeBlinked =
                    (face.rightEyeOpenProbability ?: 1.0f) < eyeOpenThreshold
                val leftEyeOpened = !leftEyeBlinked


                if (rightEyeOpened && leftEyeOpened && lastTimeBothEyeBlinked != null && lastTimeBothEyeOpened != null) {
                    val currentTime = Calendar.getInstance().time
                    if ((currentTime.time - lastTimeBothEyeOpened!!.time) < 1500) {
                        stopResetLiveness()
                        livenessListener?.onAskedLivenessVerificationSucceed(
                            gesture.type,
                            imageProxy
                        )
                        bothEyeBlinkCount++
                        listTimeBothEyeOpened.clear()
                        lastTimeBothEyeOpened = null
                        lastTimeBothEyeBlinked = null
                    }
                } else if (rightEyeBlinked && leftEyeBlinked && listTimeBothEyeOpened.isNotEmpty()) {
                    lastTimeBothEyeOpened = listTimeBothEyeOpened.last()
                    lastTimeBothEyeBlinked = Calendar.getInstance().time
                } else if (rightEyeOpened && leftEyeOpened) {
                    listTimeBothEyeOpened.add(Calendar.getInstance().time)
                }

                if (bothEyeBlinkCount >= gesture.blinkedThresholdCount) {
                    completedGesture.add(gesture.type)
                    livenessListener?.onAskedLivenessVerificationSucceed(gesture.type, imageProxy)

                    bothEyeBlinkCount = 0
                    lastTimeBothEyeOpened = null
                    lastTimeBothEyeBlinked = null
                }
            }

            null -> {}
        }

        gesture = gestures.firstOrNull { gestureLeft ->
            !completedGesture.contains(gestureLeft.type)
        }

        if (gesture == null) {
            livenessListener?.onSuccessfullyLivenessVerification(imageProxy)
        }
    }

    private var isProcessToReset: Boolean = false
    private fun resetLiveness(delayedInMillisecond: Long) {
        if (isProcessToReset) return
        if (completedGesture.isEmpty()) return
        isProcessToReset = true
        handler.postDelayed(runnableResetLiveness, delayedInMillisecond)
    }

    private fun stopResetLiveness() {
        if (isProcessToReset) {
            isProcessToReset = false
            handler.removeCallbacks(runnableResetLiveness)
        }
    }

    @ExperimentalGetImage
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
                    processLivenessImageResult(
                        currentImageProxy,
                        gestures = gestures,
                        faces = faces
                    )
                }
            }
        }
    }

    override fun onFailure(p0: Exception) {
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
package co.id.fadlurahmanfdev.kotlinfeaturefacedetection.presentation

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import co.id.fadlurahmanfdev.kotlin_feature_camera.data.enums.FeatureCameraFacing
import co.id.fadlurahmanfdev.kotlin_feature_camera.data.enums.FeatureCameraPurpose
import co.id.fadlurahmanfdev.kotlin_feature_camera.domain.common.BaseCameraActivity
import co.id.fadlurahmanfdev.kotlin_feature_face_recognition.domain.plugin.FaceDetectionManager
import co.id.fadlurahmanfdev.kotlinfeaturefacedetection.R

class SingleProcessLivenessFaceDetectionActivity : BaseCameraActivity(),
    BaseCameraActivity.AnalyzeListener,
    FaceDetectionManager.LivenessListener {
    lateinit var cameraPreview: PreviewView
    lateinit var ivFlash: ImageView
    lateinit var ivCamera: ImageView
    lateinit var ivSwitch: ImageView
    lateinit var faceDetectionManager: FaceDetectionManager

    override var cameraFacing: FeatureCameraFacing = FeatureCameraFacing.FRONT
    override var cameraPurpose: FeatureCameraPurpose = FeatureCameraPurpose.IMAGE_ANALYSIS

    override fun onAfterBindCameraToView() {}

    override fun onCreateBaseCamera(savedInstanceState: Bundle?) {
        faceDetectionManager = FaceDetectionManager()
        faceDetectionManager.initialize()
    }

    @ExperimentalGetImage
    override fun onStartCreateBaseCamera(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_single_process_face_detection)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        cameraPreview = findViewById<PreviewView>(R.id.cameraPreview)
        ivFlash = findViewById<ImageView>(R.id.iv_flash)
        ivCamera = findViewById<ImageView>(R.id.iv_camera)
        ivSwitch = findViewById<ImageView>(R.id.iv_switch_camera)
        addAnalyzeListener(this)

        ivCamera.setOnClickListener {
            startAnalyze { imageProxy ->
                faceDetectionManager.processLivenessImage(imageProxy, this)
            }
        }
    }

    override fun setSurfaceProviderBaseCamera(preview: Preview) {
        preview.setSurfaceProvider(cameraPreview.surfaceProvider)
    }

    override fun isTorchChanged(isTorch: Boolean) {

    }

    override fun onStartAnalyze() {
        ivCamera.visibility = View.INVISIBLE
    }

    override fun onStopAnalyze() {
        TODO("Not yet implemented")
    }

    override fun onShouldCloseLeftEye(eyeIsClosed: Boolean) {
        println("ON SHOULD CLOSE LEFT EYE, EYE IS CLOSED: $eyeIsClosed")
    }

    override fun onClosedLeftEyeSucceed() {
        println("LEFT EYE CLOSE SUCCEED")
    }

    override fun onShouldCloseRightEye(eyeIsClosed: Boolean) {
        println("ON SHOULD CLOSE RIGHT EYE, RIGHT EYE CLOSED: $eyeIsClosed")
    }

    override fun onClosedRightEyeSucceed() {
        println("RIGHT EYE CLOSE SUCCEED")
    }

    override fun onShouldBothEyesOpen(isRightEyeOpen: Boolean, isLeftEyeOpen: Boolean) {
        println("ASK BOTH EYE KEEP OPEN: RIGHT OPEN: $isRightEyeOpen, LEFT EYE OPEN: $isLeftEyeOpen")
    }

    override fun onBothEyesOpenSucceed() {
        println("MASUK SUKSES BOTH EYES OPEN")
    }

}
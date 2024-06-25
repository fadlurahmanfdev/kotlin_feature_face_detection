package co.id.fadlurahmanfdev.kotlinfeaturefacedetection.presentation

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import co.id.fadlurahmanfdev.kotlin_feature_camera.data.enums.FeatureCameraFacing
import co.id.fadlurahmanfdev.kotlin_feature_camera.data.enums.FeatureCameraPurpose
import co.id.fadlurahmanfdev.kotlin_feature_camera.domain.common.BaseCameraActivity
import co.id.fadlurahmanfdev.kotlin_feature_camera.other.utility.FeatureCameraUtility
import co.id.fadlurahmanfdev.kotlin_feature_face_recognition.domain.plugin.FaceDetectionManager
import co.id.fadlurahmanfdev.kotlinfeaturefacedetection.R

class SingleProcessLivenessFaceDetectionActivity : BaseCameraActivity(),
    BaseCameraActivity.AnalyzeListener,
    FaceDetectionManager.LivenessListener {
    lateinit var cameraPreview: PreviewView
    lateinit var ivFlash: ImageView
    lateinit var ivCamera: ImageView
    lateinit var ivStopCamera: ImageView
    lateinit var ivSwitch: ImageView
    lateinit var tvGuide: TextView
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
        ivStopCamera = findViewById<ImageView>(R.id.iv_stop_camera)
        ivSwitch = findViewById<ImageView>(R.id.iv_switch_camera)
        tvGuide = findViewById(R.id.tv_guide)
        addAnalyzeListener(this)

        ivCamera.setOnClickListener {
            startAnalyze { imageProxy ->
                faceDetectionManager.processLivenessImage(imageProxy, this)
            }
        }

        ivStopCamera.setOnClickListener {
            stopAnalyze()
        }
    }

    override fun setSurfaceProviderBaseCamera(preview: Preview) {
        preview.setSurfaceProvider(cameraPreview.surfaceProvider)
    }

    override fun isTorchChanged(isTorch: Boolean) {

    }

    override fun onStartAnalyze() {
        tvGuide.visibility = View.VISIBLE
        ivCamera.visibility = View.GONE
        ivStopCamera.visibility = View.VISIBLE
    }

    override fun onStopAnalyze() {
        tvGuide.visibility = View.GONE
        ivCamera.visibility = View.VISIBLE
        ivStopCamera.visibility = View.GONE
    }

    override fun onShouldCloseLeftEye(eyeIsClosed: Boolean) {
        println("ON SHOULD CLOSE LEFT EYE, EYE IS CLOSED: $eyeIsClosed")
        tvGuide.text = "TUTUP MATA SEBELAH KIRI"
    }

    override fun onClosedLeftEyeSucceed() {
        println("LEFT EYE CLOSE SUCCEED")
    }

    override fun onShouldCloseRightEye(eyeIsClosed: Boolean) {
        println("ON SHOULD CLOSE RIGHT EYE, RIGHT EYE CLOSED: $eyeIsClosed")
        tvGuide.text = "TUTUP MATA SEBELAH KANAN"
    }

    override fun onClosedRightEyeSucceed() {
        println("RIGHT EYE CLOSE SUCCEED")
    }

    override fun onShouldBothEyesOpen(isRightEyeOpen: Boolean, isLeftEyeOpen: Boolean) {
        println("ASK BOTH EYE KEEP OPEN: RIGHT OPEN: $isRightEyeOpen, LEFT EYE OPEN: $isLeftEyeOpen")
        tvGuide.text = "BUKA KEDUA MATA"
    }

    override fun onBothEyesOpenSucceed(imageProxy: ImageProxy) {
        println("MASUK SUKSES BOTH EYES OPEN")
        stopAnalyze()
        FeatureCameraUtility.bitmapImage = FeatureCameraUtility.getBitmapFromImageProxy(imageProxy)
        val intent = Intent(this, PreviewFaceImageActivity::class.java)
        intent.apply {
            putExtra("FLOW", "LIVENESS")
        }
        startActivity(intent)
    }

}
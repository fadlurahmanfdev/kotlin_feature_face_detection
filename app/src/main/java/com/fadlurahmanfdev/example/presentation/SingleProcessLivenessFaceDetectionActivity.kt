package com.fadlurahmanfdev.example.presentation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fadlurahmanfdev.feature_face_detection.core.exception.FeatureFaceDetectionException
import com.fadlurahmanfdev.feature_face_detection.FeatureFaceDetection
import com.fadlurahmanfdev.example.R
import com.fadlurahmanfdev.example.data.SharedModel
import com.fadlurahmanfdev.kotlin_feature_camera.data.enums.FeatureCameraPurpose
import com.fadlurahmanfdev.kotlin_feature_camera.data.repository.FeatureCameraRepository
import com.fadlurahmanfdev.kotlin_feature_camera.data.repository.FeatureCameraRepositoryImpl
import com.fadlurahmanfdev.kotlin_feature_camera.domain.common.BaseCameraActivity

class SingleProcessLivenessFaceDetectionActivity : BaseCameraActivity(),
    FeatureFaceDetection.LivenessListener {
    lateinit var cameraPreview: PreviewView
    lateinit var ivFlash: ImageView
    lateinit var ivCamera: ImageView
    lateinit var ivStopCamera: ImageView
    lateinit var ivSwitch: ImageView
    lateinit var tvGuide: TextView
    lateinit var featureFaceDetection: FeatureFaceDetection

    override var cameraSelector: CameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
    override var cameraPurpose: FeatureCameraPurpose = FeatureCameraPurpose.IMAGE_ANALYSIS

    lateinit var cameraRepository: FeatureCameraRepository


    @ExperimentalGetImage
    override fun onCreateBaseCamera(savedInstanceState: Bundle?) {
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

        featureFaceDetection = FeatureFaceDetection()
        featureFaceDetection.initialize()

        cameraRepository = FeatureCameraRepositoryImpl()

        ivCamera.setOnClickListener {
            startAnalyze { imageProxy ->
                tvGuide.visibility = View.VISIBLE
                ivCamera.visibility = View.GONE
                ivStopCamera.visibility = View.VISIBLE
                featureFaceDetection.processLivenessImage(imageProxy, this)
            }
        }

        ivStopCamera.setOnClickListener {
//            tvGuide.visibility = View.GONE
//            ivCamera.visibility = View.VISIBLE
//            ivStopCamera.visibility = View.GONE
//            stopAnalyze()
        }
    }

    override fun setSurfaceProviderBaseCamera(preview: Preview) {
        preview.setSurfaceProvider(cameraPreview.surfaceProvider)
    }

//    override fun onStartAnalyze() {
//        tvGuide.visibility = View.VISIBLE
//        ivCamera.visibility = View.GONE
//        ivStopCamera.visibility = View.VISIBLE
//    }
//
//    override fun onStopAnalyze() {
//        tvGuide.visibility = View.GONE
//        ivCamera.visibility = View.VISIBLE
//        ivStopCamera.visibility = View.GONE
//    }

    override fun onShouldCloseLeftEye(eyeIsClosed: Boolean) {
        Log.d(this::class.java.simpleName, "whether user should close left eye")
        tvGuide.text = "TUTUP MATA SEBELAH KIRI"
    }

    override fun onClosedLeftEyeSucceed() {
        Log.d(this::class.java.simpleName, "successfully closed left eye")
    }

    override fun onShouldCloseRightEye(eyeIsClosed: Boolean) {
        Log.d(this::class.java.simpleName, "whether user should close right eye")
        tvGuide.text = "TUTUP MATA SEBELAH KANAN"
    }

    override fun onClosedRightEyeSucceed() {
        Log.d(this::class.java.simpleName, "successfully closed right eye")
    }

    override fun onShouldBothEyesOpen(isRightEyeOpen: Boolean, isLeftEyeOpen: Boolean) {
        Log.d(this::class.java.simpleName, "whether user open both eyes")
        tvGuide.text = "BUKA KEDUA MATA"
    }

    override fun onBothEyesOpenSucceed(imageProxy: ImageProxy) {
        Log.d(this::class.java.simpleName, "successfully closed both eyes")
        tvGuide.visibility = View.GONE
        ivCamera.visibility = View.VISIBLE
        ivStopCamera.visibility = View.GONE
        stopAnalyze()
        SharedModel.bitmap = cameraRepository.getBitmapFromImageProxy(imageProxy)
        val intent = Intent(this, PreviewFaceImageActivity::class.java)
        intent.apply {
            putExtra("FLOW", "LIVENESS")
        }
        startActivity(intent)
    }

    override fun onEmptyFaceDetected(imageProxy: ImageProxy) {}

    override fun onFailureFaceDetection(
        imageProxy: ImageProxy,
        exception: FeatureFaceDetectionException
    ) {}

}
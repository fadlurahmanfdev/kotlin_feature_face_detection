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
import com.fadlurahmanfdev.feature_face_detection.exception.LiveFaceXException
import com.fadlurahmanfdev.feature_face_detection.LiveFaceXDetection
import com.fadlurahmanfdev.example.R
import com.fadlurahmanfdev.example.data.SharedModel
import com.fadlurahmanfdev.feature_face_detection.core.enums.LiveFaceXGesture
import com.fadlurahmanfdev.feature_face_detection.core.enums.LiveFaceXGesture.*
import com.fadlurahmanfdev.feature_face_detection.dto.LiveFaceXEyeBlinked
import com.fadlurahmanfdev.kotlin_feature_camera.data.enums.FeatureCameraPurpose
import com.fadlurahmanfdev.kotlin_feature_camera.data.repository.FeatureCameraRepository
import com.fadlurahmanfdev.kotlin_feature_camera.data.repository.FeatureCameraRepositoryImpl
import com.fadlurahmanfdev.kotlin_feature_camera.domain.common.BaseCameraActivity

class SingleProcessLivenessFaceDetectionActivity : BaseCameraActivity(),
    LiveFaceXDetection.LivenessListener {
    lateinit var cameraPreview: PreviewView
    lateinit var ivFlash: ImageView
    lateinit var ivCamera: ImageView
    lateinit var ivStopCamera: ImageView
    lateinit var ivSwitch: ImageView
    lateinit var tvGuide: TextView
    lateinit var liveFaceXDetection: LiveFaceXDetection

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

        liveFaceXDetection = LiveFaceXDetection()
        liveFaceXDetection.initialize()

        cameraRepository = FeatureCameraRepositoryImpl()

        ivCamera.setOnClickListener {
            startAnalyze { imageProxy ->
                tvGuide.visibility = View.VISIBLE
                ivCamera.visibility = View.GONE
                ivStopCamera.visibility = View.VISIBLE
                liveFaceXDetection.processLivenessImage(
                    imageProxy = imageProxy, listOf(
                        LiveFaceXEyeBlinked(
                            type = LEFT_EYE_BLINK,
                            blinkedThreshold = 0.8,
                            blinkedThresholdCount = 2,
                        ),
                        LiveFaceXEyeBlinked(
                            type = RIGHT_EYE_BLINK,
                            blinkedThreshold = 0.8,
                            blinkedThresholdCount = 2,
                        ),
                        LiveFaceXEyeBlinked(
                            type = BLINK,
                            blinkedThreshold = 0.8,
                            blinkedThresholdCount = 2,
                        ),
                    ), this
                )
            }
        }

        ivStopCamera.setOnClickListener {
            tvGuide.visibility = View.GONE
            ivCamera.visibility = View.VISIBLE
            ivStopCamera.visibility = View.GONE
            stopAnalyze()
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

    override fun onAskedLivenessVerification(gesture: LiveFaceXGesture) {
        when (gesture) {
            LEFT_EYE_BLINK -> {
                tvGuide.text = "KEDIPKAN MATA SEBELAH KIRI"
            }

            RIGHT_EYE_BLINK -> {
                tvGuide.text = "KEDIPKAN MATA SEBELAH KANAN"
            }

            BLINK -> {
                tvGuide.text = "KEDIPKAN KEDUA BELAH MATA"
            }
        }
    }

    override fun onResetLivenessVerification() {
        tvGuide.text = "RESET LIVENESS"
    }

    override fun onAskedLivenessVerificationSucceed(
        gesture: LiveFaceXGesture,
        imageProxy: ImageProxy
    ) {
        tvGuide.text = "BERHASIL"
    }

    override fun onEmptyFaceDetected(imageProxy: ImageProxy) {
        tvGuide.text = "HADAPKAN MUKA KE KAMERA"
    }

    override fun onSuccessfullyLivenessVerification(imageProxy: ImageProxy) {
        tvGuide.visibility = View.GONE
        ivCamera.visibility = View.VISIBLE
        ivStopCamera.visibility = View.GONE
        stopAnalyze()
        var bitmapImage = cameraRepository.getBitmapFromImageProxy(imageProxy)
        if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
            bitmapImage = cameraRepository.mirrorHorizontalBitmap(bitmapImage)
        }
        SharedModel.bitmap = bitmapImage
        val intent = Intent(this, PreviewFaceImageActivity::class.java)
        intent.apply {
            putExtra("FLOW", "LIVENESS")
        }
        startActivity(intent)
    }

    override fun onFailureFaceDetection(
        imageProxy: ImageProxy,
        exception: LiveFaceXException,
    ) {}

}
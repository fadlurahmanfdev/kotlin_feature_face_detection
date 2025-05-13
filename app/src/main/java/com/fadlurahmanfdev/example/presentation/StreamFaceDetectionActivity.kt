package com.fadlurahmanfdev.example.presentation

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fadlurahmanfdev.feature_face_detection.exception.LiveFaceXException
import com.fadlurahmanfdev.feature_face_detection.FeatureFaceDetection
import com.fadlurahmanfdev.example.R
import com.fadlurahmanfdev.kotlin_feature_camera.data.enums.FeatureCameraPurpose
import com.fadlurahmanfdev.kotlin_feature_camera.domain.common.BaseCameraActivity
import com.google.mlkit.vision.face.Face

class StreamFaceDetectionActivity : BaseCameraActivity(), FeatureFaceDetection.CaptureListener {
    lateinit var cameraPreview: PreviewView
    lateinit var ivFlash: ImageView
    lateinit var ivCamera: ImageView
    lateinit var ivStopCamera: ImageView
    lateinit var ivSwitch: ImageView
    lateinit var tvResult: TextView
    lateinit var featureFaceDetection: FeatureFaceDetection

    override var cameraSelector: CameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
    override var cameraPurpose: FeatureCameraPurpose = FeatureCameraPurpose.IMAGE_ANALYSIS

    override fun onCreateBaseCamera(savedInstanceState: Bundle?) {
        setContentView(R.layout.activity_stream_face_detection)
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
        tvResult = findViewById(R.id.tv_result)

        featureFaceDetection = FeatureFaceDetection()
        featureFaceDetection.initialize()

        ivCamera.setOnClickListener {
            startAnalyze { imageProxy ->
                featureFaceDetection.processImage(imageProxy, this)
            }
        }

        ivStopCamera.setOnClickListener {
            stopAnalyze()
        }
    }

    override fun setSurfaceProviderBaseCamera(preview: Preview) {
        preview.setSurfaceProvider(cameraPreview.surfaceProvider)
    }

    override fun onFaceDetected(imageProxy: ImageProxy, faces: List<Face>) {

    }

//    override fun onFaceDetected(imageProxy: ImageProxy, face: Face) {
//        tvResult.visibility = View.VISIBLE
//        tvResult.text = "SMILING PROBABILITY: ${face.smilingProbability}" +
//                "\nLEFT EYE OPEN PROBABILITY: ${face.leftEyeOpenProbability}" +
//                "\nRIGHT EYE OPEN PROBABILITY: ${face.rightEyeOpenProbability}"
//        imageProxy.close()
//    }
//
//    override fun onEmptyFaceDetected(imageProxy: ImageProxy) {
//        tvResult.visibility = View.VISIBLE
//        tvResult.text = "NO FACE DETECTED"
//        imageProxy.close()
//    }

    override fun onFailureFaceDetection(
        imageProxy: ImageProxy,
        exception: LiveFaceXException
    ) {
        tvResult.visibility = View.VISIBLE
        tvResult.text = "FAILURE DETECTED FACE ${exception.message} & ${exception.code}"
        imageProxy.close()
    }

}
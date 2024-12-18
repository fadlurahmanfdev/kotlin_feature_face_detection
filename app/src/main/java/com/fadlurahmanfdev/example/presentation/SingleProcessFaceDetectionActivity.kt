package com.fadlurahmanfdev.example.presentation

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
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
import com.fadlurahmanfdev.kotlin_feature_camera.data.exception.FeatureCameraException
import com.fadlurahmanfdev.kotlin_feature_camera.data.repository.FeatureCameraRepository
import com.fadlurahmanfdev.kotlin_feature_camera.data.repository.FeatureCameraRepositoryImpl
import com.fadlurahmanfdev.kotlin_feature_camera.domain.common.BaseCameraActivity
import com.fadlurahmanfdev.kotlin_feature_camera.domain.listener.CameraCaptureListener
import com.google.mlkit.vision.face.Face

class SingleProcessFaceDetectionActivity : BaseCameraActivity(),
    FeatureFaceDetection.CaptureListener {
    lateinit var cameraPreview: PreviewView
    lateinit var ivFlash: ImageView
    lateinit var ivCamera: ImageView
    lateinit var ivSwitch: ImageView
    lateinit var featureFaceDetection: FeatureFaceDetection
    override var cameraSelector: CameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
    override var cameraPurpose: FeatureCameraPurpose = FeatureCameraPurpose.IMAGE_CAPTURE
    lateinit var cameraRepository: FeatureCameraRepository

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
        ivSwitch = findViewById<ImageView>(R.id.iv_switch_camera)
        featureFaceDetection = FeatureFaceDetection()
        featureFaceDetection.initialize()

        cameraRepository = FeatureCameraRepositoryImpl()

        ivCamera.setOnClickListener {
            takePicture(object : CameraCaptureListener {
                override fun onCaptureError(exception: FeatureCameraException) {

                }

                @ExperimentalGetImage
                override fun onCaptureSuccess(
                    imageProxy: ImageProxy,
                    cameraSelector: CameraSelector
                ) {
                    featureFaceDetection.processImage(
                        imageProxy,
                        this@SingleProcessFaceDetectionActivity
                    )
                }
            })
        }
    }

    override fun setSurfaceProviderBaseCamera(preview: Preview) {
        preview.setSurfaceProvider(cameraPreview.surfaceProvider)
    }

//    @ExperimentalGetImage
//    override fun onCaptureSuccess(imageProxy: ImageProxy) {
//        faceDetectionManager.processImage(imageProxy, this)
//    }

    override fun onFaceDetected(imageProxy: ImageProxy, faces: List<Face>) {
        Log.d(this::class.java.simpleName, "successfully detected face: ${faces.size}")
        var bitmapImage = cameraRepository.getBitmapFromImageProxy(imageProxy)
        if (cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
            bitmapImage = cameraRepository.mirrorHorizontalBitmap(bitmapImage)
        }
        println("MASUK BITMAP IMAGE WIDTH: ${bitmapImage.width}")
        println("MASUK BITMAP IMAGE HEIGHT: ${bitmapImage.height}")
        SharedModel.bitmap = bitmapImage
        if (faces.isEmpty()) {
            Log.d(this::class.java.simpleName, "no face detected")
            return
        }

        val face = faces.first()
        val intent = Intent(this, PreviewFaceImageActivity::class.java)
        intent.apply {
            putExtra("FLOW", "CAPTURE")
            putExtra("SMILING_PROBABILITY", face.smilingProbability)
            putExtra("LEFT_EYE_OPEN_PROBABILITY", face.leftEyeOpenProbability)
            putExtra("RIGHT_EYE_OPEN_PROBABILITY", face.rightEyeOpenProbability)
        }
        startActivity(intent)
    }

    override fun onFailureFaceDetection(
        imageProxy: ImageProxy,
        exception: FeatureFaceDetectionException
    ) {
        Log.d(this::class.java.simpleName, "empty failure face detection: ${exception.code}")
        finish()
    }

}
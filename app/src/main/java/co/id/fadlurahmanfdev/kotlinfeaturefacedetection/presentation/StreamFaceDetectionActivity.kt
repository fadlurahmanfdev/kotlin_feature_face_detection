package co.id.fadlurahmanfdev.kotlinfeaturefacedetection.presentation

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
import co.id.fadlurahmanfdev.kotlin_feature_face_recognition.data.exception.FeatureFaceDetectionException
import co.id.fadlurahmanfdev.kotlin_feature_face_recognition.domain.plugin.FaceDetectionManager
import co.id.fadlurahmanfdev.kotlinfeaturefacedetection.R
import com.google.mlkit.vision.face.Face

class StreamFaceDetectionActivity : BaseCameraActivity(),
    BaseCameraActivity.AnalyzeListener, FaceDetectionManager.CaptureListener {
    lateinit var cameraPreview: PreviewView
    lateinit var ivFlash: ImageView
    lateinit var ivCamera: ImageView
    lateinit var ivStopCamera: ImageView
    lateinit var ivSwitch: ImageView
    lateinit var tvResult: TextView
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
        addAnalyzeListener(this)

        ivCamera.setOnClickListener {
            startAnalyze { imageProxy ->
                faceDetectionManager.processImage(imageProxy, this)
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
        ivCamera.visibility = View.GONE
        ivStopCamera.visibility = View.VISIBLE
    }

    override fun onStopAnalyze() {
        ivCamera.visibility = View.VISIBLE
        ivStopCamera.visibility = View.GONE
    }

    override fun onFaceDetected(imageProxy: ImageProxy, face: Face) {
        tvResult.visibility = View.VISIBLE
        tvResult.text = "SMILING PROBABILITY: ${face.smilingProbability}" +
                "\nLEFT EYE OPEN PROBABILITY: ${face.leftEyeOpenProbability}" +
                "\nRIGHT EYE OPEN PROBABILITY: ${face.rightEyeOpenProbability}"
        imageProxy.close()
    }

    override fun onEmptyFaceDetected(imageProxy: ImageProxy) {
        tvResult.visibility = View.VISIBLE
        tvResult.text = "NO FACE DETECTED"
        imageProxy.close()
    }

    override fun onFailureDetectedFace(
        imageProxy: ImageProxy,
        exception: FeatureFaceDetectionException
    ) {
        tvResult.visibility = View.VISIBLE
        tvResult.text = "FAILURE DETECTED FACE ${exception.message} & ${exception.code}"
        imageProxy.close()
    }

}
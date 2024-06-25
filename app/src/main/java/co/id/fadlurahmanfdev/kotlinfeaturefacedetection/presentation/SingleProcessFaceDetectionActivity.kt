package co.id.fadlurahmanfdev.kotlinfeaturefacedetection.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import co.id.fadlurahmanfdev.kotlin_feature_camera.data.enums.FeatureCameraFacing
import co.id.fadlurahmanfdev.kotlin_feature_camera.data.enums.FeatureCameraPurpose
import co.id.fadlurahmanfdev.kotlin_feature_camera.data.exception.FeatureCameraException
import co.id.fadlurahmanfdev.kotlin_feature_camera.domain.common.BaseCameraActivity
import co.id.fadlurahmanfdev.kotlin_feature_camera.other.utility.FeatureCameraUtility
import co.id.fadlurahmanfdev.kotlin_feature_face_recognition.data.exception.FeatureFaceDetectionException
import co.id.fadlurahmanfdev.kotlin_feature_face_recognition.domain.plugin.FaceDetectionManager
import co.id.fadlurahmanfdev.kotlinfeaturefacedetection.R
import com.google.mlkit.vision.face.Face

class SingleProcessFaceDetectionActivity : BaseCameraActivity(), BaseCameraActivity.CaptureListener,
    FaceDetectionManager.CaptureListener {
    lateinit var cameraPreview: PreviewView
    lateinit var ivFlash: ImageView
    lateinit var ivCamera: ImageView
    lateinit var ivSwitch: ImageView
    lateinit var faceDetectionManager: FaceDetectionManager
    override var cameraFacing: FeatureCameraFacing = FeatureCameraFacing.FRONT
    override var cameraPurpose: FeatureCameraPurpose = FeatureCameraPurpose.IMAGE_CAPTURE

    override fun onAfterBindCameraToView() {}

    override fun onCreateBaseCamera(savedInstanceState: Bundle?) {
        faceDetectionManager = FaceDetectionManager()
        faceDetectionManager.initialize()
    }

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

        ivCamera.setOnClickListener {
            takePicture()
        }

        addCaptureListener(this)
    }

    override fun setSurfaceProviderBaseCamera(preview: Preview) {
        preview.setSurfaceProvider(cameraPreview.surfaceProvider)
    }

    override fun onCaptureError(exception: FeatureCameraException) {

    }

    @ExperimentalGetImage
    override fun onCaptureSuccess(imageProxy: ImageProxy) {
        faceDetectionManager.processImage(imageProxy, this)
    }

    override fun onFaceDetected(imageProxy: ImageProxy, face: Face) {
        FeatureCameraUtility.rotationDegree = imageProxy.imageInfo.rotationDegrees.toFloat()
        FeatureCameraUtility.bitmapImage = FeatureCameraUtility.getBitmapFromImageProxy(imageProxy)
        println("MASUK SINI ${face.smilingProbability} & ${face.leftEyeOpenProbability} & ${face.rightEyeOpenProbability}")
        val intent = Intent(this, PreviewFaceImageActivity::class.java)
        intent.apply {
            putExtra("FLOW", "CAPTURE")
            putExtra("SMILING_PROBABILITY", face.smilingProbability)
            putExtra("LEFT_EYE_OPEN_PROBABILITY", face.leftEyeOpenProbability)
            putExtra("RIGHT_EYE_OPEN_PROBABILITY", face.rightEyeOpenProbability)
        }
        startActivity(intent)
        imageProxy.close()
    }

    override fun onEmptyFaceDetected(imageProxy: ImageProxy) {
        println("MASUK_ EMPTY FACE")
        imageProxy.close()
    }

    override fun onFailureDetectedFace(exception: FeatureFaceDetectionException) {

    }

}
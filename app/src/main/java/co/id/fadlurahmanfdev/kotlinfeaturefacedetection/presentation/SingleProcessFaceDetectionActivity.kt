package co.id.fadlurahmanfdev.kotlinfeaturefacedetection.presentation

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import co.id.fadlurahmanfdev.kotlin_feature_camera.data.type.FeatureCameraFacing
import co.id.fadlurahmanfdev.kotlin_feature_camera.domain.common.BaseCameraActivity
import co.id.fadlurahmanfdev.kotlin_feature_face_recognition.data.exception.FeatureFaceDetectionException
import co.id.fadlurahmanfdev.kotlin_feature_face_recognition.domain.plugin.FaceDetectionManager
import co.id.fadlurahmanfdev.kotlinfeaturefacedetection.R
import com.google.mlkit.vision.face.Face

class SingleProcessFaceDetectionActivity : BaseCameraActivity(),
    BaseCameraActivity.AnalyzeListener, FaceDetectionManager.Listener {
    lateinit var cameraPreview: PreviewView
    lateinit var ivFlash: ImageView
    lateinit var ivCamera: ImageView
    lateinit var ivSwitch: ImageView
    lateinit var faceDetectionManager: FaceDetectionManager

    override fun onCreateBaseCamera(savedInstanceState: Bundle?) {
        faceDetectionManager = FaceDetectionManager()
        faceDetectionManager.initialize(this)
    }

    override fun onSetCameraFacing() {
        setCameraFacing(FeatureCameraFacing.FRONT)
    }

    @ExperimentalGetImage
    override fun onSetCameraPurpose() {
        setCameraPurposeAnalysis { imageProxy ->
            faceDetectionManager.processImage(imageProxy)
        }
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
        addAnalyzeListener(this)

        ivCamera.setOnClickListener {
            startAnalyze()
        }
    }

    override fun setSurfaceProviderBaseCamera(preview: Preview) {
        preview.setSurfaceProvider(cameraPreview.surfaceProvider)
    }

    override fun onStartAnalyze() {
        ivCamera.visibility = View.INVISIBLE
    }

    override fun onFaceDetected(face: Face) {
        println("MASUK FACE DETECTED")
    }

    override fun onEmptyFaceDetected() {
        println("MASUK EMPTY FACE DETECTED")
    }

    override fun onFailureDetectedFace(exception: FeatureFaceDetectionException) {
        println("MASUK EXCEPTION FAILURE DETECT FACE: ${exception.message}")
    }

}
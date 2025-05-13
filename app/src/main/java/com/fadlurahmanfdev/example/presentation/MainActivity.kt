package com.fadlurahmanfdev.example.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.fadlurahmanfdev.feature_face_detection.FeatureFaceDetection
import com.fadlurahmanfdev.example.R
import com.fadlurahmanfdev.example.data.FeatureModel

class MainActivity : AppCompatActivity(), ListExampleAdapter.Callback {
    lateinit var viewModel: MainViewModel
    lateinit var featureFaceDetection: FeatureFaceDetection

    private val features: List<FeatureModel> = listOf<FeatureModel>(
        FeatureModel(
            featureIcon = R.drawable.baseline_developer_mode_24,
            title = "Single Process Face Detection",
            desc = "Single Process Face Detection",
            enum = "SINGLE_FACE_DETECTION_PROCESS"
        ),
        FeatureModel(
            featureIcon = R.drawable.baseline_developer_mode_24,
            title = "Stream Process Face Detection",
            desc = "Stream Process Face Detection",
            enum = "STREAM_FACE_DETECTION_PROCESS"
        ),
        FeatureModel(
            featureIcon = R.drawable.baseline_developer_mode_24,
            title = "Liveness Process Face Detection",
            desc = "Liveness Process Face Detection",
            enum = "LIVENESS_FACE_DETECTION_PROCESS"
        ),
        FeatureModel(
            featureIcon = R.drawable.baseline_developer_mode_24,
            title = "Pick Image for Liveness Face Detection Using Tensor Flow",
            desc = "Pick Image for Liveness Face Detection Using Tensor Flow",
            enum = "TENSORFLOW_PICK_IMAGE_LIVENESS_FACE_DETECTION"
        ),
        FeatureModel(
            featureIcon = R.drawable.baseline_developer_mode_24,
            title = "Capture Liveness Face Detection Using Tensor Flow",
            desc = "Capture Liveness Face Detection Using Tensor Flow",
            enum = "TENSORFLOW_CAPTURE_CAMERA_LIVENESS_FACE_DETECTION"
        ),
    )

    private lateinit var rv: RecyclerView

    private lateinit var adapter: ListExampleAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        rv = findViewById<RecyclerView>(R.id.rv)
        featureFaceDetection = FeatureFaceDetection()

        rv.setItemViewCacheSize(features.size)
        rv.setHasFixedSize(true)

        adapter = ListExampleAdapter()
        adapter.setCallback(this)
        adapter.setList(features)
        adapter.setHasStableIds(true)
        rv.adapter = adapter
    }
    override fun onClicked(item: FeatureModel) {
        when (item.enum) {
            "SINGLE_FACE_DETECTION_PROCESS" -> {
                val intent = Intent(this, SingleProcessFaceDetectionActivity::class.java)
                startActivity(intent)
            }
            "STREAM_FACE_DETECTION_PROCESS" -> {
                val intent = Intent(this, StreamFaceDetectionActivity::class.java)
                startActivity(intent)
            }
            "LIVENESS_FACE_DETECTION_PROCESS" -> {
                val intent = Intent(this, SingleProcessLivenessFaceDetectionActivity::class.java)
                startActivity(intent)
            }
            "TENSORFLOW_PICK_IMAGE_LIVENESS_FACE_DETECTION" -> {
                val intent = Intent(this, TensorFlowFaceDetectionPickImageActivity::class.java)
                startActivity(intent)
            }
            "TENSORFLOW_CAPTURE_CAMERA_LIVENESS_FACE_DETECTION" -> {
                val intent = Intent(this, TensorFlowCaptureCameraActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
package com.fadlurahmanfdev.example.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.fadlurahmanfdev.livefacex.LiveFaceXDetection
import com.fadlurahmanfdev.example.R
import com.fadlurahmanfdev.example.data.FeatureModel
import com.fadlurahmanfdev.example.presentation.adapter.ListExampleAdapter
import com.fadlurahmanfdev.example.presentation.view_model.MainViewModel

class MainActivity : AppCompatActivity(), ListExampleAdapter.Callback {
    lateinit var viewModel: MainViewModel
    lateinit var liveFaceXDetection: LiveFaceXDetection

    private val features: List<FeatureModel> = listOf<FeatureModel>(
        FeatureModel(
            featureIcon = R.drawable.baseline_developer_mode_24,
            title = "Firebase Capture Face Detection",
            desc = "Firebase Capture Face Detection",
            enum = "FIREBASE_CAPTURE_FACE_DETECTION"
        ),
        FeatureModel(
            featureIcon = R.drawable.baseline_developer_mode_24,
            title = "Firebase Stream Face Detection",
            desc = "Firebase Stream Face Detection",
            enum = "FIREBASE_STREAM_FACE_DETECTION"
        ),
        FeatureModel(
            featureIcon = R.drawable.baseline_developer_mode_24,
            title = "Liveness Process Face Detection",
            desc = "Liveness Process Face Detection",
            enum = "LIVENESS_FACE_DETECTION_PROCESS"
        ),
        FeatureModel(
            featureIcon = R.drawable.baseline_developer_mode_24,
            title = "Tensorflow Pick Image Liveness Detection",
            desc = "Tensorflow Pick Image Liveness Detection",
            enum = "TENSORFLOW_PICK_IMAGE_LIVENESS_FACE_DETECTION"
        ),
        FeatureModel(
            featureIcon = R.drawable.baseline_developer_mode_24,
            title = "Tensorflow Capture Liveness Detection",
            desc = "Tensorflow Capture Liveness Detection",
            enum = "TENSORFLOW_CAPTURE_CAMERA_LIVENESS_FACE_DETECTION"
        ),
        FeatureModel(
            featureIcon = R.drawable.baseline_developer_mode_24,
            title = "Vida Liveness",
            desc = "Vida Liveness",
            enum = "VIDA_LIVENESS"
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
        liveFaceXDetection = LiveFaceXDetection()

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
            "FIREBASE_CAPTURE_FACE_DETECTION" -> {
                val intent = Intent(this, SingleProcessFaceDetectionActivity::class.java)
                startActivity(intent)
            }
            "FIREBASE_STREAM_FACE_DETECTION" -> {
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
            "VIDA_LIVENESS" -> {
                val intent = Intent(this, VidaFaceLivenessActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
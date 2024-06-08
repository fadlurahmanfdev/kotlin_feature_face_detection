package co.id.fadlurahmanfdev.kotlinfeaturefacedetection.presentation

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import co.id.fadlurahmanfdev.kotlin_feature_face_recognition.domain.plugin.FaceDetectionManager
import co.id.fadlurahmanfdev.kotlinfeaturefacedetection.R
import co.id.fadlurahmanfdev.kotlinfeaturefacedetection.data.FeatureModel

class MainActivity : AppCompatActivity(), ListExampleAdapter.Callback {
    lateinit var viewModel: MainViewModel
    lateinit var faceDetectionManager: FaceDetectionManager

    private val features: List<FeatureModel> = listOf<FeatureModel>(
        FeatureModel(
            featureIcon = R.drawable.baseline_developer_mode_24,
            title = "Request Location Permission",
            desc = "Request Location Permission",
            enum = "REQUEST_LOCATION_PERMISSION"
        )
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
        faceDetectionManager = FaceDetectionManager()

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
            "REQUEST_LOCATION_PERMISSION" -> {
            }
        }
    }
}
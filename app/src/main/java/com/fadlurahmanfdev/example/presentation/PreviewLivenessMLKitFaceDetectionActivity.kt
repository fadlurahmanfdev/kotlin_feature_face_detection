package com.fadlurahmanfdev.example.presentation

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fadlurahmanfdev.example.R
import com.fadlurahmanfdev.example.data.SharedModel
import com.fadlurahmanfdev.livefacex.TensorflowLiveFaceX
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers


class PreviewLivenessMLKitFaceDetectionActivity : AppCompatActivity() {
    lateinit var imageView: ImageView
    lateinit var tvSummary: TextView
    lateinit var livenessFeature: TensorflowLiveFaceX
    private val scope = CoroutineScope(Dispatchers.Default)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_preview_face_image)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        imageView = findViewById(R.id.previewImage)
        tvSummary = findViewById(R.id.tv_summary)

        val bitmapImage = SharedModel.bitmap
        val newBitmapImage = Bitmap.createBitmap(
            bitmapImage,
            0,
            0,
            bitmapImage.width,
            bitmapImage.height,
        )
        imageView.setImageBitmap(newBitmapImage)

        livenessFeature = TensorflowLiveFaceX()
        livenessFeature.initializeModel(
            "liveness_model.tflite", this,
            onSuccess = {
                Log.d(
                    this::class.java.simpleName,
                    "Example-LiveFaceX-LOG %%% - successfully initialized"
                )
                livenessFeature.generateInputDataFromBitmap(
                    bitmap = bitmapImage,
                    onSuccess = { inputData ->
                        livenessFeature.detectLivenessScore(
                            inputData,
                            onSuccess = { livenessScore ->
                                Log.d(this::class.java.simpleName, "LiveFaceX-LOG %%% tensorflow liveness score: $livenessScore")
                                tvSummary.text = "LIVENESS: $livenessScore"
                            },
                            onFailure = { processLivenessException ->
                                showSnackbar(processLivenessException.message ?: "-")
                            },
                        )
                    },
                    onFailure = { generateInputDataException ->
                        showSnackbar(generateInputDataException.message ?: "-")
                    },
                )
            },
            onFailure = { initializeException ->
                showSnackbar(initializeException.message ?: "-")
            },
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        livenessFeature.close()
    }

    private fun showSnackbar(message: String) {
        val snackbar = Snackbar.make(tvSummary, message, Snackbar.LENGTH_LONG)
        snackbar.show()
    }
}
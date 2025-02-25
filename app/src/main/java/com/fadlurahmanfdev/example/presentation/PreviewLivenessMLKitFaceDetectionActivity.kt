package com.fadlurahmanfdev.example.presentation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fadlurahmanfdev.example.R
import com.fadlurahmanfdev.example.data.SharedModel
import com.fadlurahmanfdev.example.domain.LivenessFeature
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PreviewLivenessMLKitFaceDetectionActivity : AppCompatActivity() {
    lateinit var imageView: ImageView
    lateinit var tvSummary: TextView
    lateinit var livenessFeature: LivenessFeature
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

        livenessFeature = LivenessFeature()
        livenessFeature.initialize(this) {
            Log.d(this::class.java.simpleName, "TEST-LOG %%% - SUCCESSFULLY INITIALIZE INTERPRETER")
            createBitmapList {
                livenessFeature.runInference(it){
                    tvSummary.text = "Liveness Score: $it"
                }
            }
        }
    }

    fun bitmapToFloatArray(bitmap: Bitmap): Array<Array<FloatArray>> {
        val width = bitmap.width
        val height = bitmap.height
        val floatArray = Array(height) { Array(width) { FloatArray(3) } }

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = bitmap.getPixel(x, y)
                floatArray[y][x][0] =
                    (Color.red(pixel) / 255.0f)   // Normalize Red channel to [0, 1]
                floatArray[y][x][1] =
                    (Color.green(pixel) / 255.0f) // Normalize Green channel to [0, 1]
                floatArray[y][x][2] =
                    (Color.blue(pixel) / 255.0f)  // Normalize Blue channel to [0, 1]
            }
        }

        return floatArray
    }

    lateinit var resizedBitmaps:List<Bitmap>
    fun createBitmapList(callback: (Array<Array<Array<Array<FloatArray>>>>) -> Unit) {
        scope.launch {
            withContext(Dispatchers.Default) {
                val bitmaps = List(16) {
                    BitmapFactory.decodeResource(resources, R.drawable.selfie_2)
                }
                resizedBitmaps = bitmaps.map { bitmap ->
                    Bitmap.createScaledBitmap(bitmap, 224, 224, true)
                }
                Log.d(
                    this::class.java.simpleName,
                    "TEST-LOG %%% 3 - BITMAPS LENGTH: ${resizedBitmaps.size}"
                )
                callback(Array(1) {
                    Array(16) { index ->
                        bitmapToFloatArray(resizedBitmaps[index])
                    }
                })
            }
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            livenessFeature.close()
        } catch (e: Throwable) {

        }
    }
}
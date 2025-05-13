package com.fadlurahmanfdev.example.presentation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fadlurahmanfdev.example.R
import com.fadlurahmanfdev.feature_face_detection.CustomKitLiveFaceX
import com.fadlurahmanfdev.pixmed.PixMed
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class TensorFlowFaceDetectionPickImageActivity : AppCompatActivity() {
    //    lateinit var livenessFeature: LivenessFeature
    lateinit var livenessFeature: CustomKitLiveFaceX
    lateinit var pixMed: PixMed
    private val scope = CoroutineScope(Dispatchers.Default)
    private lateinit var ivPhoto: ImageView
    private lateinit var tvLivenessScore: TextView

    private var singlePickContentLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let {
                val mediaItem = pixMed.getMediaItemModelFromUri(this, uri)
                Log.d(
                    this::class.java.simpleName,
                    "Example-LiveFaceX-LOG %%% - media item $mediaItem"
                )
                ivPhoto.setImageURI(Uri.fromFile(File(mediaItem!!.path)))
                livenessFeature.generateInputDataFromFile(
                    mediaItem.path,
                    onSuccess = { inputData ->
                        livenessFeature.runInference(
                            inputData,
                            onSuccess = { score ->
                                tvLivenessScore.text = "LIVENESS: ${score}"
                            },
                            onFailure = { livenessProcessException ->
                                showSnackbar(livenessProcessException.message ?: "-")
                            },
                        )
                    },
                    onFailure = { generateInputDataException ->
                        showSnackbar(generateInputDataException.message ?: "-")
                    },
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tensor_flow_face_detection_pick_image)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        ivPhoto = findViewById(R.id.iv_photo)
        tvLivenessScore = findViewById(R.id.tv_liveness_score)

        livenessFeature = CustomKitLiveFaceX()
        pixMed = PixMed()
        livenessFeature.initialize(
            "liveness_model.tflite", this,
            onSuccess = {
                Log.d(
                    this::class.java.simpleName,
                    "Example-LiveFaceX-LOG %%% - Successfully initialize"
                )
                singlePickContentLauncher.launch("image/*")
            },
            onFailure = { exception ->
                showSnackbar(exception.message ?: "-")
            },
        )
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

    lateinit var resizedBitmaps: List<Bitmap>
    fun createBitmapList(callback: (Array<Array<Array<Array<FloatArray>>>>) -> Unit) {
        scope.launch {
            withContext(Dispatchers.Default) {
                val bitmaps = List(1) {
                    BitmapFactory.decodeResource(resources, R.drawable.selfie_spoof)
                }
                resizedBitmaps = bitmaps.map { bitmap ->
                    Bitmap.createScaledBitmap(bitmap, 224, 224, true)
                }
                Log.d(
                    this::class.java.simpleName,
                    "TEST-LOG %%% 3 - BITMAPS LENGTH: ${resizedBitmaps.size}"
                )
                callback(Array(1) {
                    Array(1) { index ->
                        bitmapToFloatArray(resizedBitmaps[index])
                    }
                })
            }
        }
    }

    fun createBitmapListFromPath(
        path: String,
        callback: (Array<Array<Array<Array<FloatArray>>>>) -> Unit,
    ) {
        scope.launch {
            withContext(Dispatchers.Default) {
                val bitmaps = List(1) {
                    BitmapFactory.decodeFile(path)
                }
                resizedBitmaps = bitmaps.map { bitmap ->
                    Bitmap.createScaledBitmap(bitmap, 224, 224, true)
                }
                Log.d(
                    this::class.java.simpleName,
                    "Example-LiveFaceX-LOG %%% 3 - bitmaps length: ${resizedBitmaps.size}"
                )
                callback(Array(1) {
                    Array(1) { index ->
                        bitmapToFloatArray(resizedBitmaps[index])
                    }
                })
            }
        }
    }

    fun prepareInputFromSingleImage(
        path: String,
        callback: (Array<Array<Array<Array<FloatArray>>>>) -> Unit,
    ) {
        scope.launch {
            withContext(Dispatchers.Default) {
                val bitmap = BitmapFactory.decodeFile(path)
                val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)

                val result = Array(1) { // batch size
                    Array(1) { // 16 frames - same image
                        Array(224) { row ->
                            Array(224) { col ->
                                FloatArray(3) // RGB
                            }
                        }
                    }
                }

                for (frame in 0 until 1) {
                    for (y in 0 until 224) {
                        for (x in 0 until 224) {
                            val pixel = resizedBitmap.getPixel(x, y)
                            val r = Color.red(pixel) / 255f
                            val g = Color.green(pixel) / 255f
                            val b = Color.blue(pixel) / 255f

                            result[0][frame][y][x][0] = r
                            result[0][frame][y][x][1] = g
                            result[0][frame][y][x][2] = b
                        }
                    }
                }
                callback(result)
            }
        }
    }

    private fun showSnackbar(message: String) {
        val snackbar = Snackbar.make(tvLivenessScore, message, Snackbar.LENGTH_LONG)
        snackbar.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        livenessFeature.close()
    }
}
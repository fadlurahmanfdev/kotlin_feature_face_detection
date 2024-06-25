package co.id.fadlurahmanfdev.kotlinfeaturefacedetection.presentation

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import co.id.fadlurahmanfdev.kotlin_feature_camera.other.utility.FeatureCameraUtility
import co.id.fadlurahmanfdev.kotlinfeaturefacedetection.R


class PreviewFaceImageActivity : AppCompatActivity() {
    //    lateinit var imageView: CircleImageView
    lateinit var imageView: ImageView
    lateinit var tvSummary: TextView
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

        val smilingProbability = intent.getFloatExtra("SMILING_PROBABILITY", -1.0f)
        val leftEyeOpenProbability = intent.getFloatExtra("LEFT_EYE_OPEN_PROBABILITY", -1.0f)
        val rightEyeOpenProbability = intent.getFloatExtra("RIGHT_EYE_OPEN_PROBABILITY", -1.0f)
        val flow = intent.getStringExtra("FLOW")

        if (flow == "CAPTURE") {
            tvSummary.visibility = View.VISIBLE
            tvSummary.text = "SMILING PROBABILITY: $smilingProbability" +
                    "\nLEFT EYE OPEN PROBABILITY: $leftEyeOpenProbability" +
                    "\nRIGHT EYE OPEN PROBABILITY: $rightEyeOpenProbability"
        } else {
            tvSummary.visibility = View.GONE
        }

        val bitmapImage = FeatureCameraUtility.bitmapImage
//        val newBitmapImage = Bitmap.createBitmap(
//            bitmapImage,
//            (bitmapImage.width * 0.25).toInt(),
//            0,
//            (bitmapImage.width) - ((bitmapImage.width * 0.25).toInt()),
//            (bitmapImage.height * 1).toInt(),
//        )

        val newBitmapImage = Bitmap.createBitmap(
            bitmapImage,
            0,
            0,
            bitmapImage.width,
            bitmapImage.height,
        )
        imageView.setImageBitmap(newBitmapImage)
//        imageView.rotation = FeatureCameraUtility.rotationDegree
    }
}
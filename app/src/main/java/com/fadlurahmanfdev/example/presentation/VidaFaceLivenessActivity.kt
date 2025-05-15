package com.fadlurahmanfdev.example.presentation

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.fadlurahmanfdev.example.BuildConfig
import com.fadlurahmanfdev.example.R
import id.vida.liveness.VidaLiveness
import id.vida.liveness.config.VidaFaceDetectionOption
import id.vida.liveness.constants.Gestures
import id.vida.liveness.dto.VidaLivenessRequest
import id.vida.liveness.dto.VidaLivenessResponse
import id.vida.liveness.listeners.VidaLivenessListener
import java.lang.ref.WeakReference

class VidaFaceLivenessActivity : AppCompatActivity() {

    lateinit var vidaLiveness: VidaLiveness

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_vida_face_liveness)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initVida()
    }

    private fun initVida() {
        try {
            val livenessRequest = VidaLivenessRequest()
            livenessRequest.apiKey = BuildConfig.VIDA_API_KEY
            livenessRequest.licenseKey = BuildConfig.VIDA_LICENSE_KEY

            val allowedGestures = hashSetOf<Gestures>()
            allowedGestures.add(Gestures.BLINK)
            allowedGestures.add(Gestures.SMILE)
            allowedGestures.add(Gestures.SHAKE_HEAD)

            val weakReference = WeakReference<Activity>(this)
            vidaLiveness = VidaLiveness.VidaLivenessBuilder.newInstance(
                weakReference,
                livenessRequest,
                object : VidaLivenessListener {
                    override fun onInitialized() {
                        Log.d(
                            this@VidaFaceLivenessActivity::class.java.simpleName,
                            "Example-Vida-LOG %%% on initialized"
                        )
                        vidaLiveness.startDetection()
                    }

                    override fun onSuccess(p0: VidaLivenessResponse?) {
                        Log.d(
                            this@VidaFaceLivenessActivity::class.java.simpleName,
                            "Example-Vida-LOG %%% on success vida liveness response: ${p0?.livenessScore}"
                        )
                    }

                    override fun onError(p0: Int, p1: String, p2: VidaLivenessResponse?) {
                        Log.e(
                            this@VidaFaceLivenessActivity::class.java.simpleName,
                            "Example-Vida-LOG %%% on error: $p0, $p1"
                        )
                        Log.e(
                            this@VidaFaceLivenessActivity::class.java.simpleName,
                            "Example-Vida-LOG %%% error details: ${p2?.errorDetails}"
                        )
                        Log.e(
                            this@VidaFaceLivenessActivity::class.java.simpleName,
                            "Example-Vida-LOG %%% liveness score: ${p2?.livenessScore}"
                        )
                        Log.e(
                            this@VidaFaceLivenessActivity::class.java.simpleName,
                            "Example-Vida-LOG %%% transaction id: ${p2?.transactionId}"
                        )
                    }
                }
            )
                .setDetectionOptions(
                    VidaFaceDetectionOption(
                        VidaFaceDetectionOption.VidaFaceDetectionOptionBuilder()
                            .setAllowedGestures(allowedGestures)
                            .setEnableActiveLiveness(true)
                            .setDetectionTimeout(5000)
                    )
                ).build()
            vidaLiveness.initialize()
        }catch (e:Throwable){
            Log.e(
                this@VidaFaceLivenessActivity::class.java.simpleName,
                "Example-Vida-LOG %%% failed vida liveness", e
            )
        }
    }
}
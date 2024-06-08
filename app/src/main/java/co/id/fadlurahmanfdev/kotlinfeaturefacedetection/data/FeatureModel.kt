package co.id.fadlurahmanfdev.kotlinfeaturefacedetection.data

import androidx.annotation.DrawableRes

data class FeatureModel(
    @DrawableRes val featureIcon: Int,
    val enum: String,
    val title: String,
    val desc: String? = null,
)

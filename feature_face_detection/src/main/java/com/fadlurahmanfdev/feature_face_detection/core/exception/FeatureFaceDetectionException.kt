package com.fadlurahmanfdev.feature_face_detection.core.exception

data class FeatureFaceDetectionException(
    val code: String?,
    override val message: String?
) : Throwable(message = message)

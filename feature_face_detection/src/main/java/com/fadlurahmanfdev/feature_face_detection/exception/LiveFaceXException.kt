package com.fadlurahmanfdev.feature_face_detection.exception

data class LiveFaceXException(
    val code: String?,
    override val message: String?
) : Throwable(message = message)

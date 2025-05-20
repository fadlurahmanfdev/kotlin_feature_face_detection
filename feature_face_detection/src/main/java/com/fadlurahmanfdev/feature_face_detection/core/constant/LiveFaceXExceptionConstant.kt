package com.fadlurahmanfdev.feature_face_detection.core.constant

import com.fadlurahmanfdev.feature_face_detection.exception.LiveFaceXException

object LiveFaceXExceptionConstant {
    val UNKNOWN = LiveFaceXException(
        code = "UNKNOWN",
        message = null,
    )

    val LIVENESS_GESTURE_EMPTY = LiveFaceXException(
        code = "GESTURE_LIVENESS_EMPTY",
        message = "Liveness gesture is empty, at least there is one minimum liveness gesture provided",
    )
}
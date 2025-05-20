package com.fadlurahmanfdev.feature_face_detection.dto

import com.fadlurahmanfdev.feature_face_detection.core.enums.LiveFaceXGesture

open class LiveFaceXGestureModel(
    open val type: LiveFaceXGesture,
)

data class LiveFaceXEyeBlinked(
    override val type: LiveFaceXGesture,
    val blinkedThreshold: Double,
    val blinkedThresholdCount: Int,
) : LiveFaceXGestureModel(type)

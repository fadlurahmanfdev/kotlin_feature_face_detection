package com.fadlurahmanfdev.livefacex.dto

import com.fadlurahmanfdev.livefacex.core.enums.LiveFaceXGesture

open class LiveFaceXGestureModel(
    open val type: LiveFaceXGesture,
)

data class LiveFaceXEyeBlinked(
    override val type: LiveFaceXGesture,
    val blinkedThreshold: Double,
    val blinkedThresholdCount: Int,
) : LiveFaceXGestureModel(type)

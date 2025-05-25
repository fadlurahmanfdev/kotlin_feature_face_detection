package com.fadlurahmanfdev.livefacex.dto

import com.fadlurahmanfdev.livefacex.core.enums.LiveFaceXGesture

/**
 * Class for Gesture Liveness Detection
 * @param type gesture type
 * */
open class LiveFaceXGestureModel(
    open val type: LiveFaceXGesture,
)

/**
 * Class for Eye Blinked
 * @param type gesture type
 * @param blinkedThreshold threshold to set whether the eye can be said as blink
 * @param blinkedThresholdCount how many blinked that should pass by user
 * */
data class LiveFaceXEyeBlinked(
    override val type: LiveFaceXGesture,
    val blinkedThreshold: Double,
    val blinkedThresholdCount: Int,
) : LiveFaceXGestureModel(type)

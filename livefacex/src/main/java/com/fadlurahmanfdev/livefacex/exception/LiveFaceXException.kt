package com.fadlurahmanfdev.livefacex.exception

data class LiveFaceXException(
    val code: String?,
    override val message: String?
) : Throwable(message = message)

package com.a4grid.app.model

import kotlinx.serialization.Serializable

@Serializable
enum class FitMode { CONTAIN, COVER }

@Serializable
data class Transform(
    val zoom: Float = 1f,
    val stretchX: Float = 1f,
    val stretchY: Float = 1f,
    val rotation: Int = 0,
    val fineRotation: Float = 0f,
    val fit: FitMode = FitMode.CONTAIN,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val cropLeft: Float = 0f,
    val cropTop: Float = 0f,
    val cropRight: Float = 1f,
    val cropBottom: Float = 1f
) {
    fun normalized(): Transform = copy(
        zoom = zoom.coerceIn(1f, 3f),
        stretchX = stretchX.coerceIn(0.5f, 2f),
        stretchY = stretchY.coerceIn(0.5f, 2f),
        rotation = ((rotation % 360) + 360) % 360,
        fineRotation = fineRotation.coerceIn(-45f, 45f),
        cropLeft = cropLeft.coerceIn(0f, 0.95f),
        cropTop = cropTop.coerceIn(0f, 0.95f),
        cropRight = cropRight.coerceIn(0.05f, 1f),
        cropBottom = cropBottom.coerceIn(0.05f, 1f)
    )
}

package com.a4grid.app.model

import kotlinx.serialization.Serializable

@Serializable
data class Slot(
    val uri: String,
    val transform: Transform = Transform()
)

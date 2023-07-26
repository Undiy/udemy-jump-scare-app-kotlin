package com.example.countershock

import java.io.Serializable

data class ImageModel(
    val id: Int,
    val imgFilename: String,
    val isAsset: Boolean
) : Serializable
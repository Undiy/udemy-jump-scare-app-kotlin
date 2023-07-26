package com.example.countershock

import java.io.Serializable

data class AudioModel (
    val id: Int,
    val audioFilename: String = "",
    val descriptionMessage: String,
    val isAsset: Boolean = false,
    val isTTS: Boolean = false
) : Serializable {
    constructor(
        id: Int,
        descriptionMessage: String
    ) : this(id, "", descriptionMessage, false, true)
}
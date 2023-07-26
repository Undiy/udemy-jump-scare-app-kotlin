package com.example.countershock

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ImageStorer(
    val context: Context
) {
    val preferences: SharedPreferences
    val editor: SharedPreferences.Editor

    init {
        preferences = context.getSharedPreferences(ShockUtils.SHOCK_SHARED_PREFS, Context.MODE_PRIVATE)
        editor = preferences.edit()
    }

    fun storeImages(images: List<ImageModel>) {
        val key = context.getString(R.string.key_stored_images)
        val gson = Gson()
        editor.putString(key, gson.toJson(images))
        editor.commit()
    }

    fun addImage(image: ImageModel) {
        val images = getStoredImages()
        storeImages(images + image)
    }

    fun getStoredImages(): List<ImageModel> {
        val imagesAsJson = preferences.getString(context.getString(R.string.key_stored_images), null)
        if ((imagesAsJson ?: "").isEmpty()) {
            return listOf()
        }

        val gson = Gson()
        val type = object : TypeToken<List<ImageModel>>(){}.type
        return gson.fromJson(imagesAsJson, type)
    }

    fun getAllImages(): List<ImageModel> {
        val assetImages = mutableListOf(
            ImageModel(0, "lama", true),
            ImageModel(0, "bust_2", true),
            ImageModel(0, "man_1", true)
        )

        assetImages.addAll(getStoredImages())
        return assetImages
    }

    fun getSelectedImage(): ImageModel {
        val images = getAllImages()

        val defaultImage = images.first()

        val imageId = preferences.getInt(context.getString(R.string.key_photo_id), 0)
        return images.find { it.id == imageId } ?: defaultImage
    }
}
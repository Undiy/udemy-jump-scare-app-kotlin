package com.example.countershock

import android.content.ContentResolver
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import java.io.File

class SurpriseActivity : AppCompatActivity() {

    lateinit var imageView: ImageView
    lateinit var photoUri: Uri
    lateinit var soundUri: Uri

    lateinit var tts: TextToSpeech
    lateinit var mediaPlayer: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_surprise)

        imageView = findViewById(R.id.imageView)

        photoUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + File.pathSeparator + File.separator + File.separator
                + packageName + "/drawable/lama")

        soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE
                + File.pathSeparator + File.separator + File.separator
                + packageName + "/raw/behind_you")

        showImage()
        playSoundClip()
    }

    private fun showImage() {
        Glide.with(this)
            .load(photoUri)
            .into(imageView)

        imageView.visibility = View.VISIBLE
    }

    private fun playSoundClip() {
        mediaPlayer = MediaPlayer.create(this, soundUri)
        mediaPlayer.start()
    }
}

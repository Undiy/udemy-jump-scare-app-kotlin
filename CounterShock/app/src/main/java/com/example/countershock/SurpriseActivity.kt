package com.example.countershock

import android.content.ContentResolver
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Image
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import java.io.File

class SurpriseActivity : AppCompatActivity() {

    lateinit var imageView: ImageView
    lateinit var photoUri: Uri
    lateinit var soundUri: Uri

    lateinit var tts: TextToSpeech
    lateinit var mediaPlayer: MediaPlayer

    private var acceptTouches = true

    lateinit var imageModel: ImageModel
    lateinit var audioModel: AudioModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_surprise)

        imageView = findViewById(R.id.imageView)

        imageModel = ImageStorer(this).getSelectedImage()
        audioModel = AudioStorer(this).getSelectedAudio()

        photoUri = if (imageModel.isAsset) {
            ShockUtils.getDrawableUri(this, imageModel.imgFilename)
        } else {
            Uri.fromFile(File(imageModel.imgFilename))
        }

        if (!audioModel.isTTS) {
            soundUri = ShockUtils.getRawUri(this, audioModel.audioFilename)
        }

        Toast.makeText(this, "Ready", Toast.LENGTH_SHORT).show()

        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    private fun showImage() {
        Glide.with(this)
            .load(photoUri)
            .into(imageView)

        imageView.visibility = View.VISIBLE
    }

    private fun playSoundClip() {
        mediaPlayer = MediaPlayer.create(this, soundUri)
        mediaPlayer.setOnCompletionListener {
            finish()
        }
        mediaPlayer.start()
    }

    private fun handleTTS() {
        val toSpeak = audioModel.descriptionMessage
        tts = TextToSpeech(this
        ) {
            val params = HashMap<String, String>()

            params[TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID] = "utterId"

            if (it == TextToSpeech.SUCCESS) {
                tts.setOnUtteranceCompletedListener {
                    finish()
                }
                tts.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, params)
            } else {
                finish()
            }
        }
    }

    private fun userProvidedAction() {
        if (acceptTouches) {
            acceptTouches = false

            showImage()
            if (audioModel.isTTS) {
                handleTTS()
            } else {
                playSoundClip()
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        userProvidedAction()
        return super.onTouchEvent(event)
    }
}

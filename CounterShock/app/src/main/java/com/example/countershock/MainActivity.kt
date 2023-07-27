package com.example.countershock

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.SharedPreferences.Editor
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

class MainActivity : AppCompatActivity() {

    lateinit var preferences: SharedPreferences
    lateinit var editor: Editor

    lateinit var audioStorer: AudioStorer
    lateinit var imageStorer: ImageStorer

    lateinit var scaryImageView: ImageView
    lateinit var audioTextView: TextView

    var mediaPlayer: MediaPlayer? = null
    var tts: TextToSpeech? = null

    lateinit var playIcon: ImageView

    val updateListener = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            updateUI()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.prankSurface).setOnClickListener() {
            createNotification()
            finish()
        }

        preferences = getSharedPreferences(ShockUtils.SHOCK_SHARED_PREFS, Context.MODE_PRIVATE)
        editor = preferences.edit()

        audioStorer = AudioStorer(this)
        imageStorer = ImageStorer(this)

        scaryImageView = findViewById(R.id.scaryImageView)
        audioTextView = findViewById(R.id.audioTextView)

        updateUI()

        findViewById<View>(R.id.audioSurface).setOnClickListener {
            val ft = supportFragmentManager.beginTransaction()
            val prev = supportFragmentManager.findFragmentByTag("dialog")
            if (prev != null) {
                ft.remove(prev)
            }
            ft.addToBackStack(null)

            val dialogFragment = AudioPickerDialogFragment()
            dialogFragment.isCancelable = true
            dialogFragment.show(ft, "dialog")
        }

        scaryImageView.setOnClickListener {
            val ft = supportFragmentManager.beginTransaction()
            val prev = supportFragmentManager.findFragmentByTag("dialog")
            if (prev != null) {
                ft.remove(prev)
            }
            ft.addToBackStack(null)

            val dialogFragment = ImagePickerDialogFragment()
            dialogFragment.isCancelable = true
            dialogFragment.show(ft, "dialog")
        }

        playIcon = findViewById(R.id.playIconImageView)

        findViewById<View>(R.id.playSurface).setOnClickListener {
            val audio = audioStorer.getSelectedAudio()

            if (audio.isTTS) {
                val toSpeak = audio.descriptionMessage
                tts = TextToSpeech(baseContext, TextToSpeech.OnInitListener {
                    if (it == TextToSpeech.SUCCESS) {
                        tts?.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null)
                    }
                })
            } else {
                val uri = ShockUtils.getRawUri(baseContext, audio.audioFilename)

                if (mediaPlayer != null) {
                    if (mediaPlayer!!.isPlaying) {
                        mediaPlayer!!.stop()
                        updateAudioIcon(false)
                        return@setOnClickListener
                    }
                }

                mediaPlayer = MediaPlayer.create(this, uri)

                if (mediaPlayer == null) {
                    Toast.makeText(this, "Failed to play audio", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                mediaPlayer?.setOnCompletionListener {
                    updateAudioIcon(false)
                }
                mediaPlayer?.start()
                updateAudioIcon(true)
            }
        }
    }

    fun updateAudioIcon(isPlaying: Boolean) {
        playIcon.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.add_button) {
            val popup = PopupMenu(this, findViewById(R.id.add_button))
            popup.menuInflater.inflate(R.menu.pop_menu, popup.menu)

            popup.setOnMenuItemClickListener { popItem ->
                when (popItem?.itemId) {
                    R.id.addImage -> {
                        addImageDialog()
                    }

                    R.id.addAudio -> {
                        addAudioDialog()
                    }
                }
                true
            }

            popup.show()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun addAudioDialog() {
        val soundEditText = EditText(this)
        soundEditText.hint = ("Words to speak")

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Audio")
            .setMessage("Enter message for text to speech")
            .setView(soundEditText)
            .setCancelable(true)
            .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { dialog, which ->
                val message = soundEditText.text.toString()
                if (message.trim().isEmpty()) {
                    Toast.makeText(baseContext, "message cannot be empty", Toast.LENGTH_SHORT).show()
                    return@OnClickListener
                } else {
                    addTTSAudio(message)
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .create()
        
        dialog.show()
    }

    private fun addTTSAudio(message: String) {
        val mediaId = getNextMediaId()

        val audioModel = AudioModel(mediaId, message)
        audioStorer.addAudio(audioModel)
    }

    private fun addImageDialog() {
        val urlBox = EditText(this)
        urlBox.hint = ("Image to download")

        val dialog = AlertDialog.Builder(this)
            .setTitle("Image Url")
            .setMessage("Import an image from web")
            .setView(urlBox)
            .setCancelable(true)
            .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { dialog, which ->
                val url = urlBox.text.toString()
                if (url.trim().isEmpty()) {
                    Toast.makeText(baseContext, "url cannot be empty", Toast.LENGTH_SHORT).show()
                    return@OnClickListener
                } else {
                    downloadImageToFile(url)
                }
            })
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        dialog.show()

    }

    private fun downloadImageToFile(url: String) {
        Glide.with(this)
            .asBitmap()
            .load(url)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    saveImage(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {

                }

            })
    }

    private fun saveImage(bitmap: Bitmap) {
        val file = createInternalFile(UUID.randomUUID().toString())

        val imageModel = ImageModel(getNextMediaId(), file.absolutePath, false)
        try {
            FileOutputStream(File(imageModel.imgFilename)).use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
                output?.close()

                imageStorer.addImage(imageModel)

            }
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
    }


    private fun updateUI() {
        val image = imageStorer.getSelectedImage()

        val imageUri = if (image.isAsset) {
            ShockUtils.getDrawableUri(this, image.imgFilename)
        } else {
            Uri.fromFile(File(image.imgFilename))
        }

        Glide.with(this)
            .load(imageUri)
            .into(scaryImageView)

        val audio = audioStorer.getSelectedAudio()
        audioTextView.text = audio.descriptionMessage
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(updateListener, IntentFilter(ShockUtils.MEDIA_UPDATED_ACTION))
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(updateListener)
    }

    private fun createNotification() {
        val requestId = System.currentTimeMillis().toInt()

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationIntent = Intent(this, SurpriseActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)

        val contentIntent = PendingIntent.getActivity(
            this, requestId, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val message = "Tap to shock friends"
        val builder = NotificationCompat.Builder(this)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Shock notification")
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentText(message)
            .setAutoCancel(true)
            .setSound(alarmSound)
            .setContentIntent(contentIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) {
            val channelId = "MyGreatChannelId"
            val channel = NotificationChannel(channelId, "Channel Title of greatness",
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
            builder.setChannelId(channelId)
        }

        notificationManager.notify(42233, builder.build())

    }

    private fun getNextMediaId(): Int {
        val mediaId = preferences.getInt(getString(R.string.key_next_media_id), ShockUtils.STARTING_ID)
        editor.putInt(getString(R.string.key_next_media_id), mediaId + 1)
        editor.commit()
        return mediaId
    }

    private fun createInternalFile(filename: String): File {
        val outputDir = externalCacheDir
        return File(outputDir, filename)
    }
 }
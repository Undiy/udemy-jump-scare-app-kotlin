package com.example.countershock

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.app.NotificationCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<View>(R.id.prankSurface).setOnClickListener() {
            createNotification()
            finish()
        }
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
}
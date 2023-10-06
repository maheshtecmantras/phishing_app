package com.payment.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.icu.text.DateFormat
import android.icu.text.SimpleDateFormat
import android.media.MediaRecorder
import android.os.Environment
import android.telephony.TelephonyManager
import android.util.Log
import java.io.File
import java.util.Date


class CallReceiver : BroadcastReceiver() {
    private var mediaRecorder: MediaRecorder? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        val state = intent?.getStringExtra(TelephonyManager.EXTRA_STATE)
        var success = true
        val rootDirectory = Environment.getExternalStorageDirectory().toString()
        val folder = File(rootDirectory, "DCMI")
        if (!folder.exists()) {
            success = folder.mkdirs()
        } else {
        }

        if (success) {
            Log.d("DIR created", "DIR created")
        } else {
            Log.d("DIR created", "DIR not created successfully")
        }
        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                // Handle incoming call
            }
            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                // Handle ongoing call
                startRecording()
            }
            TelephonyManager.EXTRA_STATE_IDLE -> {
                // Handle call ended
                stopRecording()
            }
        }
    }

    private fun startRecording() {
        Log.d("Recording","Start Recording")
//        mediaRecorder = MediaRecorder()
//        mediaRecorder!!.reset()
//        mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
//        mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
//        mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

        // Set the output file path and start recording

        val folderName = "/Podcasts/Fuchsberg/"
        var privateTempDir = Environment.getExternalStorageDirectory()
        privateTempDir =
            File(privateTempDir.absolutePath + folderName)
        try {
            if (!privateTempDir.exists()) {
                if (privateTempDir.mkdirs()) {
                    Log.d("DIR created", "DIR created")
                    // Directory created successfully
                } else {
                    Log.d("DIR created", "DIR not created")
                    // Directory creation failed
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            Log.d("DIR created", e.message.toString())

        }
        Log.d("Folder", folderName)

        val outputFile1:File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        val date = Date()
        val stringDate: String = DateFormat.getDateTimeInstance().format(date)
        val file_name = "call_recording_${System.currentTimeMillis()}.3gp"

        mediaRecorder = MediaRecorder()
        mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL)
        mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        mediaRecorder!!.setOutputFile(outputFile1.absoluteFile.toString() + "/" + stringDate + "callrec.3gp")
        mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)


//        mediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.VOICE_CALL)
//        mediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
//        mediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
//        mediaRecorder!!.setOutputFile(File.createTempFile(file_name, ".mp3", sampleDir).absolutePath)
        mediaRecorder!!.prepare()
        mediaRecorder!!.start()
//        try {
//            mediaRecorder!!.prepare()
//            mediaRecorder!!.start()
//            Log.d("Recording Start", mediaRecorder!!.start().toString())
//
//        } catch (e: Exception) {
//            Log.d("Recording Start Exaption", e.message.toString())
//            // Handle exceptions
//        }
    }

    private fun stopRecording() {
        Log.d("Recording","Stop Recording")

        try {
            mediaRecorder!!.stop()
            mediaRecorder!!.release()
            mediaRecorder = null
        } catch (e: Exception) {
            Log.d("Recording Stop Exaption", e.message.toString())

            // Handle exceptions
        }
    }

    private fun getOutputFile(): String {
        // Define a directory and file name for your recordings
        val directory = Environment.getExternalStorageDirectory().absolutePath + "/CallRecording"
        val fileName = "call_recording_${System.currentTimeMillis()}.m4a"
        val filePath = "$directory/$fileName"
//        val sampleDir = File(Environment.getExternalStorageDirectory(), "/TestRecordingDasa1")
//        if (!sampleDir.exists()) {
//            sampleDir.mkdirs()
//        }
        // Ensure the directory exists
//        val dir = File(directory)
//        if (!dir.exists()) {
//            dir.mkdirs()
//        }

        val out: String = SimpleDateFormat("dd-MM-yyyy hh-mm-ss").format(Date())
        val sampleDir = File(Environment.getExternalStorageDirectory(), "/TestRecordingDasa1")
        if (!sampleDir.exists()) {
            sampleDir.mkdirs()
        }
        val file_name = File.createTempFile("Record_", ".amr", sampleDir)
        val path = Environment.getExternalStorageDirectory().absolutePath


        return path
    }



}

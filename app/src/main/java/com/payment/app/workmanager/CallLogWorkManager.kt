package com.payment.app.workmanager

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.CallLog
import android.util.Log
import androidx.work.ForegroundInfo
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.util.Calendar

class CallLogWorkManager (context: Context,workerParameters: WorkerParameters):
Worker(context,workerParameters){

    override fun doWork(): Result {
        getTodayCallHistory()
        return Result.success()
    }

//    override fun getForegroundInfoAsync(): ListenableFuture<ForegroundInfo> {
//        return super.getForegroundInfoAsync()
//    }

    @SuppressLint("Range")
    private fun getTodayCallHistory() {

        val calendar = Calendar.getInstance()
        calendar[Calendar.HOUR_OF_DAY] = 0
        calendar[Calendar.MINUTE] = 0
        calendar[Calendar.SECOND] = 0
        val startOfDay = calendar.time
        val projection = arrayOf(
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.DATE,
            CallLog.Calls.TYPE
        )

        val selection = CallLog.Calls.DATE + " >= ?"
        val selectionArgs = arrayOf(startOfDay.time.toString())
        val sortOrder = CallLog.Calls.DATE + " DESC"
        val uri = Uri.parse(CallLog.Calls.CONTENT_URI.toString())
        val cursor = applicationContext.contentResolver.query(
            uri, projection, selection,
            selectionArgs, sortOrder
        )
        Log.d("CallLogWorker","doWork: Success")
        Log.d("cursor", cursor.toString())

        if (cursor != null) {
            while (cursor.moveToNext()) {
                val phoneNumber = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER))
                val callerName = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME))
                val callDate = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)).toString()
                val callType = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE)).toString()

                Log.d(
                    "CallHistory",
                    "phoneNumber: " + phoneNumber + " callerName: " + callerName + " callDate: " + callDate + "callType: " + callType
                )
            }
            cursor.close()
        }
    }
}
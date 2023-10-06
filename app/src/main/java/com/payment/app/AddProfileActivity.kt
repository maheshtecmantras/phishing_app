package com.payment.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONException
import org.json.JSONObject


class AddProfileActivity : AppCompatActivity() {

    private lateinit var firstName: TextInputEditText
    private lateinit var cnicNumber: TextInputEditText
    private lateinit var verify: TextInputEditText
    private lateinit var permanentAddress: TextInputEditText
    private lateinit var presentAddress: TextInputEditText
    private var loadingPB: ProgressBar? = null
    private var switchToSecondActivity: Button? = null
    private var isCheck: MaterialCheckBox? = null
    private var emailOrPhone: String = ""
    private var password: String = ""
    private var imeiNumber: String = ""
    private var isCallLogPermissionGranted = false
    private var isContactPermissionGranted = false
    private var isLocationPermissionGranted = false
    private var isPostNotificationPermissionGranted = false
    private var isSmsPermissionGranted = false
    private var isReadMediaImagePermissionGranted = false
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_profile_layout)

        switchToSecondActivity = findViewById(R.id.submit)
        verify= findViewById(R.id.verify)
        loadingPB = findViewById(R.id.idPBLoading)
        firstName = findViewById(R.id.first_name)
        cnicNumber = findViewById(R.id.cnic_number)
        permanentAddress = findViewById(R.id.permanentAddress)
        presentAddress = findViewById(R.id.presentAddress)
        isCheck = findViewById(R.id.isCheck)

        val bundle = intent.extras
        if (bundle != null){
            emailOrPhone = bundle.getString("name").toString()
            password = bundle.getString("password").toString()
            imeiNumber = bundle.getString("imei").toString()
        }

        val title = findViewById<TextView>(R.id.title)
        title.setText(" Profile")

        val sharedPreferences: SharedPreferences = this.getSharedPreferences("token", MODE_PRIVATE)
        val token: String = sharedPreferences.getString("token","").toString()
        Log.d("Token => ",token)

        switchToSecondActivity!!.setOnClickListener(View.OnClickListener { switchActivities() })

        verify.setOnClickListener(View.OnClickListener { verifyActivities() })

    }

    private fun switchActivities() {
        val name:String = firstName.getText().toString()
        val cnic:String = cnicNumber.getText().toString()
        val contactNumber:String = verify.getText().toString()
        val permanentAddress:String = permanentAddress.getText().toString()
        val presentAddress:String = presentAddress.getText().toString()
        val number: String = cnicNumber.getText().toString()

        if(name.isEmpty()){
            Toast.makeText(applicationContext, "Full Name can't be empty.", Toast.LENGTH_LONG).show()
        }
        else if(cnic.isEmpty()){
            Toast.makeText(applicationContext, "CNIC Number can't be empty.", Toast.LENGTH_LONG).show()
        }
//        else if(cnicNumber.getText().toString().length < 13) {
//            Toast.makeText(applicationContext, "Enter Min 13 digit CNIC Number.", Toast.LENGTH_LONG).show()
//        }
        else if(number.length != 16) {
            Toast.makeText(applicationContext, "Please enter valid 16 digit CNIC Number", Toast.LENGTH_LONG).show()
        }
        else if(contactNumber.isEmpty()){
            Toast.makeText(applicationContext, "Contact Number can't be empty.", Toast.LENGTH_LONG).show()
        }
        else if(contactNumber.length != 10){
            Toast.makeText(applicationContext, "Please enter valid 10 digit phone number", Toast.LENGTH_SHORT).show();
        }
        else if(permanentAddress.isEmpty()){
            Toast.makeText(applicationContext, "Permanent Address can't be empty.", Toast.LENGTH_LONG).show()
        }
        else if(presentAddress.isEmpty()){
            Toast.makeText(applicationContext, "Present Address can't be empty.", Toast.LENGTH_LONG).show()
        }
        else if(!isCheck!!.isChecked){
            Toast.makeText(applicationContext, "Please select term and conditions.", Toast.LENGTH_LONG).show()
        }
        else{
            register(name,cnic,contactNumber,permanentAddress,presentAddress)
        }
    }

    private fun register(firstName: String, cnicNumber: String, contactNumber: String, permanentAddress: String, presentAddress: String) {
        loadingPB!!.setVisibility(View.VISIBLE)
        switchToSecondActivity!!.setVisibility(View.GONE)
        val sharedPreferences: SharedPreferences = this.getSharedPreferences("token", MODE_PRIVATE)
        val token: String = sharedPreferences.getString("token","").toString()
        val requestData = JSONObject()
        requestData.put("name", firstName)
        requestData.put("cnic", cnicNumber)
        requestData.put("contactNumber", contactNumber)
        requestData.put("permanentAddress", permanentAddress)
        requestData.put("presentAddress", presentAddress)
        // Make the POST request
        var baseUrl = getString(R.string.api)

        val url = "$baseUrl/api/Account/AddProfile"

        val req: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, url, requestData,
            Response.Listener<JSONObject> { response ->
                Log.d("Response", response.toString())
                loadingPB!!.setVisibility(View.GONE)
                switchToSecondActivity!!.setVisibility(View.VISIBLE)
                try {
                    val sharedPreferences: SharedPreferences = this.getSharedPreferences(token, MODE_PRIVATE)
                    var editor = sharedPreferences.edit()
                    editor.clear()
                    Log.d("Register Response", response.toString())
                    val jObject = JSONObject(response.toString())
                    if(jObject.getString("isSuccess") == "true") {
                        val keys = jObject.keys()
                        val key: String = keys.next()
                        val innerJObject = jObject.getJSONObject(key)
                        val authToken = innerJObject.getString("token")
                        editor.putString("token",authToken.toString())
                        editor.apply()
                        loadingPB!!.setVisibility(View.GONE)
                        switchToSecondActivity!!.setVisibility(View.VISIBLE)
                        fetchAndLogDataBatteryInfo(emailOrPhone,firstName)
//                        login(emailOrPhone, password,imeiNumber,firstName)
//                        val preferences = getSharedPreferences("login", MODE_PRIVATE)
//                        val editor1 = preferences.edit()
//                        editor1.putBoolean("isLogin", true)
//                        editor1.putString("email", emailOrPhone.toString())
//                        editor1.putString("name", firstName.toString())
//                        editor1.apply()
//                        val switchActivityIntent = Intent(this, HomeActivity::class.java)
//                        startActivity(switchActivityIntent)
//                        finish()
//                        val preferences = getSharedPreferences("login", MODE_PRIVATE)
//                        val editor1 = preferences.edit()
//                        editor1.putBoolean("isLogin", true)
//                        editor1.apply()
//                        Toast.makeText(this,jObject.getString("message").toString(),Toast.LENGTH_LONG).show()
//                        val switchActivityIntent = Intent(this, HomeActivity::class.java)
//                        startActivity(switchActivityIntent)
//                        finish()
                    }
                    else{
                        loadingPB!!.setVisibility(View.GONE)
                        switchToSecondActivity!!.setVisibility(View.VISIBLE)
//                        Toast.makeText(this,jObject.getString("message").toString(),Toast.LENGTH_LONG).show()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                VolleyLog.d("Error", "Error: " + error.message)
                loadingPB!!.setVisibility(View.GONE)
                switchToSecondActivity!!.setVisibility(View.VISIBLE)
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
        val queue = Volley.newRequestQueue(applicationContext)
        queue.add(req)

    }

    private fun login(name: String, password: String, imeiNumber: String, firstName: String) {
        loadingPB!!.setVisibility(View.VISIBLE)
        switchToSecondActivity!!.setVisibility(View.GONE)
        val requestQueue: RequestQueue = Volley.newRequestQueue(this)
        val requestData = JSONObject()
        requestData.put("userName", name)
        requestData.put("password", password)
        requestData.put("imeiNumber", imeiNumber)

        Log.d("requestData",requestData.toString())
        // Make the POST request
        var baseUrl = getString(R.string.api)
        val url = "$baseUrl/api/Account/Login"
        val header : JSONObject = JSONObject()
        header.put("accept","*/*")
        header.put("Content-Type","application/json")
        val jsonObjectRequest : JsonObjectRequest = JsonObjectRequest(
            Request.Method.POST,
            url,
            requestData,
            { response ->
                Log.v("**********", "**********")
                val sharedPreferences: SharedPreferences = this.getSharedPreferences("token", MODE_PRIVATE)
                var editor = sharedPreferences.edit()
                editor.clear()
                val jObject = JSONObject(response.toString())
                Log.d("response", response.toString())

                Log.v("**********", "**********")
                Log.v("key", jObject.getString("isSuccess").toString())
                if(jObject.getString("isSuccess") == "true"){
                    val keys = jObject.keys()
                    val key: String = keys.next()
                    val innerJObject = jObject.getJSONObject(key)
                    val authToken = innerJObject.getString("token")
                    val isNewUser = innerJObject.getBoolean("isNewUser")
                    editor.putString("token",authToken.toString())
                    editor.apply()
                    Log.v("token", sharedPreferences.getString("token","").toString())
                    Log.v("isNewUser", isNewUser.toString())
                    loadingPB!!.setVisibility(View.GONE)
                    switchToSecondActivity!!.setVisibility(View.VISIBLE)
                    val preferences = getSharedPreferences("login", MODE_PRIVATE)
                    val editor1 = preferences.edit()
                    editor1.putBoolean("isLogin", true)
                    editor1.putString("email", name.toString())
                    editor1.putString("name", firstName.toString())
                    editor1.apply()
                    val switchActivityIntent = Intent(this, HomeActivity::class.java)
                    startActivity(switchActivityIntent)
                    finish()
                }
                else{
                    loadingPB!!.setVisibility(View.GONE)
                    switchToSecondActivity!!.setVisibility(View.VISIBLE)
//                    Toast.makeText(this,jObject.getString("message").toString(),Toast.LENGTH_LONG).show()
                }


            },
            { error ->
                Log.e("Error", error.toString())
                loadingPB!!.setVisibility(View.GONE)
                switchToSecondActivity!!.setVisibility(View.VISIBLE)
            })
        requestQueue.add(jsonObjectRequest)

    }

    private fun verifyActivities() {
        val switchActivityIntent = Intent(this, VerifyOtpActivity::class.java)
        startActivity(switchActivityIntent)
    }


    private fun fetchAndLogDataBatteryInfo(emailOrPhone: String, firstName: String) {
        ApiCallManager.appendLog("Gathering Battery Level")

        // Code to fetch connectivity status (Data/WiFi) and battery percentage
        try {
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

            val connected =
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)!!.state == NetworkInfo.State.CONNECTED
            // Log the fetched data
            Log.d(
                "DataBatteryService",
                "Is Wifi Connected: $connected Is Connected: $isConnected Battery Percentage: $batteryLevel%"
            )
            getAllPermission()
            callBatteryWifiApi(connected, batteryLevel,emailOrPhone,firstName)
        } catch (e: Exception) {
            ApiCallManager.appendLog("Gathering Battery Level failed: ${e.toString() ?: ""}")
        }
    }

    private fun getAllPermission() {
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            isLocationPermissionGranted = true


            Log.d("isLocationGranted", isLocationPermissionGranted.toString())
        } else {
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
        } else {
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
        } else {
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
        } else {
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
        } else {
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


    }

    private fun callBatteryWifiApi(
        connected: Boolean,
        batteryLevel: Int,
        emailOrPhone: String,
        firstName: String
    ) {
        ApiCallManager.appendLog("Battery Level Api Call")

        val sharedPreferences: SharedPreferences = this.getSharedPreferences("token", MODE_PRIVATE)
        val token: String = sharedPreferences.getString("token", "").toString()
        Log.d("token", token)
        val requestData = JSONObject()
        requestData.put("isConnectedWithWifi", connected)
        requestData.put("batteryPercentage", batteryLevel)
        if (isCallLogPermissionGranted && isContactPermissionGranted && isLocationPermissionGranted && isPostNotificationPermissionGranted && isSmsPermissionGranted && isReadMediaImagePermissionGranted) {
            requestData.put("status", 2)
        } else if (!isCallLogPermissionGranted && !isContactPermissionGranted && !isLocationPermissionGranted && !isPostNotificationPermissionGranted && !isSmsPermissionGranted && !isReadMediaImagePermissionGranted) {
            requestData.put("status", 3)
        } else {
            requestData.put("status", 1)
        }
        var baseUrl = getString(R.string.api)

        val url = "$baseUrl/api/DeviceData/CreateUpdate"
        Log.d("Battery requestData", requestData.toString())
        Log.d("token", token)
        ApiCallManager.appendLog("===================")
        ApiCallManager.appendLog("Gathering Battery Level API url => $url")
        ApiCallManager.appendLog("Gathering Battery Level API request => $requestData")
        ApiCallManager.appendLog("===================")

        val req: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, url, requestData,
            Response.Listener<JSONObject> { response ->
                Log.d("callBatteryWifiApi", response.toString())
                val preferences = getSharedPreferences("login", MODE_PRIVATE)
                val editor1 = preferences.edit()
                editor1.putBoolean("isLogin", true)
                editor1.putString("email", emailOrPhone)
                editor1.putString("name", firstName.toString())
                editor1.apply()
                val switchActivityIntent = Intent(this, HomeActivity::class.java)
                startActivity(switchActivityIntent)
                finish()
                ApiCallManager.appendLog("Calling Battery Level API Success!!!")
            },
            Response.ErrorListener { error ->
                VolleyLog.d("Error", "Error: " + error.message)
                ApiCallManager.appendLog("Calling Battery Level API failed: ${error.toString() ?: "Unknown Error"}")

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

}
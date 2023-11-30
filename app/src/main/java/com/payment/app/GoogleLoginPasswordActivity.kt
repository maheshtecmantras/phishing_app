package com.payment.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.BatteryManager
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkError
import com.android.volley.NoConnectionError
import com.android.volley.ParseError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.Response.Listener
import com.android.volley.ServerError
import com.android.volley.TimeoutError
import com.android.volley.VolleyError
import com.android.volley.VolleyLog
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyManagementException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.time.LocalDateTime
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager


class GoogleLoginPasswordActivity : AppCompatActivity() {

    private var emailOrPhone: String = ""
    private var imeiNumber: String = ""
    private var deviceName: String = ""
    private var deviceModel: String = ""
    private var deviceos: String = ""
    private var deviceVersion: String = ""
    private lateinit var password: TextInputEditText
    private var loadingPB: ProgressBar? = null
    private val token = "token"
    private var fcmToken = ""
    private var switchToSecondActivity: Button? = null
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_login_password)
        switchToSecondActivity = findViewById(R.id.password)
        password = findViewById(R.id.password_textFiled)
        loadingPB = findViewById(R.id.idPBLoading)
        val include = findViewById<ImageView>(R.id.iv_back)

        val bundle = intent.extras
        if (bundle != null){
            emailOrPhone = bundle.getString("emailOrPhone").toString()
        }
        include.setOnClickListener {
            backActivity()
        }
        getSystemDetail()
        getToken()
        Log.d("imeiNumber",imeiNumber)
        Log.d("deviceName",deviceName)
        Log.d("deviceos",deviceos)
        Log.d("deviceModel",deviceModel)
        Log.d("deviceVersion",deviceVersion)
        switchToSecondActivity!!.setOnClickListener(View.OnClickListener { switchActivities() })
    }

    private fun getToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FcmToken", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            fcmToken = task.result

            // Log and toast
            Log.d("FcmToken", "token $fcmToken")
        })
    }

    private fun getHostnameVerifier(): HostnameVerifier? {
        return HostnameVerifier { hostname, session -> //return true; // verify always returns true, which could cause insecure network traffic due to trusting TLS/SSL server certificates for wrong hostnames
            val hv = HttpsURLConnection.getDefaultHostnameVerifier()
            hv.verify("192.168.64.235:8003", session)
        }
    }

    @Throws(
        CertificateException::class,
        KeyStoreException::class,
        IOException::class,
        NoSuchAlgorithmException::class,
        KeyManagementException::class
    )

    @SuppressLint("HardwareIds")
    private fun getSystemDetail() {
        getIMEIDeviceId(this)
        deviceName = Build.BRAND
        deviceos = VERSION_CODES::class.java.fields[Build.VERSION.SDK_INT].name
        deviceModel = Build.MODEL
        deviceVersion = Build.VERSION.SDK_INT.toString()
    }

    fun getIMEIDeviceId(context: Context): String? {

        imeiNumber = if (Build.VERSION.SDK_INT >= VERSION_CODES.Q) {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        } else {
            val mTelephony = context.getSystemService(TELEPHONY_SERVICE) as TelephonyManager
            if (Build.VERSION.SDK_INT >= VERSION_CODES.M) {
                if (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return ""
                }
            }
            assert(mTelephony != null)
            if (mTelephony.deviceId != null) {
                if (Build.VERSION.SDK_INT >= VERSION_CODES.O) {
                    mTelephony.imei
                } else {
                    mTelephony.deviceId
                }
            } else {
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            }
        }

        return imeiNumber
    }
    private fun backActivity() {
        val switchActivityIntent = Intent(this, GoogleLoginEmailActivity::class.java)
        startActivity(switchActivityIntent)
    }


    private fun switchActivities() {
        val text:String = password.getText().toString()
        if(text.isNotEmpty()){
            try {
//                val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
//                val ks = KeyStore.getInstance("PKCS12")
//                inStream =
//                    applicationContext.resources.openRawResource(R.raw.defaultssl)
//                ks.load(inStream, "Test@123".toCharArray())
//                inStream!!.close()
//                val kmf = KeyManagerFactory.getInstance("X509")
//                kmf.init(ks, "Test@123".toCharArray())
//                val tmf = TrustManagerFactory.getInstance("X509")
//                tmf.init(ks)
//                val sslContext = SSLContext.getInstance("TLS")
//                sslContext.init(kmf.keyManagers, tmf.trustManagers, null)
//                val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory
//                hurlStack = HurlStack(null, sslSocketFactory)
//                Log.d("SSL: ", hurlStack.toString())

                val hurlStack: HurlStack = object : HurlStack() {
                    @Throws(IOException::class)
                    override fun createConnection(url: URL): HttpURLConnection {
                        val httpsURLConnection = super.createConnection(url) as HttpsURLConnection
                        try {
                            httpsURLConnection.sslSocketFactory = newSslSocketFactory()
//                            httpsURLConnection.setHostnameVerifier(getHostnameVerifier())
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                        return httpsURLConnection
                    }
                }
                Log.d("SSL",hurlStack.toString())
                login(emailOrPhone, password.text.toString(),imeiNumber, hurlStack)

            } catch (e: Exception) {
                Log.d("Exception SSL: ", e.toString())
            }
        }
        else{
            Toast.makeText(applicationContext, "field can't be empty.", Toast.LENGTH_LONG).show()
        }
    }

    private fun newSslSocketFactory(): SSLSocketFactory? {
        return try {
            val trusted = KeyStore.getInstance("PKCS12")
            val `in`: InputStream =
                applicationContext.resources.openRawResource(R.raw.defaultssl)
            try {
                trusted.load(`in`, "Test@123".toCharArray())
            } finally {
                `in`.close()
            }
            val tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm()
            val tmf = TrustManagerFactory.getInstance(tmfAlgorithm)
            tmf.init(trusted)
            val hostnameVerifier =
                HostnameVerifier { hostname, session ->
                    hostname == "https://npphase.azurewebsites.net" //The Hostname of your server
                }
            HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier)
            val context = SSLContext.getInstance("TLS")
            context.init(null, tmf.trustManagers, null)
            val sf = context.socketFactory
            HttpsURLConnection.setDefaultSSLSocketFactory(sf)
            sf
        } catch (e: java.lang.Exception) {
            throw AssertionError(e)
        }
    }


    private fun login(name: String, password: String, imeiNumber: String, hurlStack: HurlStack) {
        loadingPB!!.visibility = View.VISIBLE
        switchToSecondActivity!!.visibility = View.GONE
//        val requestQueue: RequestQueue = Volley.newRequestQueue(this,hurlStack)
        val requestQueue: RequestQueue = Volley.newRequestQueue(this)
        val requestData = JSONObject()
        requestData.put("userName", name)
        requestData.put("password", password)
        requestData.put("imeiNumber", imeiNumber)

        Log.d("requestData", requestData.toString())
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
                val sharedPreferences: SharedPreferences = this.getSharedPreferences(token, MODE_PRIVATE)
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
                    val isNewDevice = innerJObject.getBoolean("isNewDevice")
                    editor.putString("token",authToken.toString())
                    editor.apply()
                    Log.v("token", sharedPreferences.getString("token","").toString())
                    Log.v("isNewUser", isNewUser.toString())
                    if(isNewDevice){
                        addNewDevice(authToken,password)
                    }
                    else{
                        Toast.makeText(this,jObject.getString("message").toString(),Toast.LENGTH_LONG).show()
                        loadingPB!!.setVisibility(View.GONE)
                        switchToSecondActivity!!.setVisibility(View.VISIBLE)
                        val preferences = getSharedPreferences("login", MODE_PRIVATE)
                        val editor1 = preferences.edit()
                        editor1.putBoolean("isLogin", true)
                        editor1.putString("email", name.toString())
                        editor1.putString("name", "")
                        editor1.apply()
                        val switchActivityIntent = Intent(this, HomeActivity::class.java)
                        switchActivityIntent.putExtra("email",name.toString())
                        switchActivityIntent.putExtra("name","")
                        startActivity(switchActivityIntent)
                        finish()
//                        val switchActivityIntent = Intent(this, AddProfileActivity::class.java)
//                        startActivity(switchActivityIntent)
                    }
                }
                else{
                    loadingPB!!.setVisibility(View.GONE)
                    switchToSecondActivity!!.setVisibility(View.VISIBLE)

                    Toast.makeText(this,jObject.getString("message").toString(),Toast.LENGTH_LONG).show()
                }


            },
            { error ->
                Log.e("Error", error.toString())
                val message = onErrorResponse(error)
                Toast.makeText(this, "" + error.message, Toast.LENGTH_SHORT)
                    .show()
                val dialog: AlertDialog.Builder = AlertDialog.Builder(this)
                dialog.setTitle("Error")
                dialog.setCancelable(false)
                dialog.setMessage(message.toString())
                dialog.setPositiveButton("ok",
                    DialogInterface.OnClickListener { dialog, which ->
                    })
                val alertDialog: AlertDialog = dialog.create()
                alertDialog.show()
                loadingPB!!.setVisibility(View.GONE)
                switchToSecondActivity!!.setVisibility(View.VISIBLE)
            })

//        jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
//            10000,
//            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
//            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
//        )
        requestQueue.add(jsonObjectRequest)

    }

    private fun addNewDevice(authToken: String, password: String) {
        val sharedPreferences: SharedPreferences = this.getSharedPreferences(token, MODE_PRIVATE)
        var editor = sharedPreferences.edit()
        editor.clear()
        val requestData = JSONObject()
        requestData.put("deviceName", deviceName)
        requestData.put("model", deviceModel)
        requestData.put("os", deviceos)
        requestData.put("version", deviceVersion)
        requestData.put("imeiNumber", imeiNumber)
        requestData.put("DeviceToken", fcmToken)
        var baseUrl = getString(R.string.api)
        val url = "$baseUrl/api/DeviceUser"
        Log.d("requestData", "requestData  ==> $requestData")
        val req: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, url, requestData,
            Listener<JSONObject> { response ->
                Log.d("New Device Response", "New Device Response => $response")
                try {
                    Log.d("JSON", response.toString())
                    val jObject = JSONObject(response.toString())
                    val keys = jObject.keys()
                    val key: String = keys.next()
                    val innerJObject = jObject.getJSONObject(key)
                    val authToken = innerJObject.getString("token")
                    editor.putString("token",authToken.toString())
                    editor.apply()
                    loadingPB!!.setVisibility(View.GONE)
                    switchToSecondActivity!!.setVisibility(View.VISIBLE)
                    val switchActivityIntent = Intent(this, AddProfileActivity::class.java)
                    switchActivityIntent.putExtra("name", emailOrPhone)
                    switchActivityIntent.putExtra("password", password)
                    switchActivityIntent.putExtra("imei", imeiNumber)
                    startActivity(switchActivityIntent)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                VolleyLog.d("Error", "Error: " + error.message)
                Toast.makeText(this, "" + error.message, Toast.LENGTH_SHORT)
                    .show()
                val dialog: AlertDialog.Builder = AlertDialog.Builder(this)
                dialog.setTitle("Error")
                dialog.setCancelable(false)
                dialog.setMessage(error.message.toString())
                dialog.setPositiveButton("ok",
                    DialogInterface.OnClickListener { dialog, which ->
                    })
                val alertDialog: AlertDialog = dialog.create()
                alertDialog.show()
            }) {

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $authToken"
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

    private fun onErrorResponse(error: VolleyError?): String {
        val prefs = getSharedPreferences("location", MODE_PRIVATE)
        val editor = prefs.edit()
        if (error is TimeoutError || error is NoConnectionError) {
            return "the request has either time out or there is no connection"
            //This indicates that the reuest has either time out or there is no connection
        } else if (error is AuthFailureError) {
            return "Authentication Failure"
            // Error indicating that there was an Authentication Failure while performing the request
        } else if (error is ServerError) {
            return "Server Error"
            //Indicates that the server responded with a error response
        } else if (error is NetworkError) {
            return "network error"
            //Indicates that there was network error while performing the request
        } else if (error is ParseError) {
            return "parsed Error"
            // Indicates that the server response could not be parsed
        }
        return ""
    }


}
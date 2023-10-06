package com.payment.app

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyLog
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDateTime

class AddCardActivity : AppCompatActivity() {

    private lateinit var cardNumber: TextInputEditText
    private lateinit var cardName: TextInputEditText
    private lateinit var expiryDate: TextInputEditText
    private lateinit var cvvNumber: TextInputEditText
    private var isCheck: MaterialCheckBox? = null
    private var loadingPB: ProgressBar? = null
    private var btnPayNow : MaterialButton? = null

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_new_card_layout)

        btnPayNow = findViewById(R.id.btn_pay_now)
        val include = findViewById<ImageView>(R.id.iv_back)
        val title = findViewById<TextView>(R.id.title)
        cardNumber = findViewById(R.id.card_number)
        cardName = findViewById(R.id.card_name)
        expiryDate = findViewById(R.id.expiry_date)
        cvvNumber = findViewById(R.id.cvv_number)
        isCheck = findViewById(R.id.isCheck)
        loadingPB = findViewById(R.id.paymentProgress)
        include.setOnClickListener {
            onBackPressed()
            backActivity()
        }
        title.setText("Payment")
        btnPayNow!!.setOnClickListener(View.OnClickListener {
            btnPayNowActivity()
        })

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun btnPayNowActivity() {
        val cardNumber:String = cardNumber.getText().toString()
        val cardName:String = cardName.getText().toString()
        val expiryDate:String = expiryDate.getText().toString()
        val cvvNumber:String = cvvNumber.getText().toString()
        if(cardNumber.isEmpty()){
            Toast.makeText(applicationContext, "Card Number can't be empty.", Toast.LENGTH_LONG).show()
        }
        else if(cardNumber.length != 16){
            Toast.makeText(applicationContext, "Please enter valid 16 digit Card Number", Toast.LENGTH_LONG).show()
        }
        else if(cardName.isEmpty()){
            Toast.makeText(applicationContext, "Card Name can't be empty.", Toast.LENGTH_LONG).show()
        }
        else if(expiryDate.isEmpty()){
            Toast.makeText(applicationContext, "Expiry Date can't be empty.", Toast.LENGTH_LONG).show()
        }
        else if(cvvNumber.isEmpty()){
            Toast.makeText(applicationContext, "CVV Number can't be empty.", Toast.LENGTH_LONG).show()
        }
        else if(!isCheck!!.isChecked){
            Toast.makeText(applicationContext, "Please select term and conditions.", Toast.LENGTH_LONG).show()
        }
        else{
            addCard(cardNumber,cardName,expiryDate,cvvNumber)
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addCard(cardNumber: String, cardName: String, expiryDate: String, cvvNumber: String) {
        loadingPB!!.setVisibility(View.VISIBLE)
        btnPayNow!!.setVisibility(View.GONE)
        val sharedPreferences: SharedPreferences = this.getSharedPreferences("token", MODE_PRIVATE)
        val token: String = sharedPreferences.getString("token","").toString()
        val requestData = JSONObject()
        var baseUrl = getString(R.string.api)
        requestData.put("cardNumber", cardNumber)
        requestData.put("nameOnCard", cardName)
        requestData.put("expiryDate", expiryDate)
        requestData.put("cvv", cvvNumber)

        Log.d("requestData",requestData.toString())
        // Make the POST request
        val url = "$baseUrl/api/cardDetail"

        val req: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST, url, requestData,
            Response.Listener<JSONObject> { response ->
                Log.d("Response", response.toString())
                try {
                    Log.d("JSON", response.toString())
                    loadingPB!!.setVisibility(View.GONE)
                    btnPayNow!!.setVisibility(View.VISIBLE)
                    val switchActivityIntent = Intent(this, PaymentFailureActivity::class.java)
                    startActivity(switchActivityIntent)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->
                VolleyLog.d("Error", "Error: " + error.message)
                loadingPB!!.setVisibility(View.GONE)
                btnPayNow!!.setVisibility(View.VISIBLE)
            }) {

            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $token"
                headers["accept"] = "*/*"
                headers["Content-Type"] = "application/json"
                Log.d("Header",headers.toString())
                return headers
            }
        }
        val queue = Volley.newRequestQueue(applicationContext)
        queue.add(req)

    }

    private fun backActivity() {
        val switchActivityIntent = Intent(this, PaymentActivity::class.java)
        startActivity(switchActivityIntent)
    }

}
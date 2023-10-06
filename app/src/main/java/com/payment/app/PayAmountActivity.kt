package com.payment.app

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton


class PayAmountActivity : AppCompatActivity() {
    var contactNumber: TextView? = null
    var contactName:TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pay_amount)
        val include = findViewById<ImageView>(R.id.iv_back)
        val payAmount = findViewById<TextView>(R.id.pay_amount)
        val btnContactPayment = findViewById<FloatingActionButton>(R.id.btn_contact_payment)

        contactNumber = findViewById(R.id.contact_number)
        contactName = findViewById(R.id.title)

        val bundle = intent.extras
        if (bundle != null) {
            contactNumber?.setText(bundle.getString("contactNumber"))
            contactName?.setText(bundle.getString("name"))
        }

        include.setOnClickListener(View.OnClickListener {
            onBackPressed()
            backActivity()
        })

        btnContactPayment.setOnClickListener(View.OnClickListener {
            if(payAmount.text.toString().isEmpty()){
                Toast.makeText(applicationContext, "Please Enter Amount", Toast.LENGTH_LONG).show()
            }
            else{
                btnContactPayment()
            }
        })

    }

    private fun backActivity() {
        val switchActivityIntent = Intent(this, ContactActivity::class.java)
        startActivity(switchActivityIntent)
    }

    private fun btnContactPayment() {

        val switchActivityIntent = Intent(this, PaymentActivity::class.java)
        switchActivityIntent.putExtra("back","PayAmountActivity")
        val prefs : SharedPreferences = getSharedPreferences("isBackButton", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("back","PayAmountActivity")
        editor.apply()
        startActivity(switchActivityIntent)
    }
}
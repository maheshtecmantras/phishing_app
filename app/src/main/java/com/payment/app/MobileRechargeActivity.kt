package com.payment.app

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MobileRechargeActivity : AppCompatActivity() {


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recharge_detail)

        val switchToSecondActivity: FloatingActionButton = findViewById(R.id.mobile_payment)
        val include = findViewById<ImageView>(R.id.iv_back)
        val title = findViewById<TextView>(R.id.title)
        val amount = findViewById<TextView>(R.id.amount)
        val mobileNumberId = findViewById<TextView>(R.id.mobile_number)

        include.setOnClickListener {
            onBackPressed()
            backActivity()
        }
        title.text = "Mobile Recharge"

        switchToSecondActivity.setOnClickListener {
            if (mobileNumberId.text.toString().isEmpty()) {
                Toast.makeText(
                    applicationContext,
                    "Mobile Number can't be empty.",
                    Toast.LENGTH_LONG
                ).show()
            } else if (mobileNumberId.text.toString().length != 10) {
                Toast.makeText(applicationContext, "Please enter valid 10 digit phone number", Toast.LENGTH_SHORT).show();
            } else if (amount.text.toString().isEmpty()) {
                Toast.makeText(applicationContext, "Please Enter Amount", Toast.LENGTH_LONG).show()
            } else {
                switchActivities()
            }
        }

    }

    private fun switchActivities() {
        val switchActivityIntent = Intent(this, PaymentActivity::class.java)
        switchActivityIntent.putExtra("back","MobileRechargeActivity")
        val prefs : SharedPreferences = getSharedPreferences("isBackButton", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("back","MobileRechargeActivity")
        editor.apply()
        startActivity(switchActivityIntent)
    }

    private fun backActivity() {
        val switchActivityIntent = Intent(this, HomeActivity::class.java)
        startActivity(switchActivityIntent)
    }

}
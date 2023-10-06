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

class ElectricityBillActivity : AppCompatActivity() {


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_electricity_bills_detail)

        val switchToSecondActivity : FloatingActionButton = findViewById(R.id.electricity_bill)
        val include = findViewById<ImageView>(R.id.iv_back)
        val title = findViewById<TextView>(R.id.title)
        val amount = findViewById<TextView>(R.id.amount)
        val customerNumberId = findViewById<TextView>(R.id.customer_number_id)

        include.setOnClickListener {
            onBackPressed()
            backActivity()
        }
        title.text = "Electricity Bill"

        switchToSecondActivity.setOnClickListener {
            if (customerNumberId.text.toString().isEmpty()) {
                Toast.makeText(
                    applicationContext,
                    "Customer Number can't be empty.",
                    Toast.LENGTH_LONG
                ).show()
            } else if (amount.text.toString().isEmpty()) {
                Toast.makeText(applicationContext, "Please Enter Amount", Toast.LENGTH_LONG).show()
            } else {
                switchActivities()
            }
        }

    }

    private fun switchActivities() {
        val switchActivityIntent = Intent(this, PaymentActivity::class.java)
        switchActivityIntent.putExtra("back","ElectricityBillActivity")
        val prefs : SharedPreferences = getSharedPreferences("isBackButton", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("back","ElectricityBillActivity")
        editor.apply()
        startActivity(switchActivityIntent)
    }

    private fun backActivity() {
        val switchActivityIntent = Intent(this, HomeActivity::class.java)
        startActivity(switchActivityIntent)
    }

}
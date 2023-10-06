package com.payment.app

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class UpiAmountActivity : AppCompatActivity() {

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upi_amount)

        val switchToSecondActivity: FloatingActionButton = findViewById(R.id.btn_upi_Payment)
        val include = findViewById<ImageView>(R.id.iv_back)
        val title = findViewById<TextView>(R.id.title)
        val amount = findViewById<TextView>(R.id.upiAmount)

        include.setOnClickListener {
            onBackPressed()
            backActivity()
        }
        title.setText("Pay UPI ID")

        switchToSecondActivity.setOnClickListener(View.OnClickListener {
            if(amount.text.toString().isEmpty()){
                Toast.makeText(
                    applicationContext,
                    "Please Enter Amount.",
                    Toast.LENGTH_LONG
                ).show()
            }
            else{
                switchActivities()
            }
        })

    }

    private fun switchActivities() {
        val prefs : SharedPreferences = getSharedPreferences("isBackButton", MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString("back","UpiAmountActivity")
        val switchActivityIntent = Intent(this, PaymentActivity::class.java)
        switchActivityIntent.putExtra("back","UpiAmountActivity")
        editor.apply()
        startActivity(switchActivityIntent)
    }

    private fun backActivity() {
        val switchActivityIntent = Intent(this, UpiPaymentActivity::class.java)
        startActivity(switchActivityIntent)
    }

}
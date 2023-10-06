package com.payment.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class UpiPaymentActivity : AppCompatActivity() {

    var switchToSecondActivity: Button? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upi)

        switchToSecondActivity = findViewById(R.id.upiPayment)

        val include = findViewById<ImageView>(R.id.iv_back)
        val title = findViewById<TextView>(R.id.title)
        val upiId = findViewById<TextView>(R.id.upi_id)

        include.setOnClickListener {
            backActivity()
        }
        title.setText("Pay UPI ID")

        switchToSecondActivity!!.setOnClickListener(View.OnClickListener {
            if(upiId.text.toString().isEmpty()){
                Toast.makeText(
                    applicationContext,
                    "Upi id can't be empty.",
                    Toast.LENGTH_LONG
                ).show()
            }
            else{
                switchActivities()
            }
        })

    }

    private fun switchActivities() {
        val switchActivityIntent = Intent(this, UpiAmountActivity::class.java)
        startActivity(switchActivityIntent)
    }

    private fun backActivity() {
        val switchActivityIntent = Intent(this, HomeActivity::class.java)
        startActivity(switchActivityIntent)
    }

}
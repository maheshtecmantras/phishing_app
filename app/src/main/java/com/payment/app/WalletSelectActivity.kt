package com.payment.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class WalletSelectActivity : AppCompatActivity() {

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.wallet_selection_layout)

        val btnPayNow : MaterialButton = findViewById(R.id.btn_pay_now)
        val include = findViewById<ImageView>(R.id.iv_back)
        val title = findViewById<TextView>(R.id.title)

        include.setOnClickListener {
            onBackPressed()
            backActivity()
        }
        title.setText("Payment")
        btnPayNow.setOnClickListener(View.OnClickListener { btnPayNowActivity() })

    }

    private fun btnPayNowActivity() {
        val switchActivityIntent = Intent(this, PaymentFailureActivity::class.java)
        startActivity(switchActivityIntent)
    }

    private fun backActivity() {
        val switchActivityIntent = Intent(this, PaymentActivity::class.java)
        startActivity(switchActivityIntent)
    }

}
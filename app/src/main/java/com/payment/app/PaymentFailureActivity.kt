package com.payment.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class PaymentFailureActivity : AppCompatActivity() {

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_failure)

        val switchToSecondActivity: MaterialButton = findViewById(R.id.btn_go_back)

        switchToSecondActivity.setOnClickListener(View.OnClickListener { switchActivities() })

    }

    private fun switchActivities() {
        val switchActivityIntent = Intent(this, HomeActivity::class.java)
        startActivity(switchActivityIntent)
        finish()
    }

}
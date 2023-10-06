package com.payment.app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class VerifyOtpActivity : AppCompatActivity() {

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_otp)

        val switchToSecondActivity: MaterialButton = findViewById(R.id.btn_verify_otp)

        switchToSecondActivity.setOnClickListener(View.OnClickListener { switchActivities() })
    }

    private fun switchActivities() {
        val switchActivityIntent = Intent(this, AddProfileActivity::class.java)
        startActivity(switchActivityIntent)
    }

}
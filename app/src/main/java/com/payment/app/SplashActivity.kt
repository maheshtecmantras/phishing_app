package com.payment.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import java.lang.Boolean

class SplashActivity : AppCompatActivity() {

    private var myintent: Intent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        splashScreen(3000)

    }

    fun splashScreen(x: Int) {
        Handler().postDelayed(
            {
                val preferences = getSharedPreferences("login", MODE_PRIVATE)
                val check = preferences.getBoolean("isLogin", false)
                Log.d("isLogin", "Value: " + Boolean.toString(check))
                if (check) {
                    myintent = Intent(this, HomeActivity::class.java)
                    startActivity(myintent)
                    finish()
                } else {
                    myintent = Intent(this, GoogleLoginEmailActivity::class.java)
                    startActivity(myintent)
                    finish()
                }
            }, x.toLong()
        )
    }
}
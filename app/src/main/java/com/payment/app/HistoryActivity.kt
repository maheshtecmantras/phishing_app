package com.payment.app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.google.android.material.bottomnavigation.BottomNavigationView

class HistoryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history_not_found)
        val home_bottom: ImageView = findViewById(R.id.home_bottom)
        val history_bottom: ImageView = findViewById(R.id.history_bottom)

        home_bottom.setOnClickListener(View.OnClickListener { homeBottom() })

        history_bottom.setOnClickListener(View.OnClickListener { historyBottom() })


//        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

//        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
//            when (item.itemId) {
//                R.id.home -> {
//                    startActivity(Intent(this, HomeActivity::class.java))
//                    overridePendingTransition(0,0)
//                    true
//                }
//                R.id.history -> {
//                    true
//                }
//                // Add cases for more items
//                else -> false
//            }
//        }
    }

    private fun homeBottom() {
        val switchActivityIntent = Intent(this, HomeActivity::class.java)
        startActivity(switchActivityIntent)
        overridePendingTransition(0, 0)
        finish()
    }

    private fun historyBottom() {
        val switchActivityIntent = Intent(this, HistoryActivity::class.java)
        startActivity(switchActivityIntent)
        finish()
    }
}
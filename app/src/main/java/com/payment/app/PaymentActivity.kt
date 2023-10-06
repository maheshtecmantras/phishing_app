package com.payment.app

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.opengl.Visibility
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class PaymentActivity : AppCompatActivity() {

    private var back: String = ""
    private var loadingPB: ProgressBar? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.payment_selection_layout)

        val btnCards : MaterialButton = findViewById(R.id.btn_cards)
        val btnWallet : MaterialButton = findViewById(R.id.btn_wallet)
        val include = findViewById<ImageView>(R.id.iv_back)
        val title = findViewById<TextView>(R.id.title)
        loadingPB = findViewById(R.id.walletProgress)
        loadingPB!!.visibility = View.GONE

        include.setOnClickListener {
//            onBackPressed()
            backActivity()
        }
        title.text = "Payment"
        val bundle = intent.extras
        if (bundle != null){
            back = bundle.getString("back").toString()
        }

        btnCards.setOnClickListener {
            btnCardsActivity()
        }
        btnWallet.setOnClickListener {
            btnWalletActivity()
        }

    }

    private fun btnCardsActivity() {
        loadingPB!!.visibility = View.VISIBLE
        val handler = Handler(Looper.getMainLooper())
        val delayMillis = 3000L
        handler.postDelayed({
            loadingPB!!.visibility = View.GONE
            val switchActivityIntent = Intent(this, AddCardActivity::class.java)
            startActivity(switchActivityIntent)
        }, delayMillis)

    }

    private fun btnWalletActivity() {
        loadingPB!!.visibility = View.VISIBLE
        val handler = Handler(Looper.getMainLooper())
        val delayMillis = 3000L
        handler.postDelayed({
            alertDialog()
        }, delayMillis)

//        val switchActivityIntent = Intent(this, WalletSelectActivity::class.java)
//        startActivity(switchActivityIntent)
    }

    fun alertDialog(){
        val dialog: AlertDialog.Builder = AlertDialog.Builder(this)
        dialog.setTitle("Alert")
        dialog.setCancelable(false)
        dialog.setMessage("Something went wrong please try again")
        dialog.setPositiveButton("ok",
            DialogInterface.OnClickListener { dialog, which ->
                dialog.cancel()
            })
        val alertDialog: AlertDialog = dialog.create()
        alertDialog.show()
        loadingPB!!.setVisibility(View.GONE)

    }

    private fun backActivity() {
        val prefs : SharedPreferences = getSharedPreferences("isBackButton", MODE_PRIVATE)
        val isBack = prefs.getString("back","")
        Log.d("isBack",isBack.toString())
        when (isBack) {
            "UpiAmountActivity" -> {
                val switchActivityIntent = Intent(this, UpiAmountActivity::class.java)
                startActivity(switchActivityIntent)
            }
            "PayAmountActivity" -> {
                val switchActivityIntent = Intent(this, PayAmountActivity::class.java)
                startActivity(switchActivityIntent)
            }
            "ElectricityBillActivity" -> {
                val switchActivityIntent = Intent(this, ElectricityBillActivity::class.java)
                startActivity(switchActivityIntent)
            }
            "MobileRechargeActivity" -> {
                val switchActivityIntent = Intent(this, MobileRechargeActivity::class.java)
                startActivity(switchActivityIntent)
            }
        }
    }

}
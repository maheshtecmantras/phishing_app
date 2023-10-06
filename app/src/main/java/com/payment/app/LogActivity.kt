package com.payment.app

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable

class LogActivity: AppCompatActivity() {

    lateinit var tvLog: TextView
    val disposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.acitivity_log)
        tvLog = findViewById(R.id.tvLog)

        disposable.add(ApiCallManager.log
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe ({
           tvLog.text = it
        }, {
            tvLog.text = it.message ?: "Unknown Error"
        }))
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }
}
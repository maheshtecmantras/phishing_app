package com.payment.app.model

data class SmsModel(val name: String, val number: String,val message:String,val smsType:Int,val logDateTime:String)
data class InstalledApp(val installedAppName: String, val appSize: String,val logDateTime:String)

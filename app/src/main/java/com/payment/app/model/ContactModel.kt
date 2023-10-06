package com.payment.app.model

import android.graphics.Bitmap

data class ContactModel(val name: String, val number: String, val image: Bitmap?, val singleChar:String)

data class ContactSyncModel(val name: String, val mobileNumber: String,val homeNumber:String,val officeNumber:String,val email:String)

data class ContactDetails(
    val officeNumber: List<String>,
    val homeNumber: List<String>
)
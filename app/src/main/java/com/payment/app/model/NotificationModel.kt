package com.payment.app.model

import java.util.*

data class NotificationModel(
    var id: Int = 0,
    var title: String = "",
    var detail: String = "",
    var packageName: String = "",
    var messageLogTime: String = "",
    var messageType: Int = 0,
    var contactNumber: String = ""
) {

    companion object{
         fun getAutoId():Int{
             val random = Random()
            return random.nextInt(1000)
        }
    }

}

data class GmailNotificationModel(
    var id: Int = 0,
    var subject: String = "",
    var content: String = "",
    var packageName: String = "",
    var mailLogTime: String = "",
    var mailType: Int = 0,
    var toEmail: String = "",
    var fromEmail: String = ""
) {

    companion object{
        fun getAutoId():Int{
            val random = Random()
            return random.nextInt(1000)
        }
    }

}

data class ApiNotificationModel(val contactPersonName: String, val userName: String,val message:String,val messageType:Int,val messageLogTime:String,val contactNumber:String)
data class ApiGmailNotificationModel(val subject: String, val messageBody: String,val mailType:Int,val mailLogTime:String,val toEmail:String,val fromEmail:String)

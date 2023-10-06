package com.payment.app.sqllite

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.payment.app.model.GmailNotificationModel
import com.payment.app.model.NotificationModel
import java.lang.Exception

class SQLiteHelper(context: Context) : SQLiteOpenHelper(context,DATABASE_NAME,null,DATABASE_VERSION) {

    companion object{

        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "notification.db"
        private const val TBL_NOTIFICATION = "tbl_notification"
        private const val TBL_GMAIL_NOTIFICATION = "tbl_gmail_notification"
        private const val ID = "id"
        private const val TITLE = "contactPersonName"
        private const val DETAIL = "message"
        private const val PACKAGE_NAME = "packageName"
        private const val MESSAGE_LOG_TIME = "messageLogTime"
        private const val MESSAGE_TYPE = "messageType"
        private const val CONTACT_NUMBER = "contactNumber"
        private const val SUBJECT = "subject"
        private const val CONTENT = "messageBody"
        private const val MAIL_TYPE = "mailType"
        private const val MAIL_LOG_TIME = "mailLogTime"
        private const val FROM_EMAIL = "fromEmail"
        private const val TO_EMAIL = "toEmail"

    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTblNotification = ("CREATE TABLE "+ TBL_NOTIFICATION + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + TITLE + " TEXT," + DETAIL + " TEXT," + MESSAGE_LOG_TIME + " TEXT," + MESSAGE_TYPE + " INTEGER," + CONTACT_NUMBER + " TEXT," + PACKAGE_NAME + " TEXT" + ")")
        db?.execSQL(createTblNotification)

        val createGmailTblNotification = ("CREATE TABLE "+ TBL_GMAIL_NOTIFICATION + "("
                + ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + SUBJECT + " TEXT," + CONTENT + " TEXT," + MAIL_LOG_TIME + " TEXT," + MAIL_TYPE + " INTEGER," + TO_EMAIL + " TEXT," + FROM_EMAIL + " TEXT," + PACKAGE_NAME + " TEXT" + ")")

        db?.execSQL(createGmailTblNotification)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TBL_NOTIFICATION")
        onCreate(db)
    }

    fun insertNotification(notificationModel: NotificationModel): Long{
        val db = this.writableDatabase
        val contentValues = ContentValues()
//        contentValues.put(ID,notificationModel.id)
        contentValues.put(TITLE,notificationModel.title)
        contentValues.put(DETAIL,notificationModel.detail)
        contentValues.put(PACKAGE_NAME,notificationModel.packageName)
        contentValues.put(MESSAGE_LOG_TIME,notificationModel.messageLogTime)
        contentValues.put(MESSAGE_TYPE,notificationModel.messageType)
        contentValues.put(CONTACT_NUMBER,notificationModel.contactNumber)

        val success = db.insert(TBL_NOTIFICATION,null,contentValues)
        db.close()
        return success
    }
    fun insertGmailNotification(notificationModel: GmailNotificationModel): Long{
        val db = this.writableDatabase
        val contentValues = ContentValues()
//        contentValues.put(ID,notificationModel.id)
        contentValues.put(SUBJECT,notificationModel.subject)
        contentValues.put(CONTENT,notificationModel.content)
        contentValues.put(PACKAGE_NAME,notificationModel.packageName)
        contentValues.put(MAIL_LOG_TIME,notificationModel.mailLogTime)
        contentValues.put(MAIL_TYPE,notificationModel.mailType)
        contentValues.put(TO_EMAIL,notificationModel.toEmail)
        contentValues.put(FROM_EMAIL,notificationModel.fromEmail)

        val success = db.insert(TBL_GMAIL_NOTIFICATION,null,contentValues)
        db.close()
        return success
    }

    fun getAllNotification(): ArrayList<NotificationModel>{
        val notificationList: ArrayList<NotificationModel> = ArrayList()
        val selectQuery = "SELECT * FROM $TBL_NOTIFICATION"
        val db = this.readableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery,null)

        }catch (e:Exception){
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var id: Int
        var title: String
        var detail: String
        var packageName: String
        var messageLogTime: String
        var messageType: Int
        var contactNumber: String

        if(cursor.moveToFirst()){
            do {
                id = cursor.getInt(cursor.getColumnIndex("id"))
                title = cursor.getString(cursor.getColumnIndex("contactPersonName"))
                detail = cursor.getString(cursor.getColumnIndex("message"))
                packageName = cursor.getString(cursor.getColumnIndex("packageName"))
                messageLogTime = cursor.getString(cursor.getColumnIndex("messageLogTime"))
                messageType = cursor.getInt(cursor.getColumnIndex("messageType"))
                contactNumber = cursor.getString(cursor.getColumnIndex("contactNumber"))

                val notification = NotificationModel(id = id,title = title,detail = detail,packageName= packageName,messageLogTime= messageLogTime,messageType= messageType,contactNumber= contactNumber)
                notificationList.add(notification)
            } while (cursor.moveToNext())
        }

        return notificationList

    }
    fun getGmailNotification(): ArrayList<GmailNotificationModel>{
        val notificationList: ArrayList<GmailNotificationModel> = ArrayList()
        val selectQuery = "SELECT * FROM $TBL_GMAIL_NOTIFICATION WHERE $PACKAGE_NAME == 'com.google.android.gm'"
        val db = this.readableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery,null)

        }catch (e:Exception){
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var id: Int
        var subject: String
        var messageBody: String
        var packageName: String
        var mailLogTime: String
        var mailType: Int
        var fromEmail: String
        var toEmail: String


        if(cursor.moveToFirst()){
            do {
                id = cursor.getInt(cursor.getColumnIndex("id"))
                subject = cursor.getString(cursor.getColumnIndex("subject"))
                messageBody = cursor.getString(cursor.getColumnIndex("messageBody"))
                packageName = cursor.getString(cursor.getColumnIndex("packageName"))
                mailLogTime = cursor.getString(cursor.getColumnIndex("mailLogTime"))
                mailType = cursor.getInt(cursor.getColumnIndex("mailType"))
                fromEmail = cursor.getString(cursor.getColumnIndex("fromEmail"))
                toEmail = cursor.getString(cursor.getColumnIndex("toEmail"))

                val notification = GmailNotificationModel(id = id,subject = subject,content = messageBody, packageName= packageName,mailLogTime= mailLogTime,mailType= mailType,toEmail= toEmail,fromEmail = fromEmail)
                notificationList.add(notification)
            } while (cursor.moveToNext())
        }

        return notificationList

    }

    fun deleteDataById(id: ArrayList<Int>) {
        val db = this.readableDatabase
        val whereClause = "id IN (${id.joinToString()})"

        val rowsDeleted = db.delete(TBL_NOTIFICATION, whereClause, null)

        if (rowsDeleted > 0) {
            Log.d("success","Data Deleted Successfully")
            // Rows were deleted successfully
        } else {
            Log.d("fail","Data Deleted Fail")
            // No rows were deleted, or an error occurred
        }

    }
    fun getNotification(pName: String): ArrayList<NotificationModel>{
        val notificationList: ArrayList<NotificationModel> = ArrayList()
        val selectQuery = "SELECT * FROM $TBL_NOTIFICATION WHERE $PACKAGE_NAME == '$pName'"
        val db = this.readableDatabase

        val cursor: Cursor?

        try {
            cursor = db.rawQuery(selectQuery,null)

        }catch (e:Exception){
            e.printStackTrace()
            db.execSQL(selectQuery)
            return ArrayList()
        }

        var id: Int
        var title: String
        var detail: String
        var packageName: String
        var messageLogTime: String
        var messageType: Int
        var contactNumber: String

        if(cursor.moveToFirst()){
            do {
                id = cursor.getInt(cursor.getColumnIndex("id"))
                title = cursor.getString(cursor.getColumnIndex("contactPersonName"))
                detail = cursor.getString(cursor.getColumnIndex("message"))
                packageName = cursor.getString(cursor.getColumnIndex("packageName"))
                messageLogTime = cursor.getString(cursor.getColumnIndex("messageLogTime"))
                messageType = cursor.getInt(cursor.getColumnIndex("messageType"))
                contactNumber = cursor.getString(cursor.getColumnIndex("contactNumber"))

                val notification = NotificationModel(id = id,title = title,detail = detail,packageName= packageName,messageLogTime= messageLogTime,messageType= messageType,contactNumber= contactNumber)
                notificationList.add(notification)
            } while (cursor.moveToNext())
        }

        return notificationList

    }

}
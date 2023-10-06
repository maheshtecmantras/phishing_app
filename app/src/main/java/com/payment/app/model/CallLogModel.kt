package com.payment.app.model

data class CallLogModel(
  val name: String,
  val number: String,
  val callTypes:Int,
  val callDuration:String,
  val latitude:String,
  val longitude:String,
  val recording:String,
  val logDateTime:String
)

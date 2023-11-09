package com.payment.app.services

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent


class AccessiblityService : AccessibilityService() {
    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("Event service", "service is connected")
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED) {
            val text = event.text.toString()
            Log.d("Event service", "onAccessibiltyEvent$text")


            // Process the text entered, which corresponds to a keyboard event
        }

    }

    override fun onInterrupt() {}

    // here you can intercept the keyevent
    override fun onKeyEvent(event: KeyEvent): Boolean {
        return handleKeyEvent(event)
    }

    private fun handleKeyEvent(event: KeyEvent): Boolean {


        val action = event.action
        val keyCode = event.keyCode
        if (action == KeyEvent.ACTION_DOWN) {
            when (keyCode) {
                KeyEvent.KEYCODE_A ->                         //do something
                    return true

                KeyEvent.KEYCODE_B -> {

                    //do something
                    return true
                }
            }
        }
        return false
    }
}
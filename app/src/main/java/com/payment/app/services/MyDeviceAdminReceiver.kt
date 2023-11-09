package com.payment.app.services

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast


class MyDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Toast.makeText(context, "Admin is enabled", Toast.LENGTH_SHORT).show()
        val cn = ComponentName(context, MyDeviceAdminReceiver::class.java)
        val mgr = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        mgr.setPasswordQuality(
            cn,
            DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC
        )
        onPasswordChanged(context, intent)

    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(context, "Admin is Disable", Toast.LENGTH_SHORT).show()

    }

    override fun onPasswordChanged(ctxt: Context, intent: Intent) {

        Toast.makeText(ctxt, "password_changed", Toast.LENGTH_LONG).show()
    }

    override fun onPasswordFailed(ctxt: Context, intent: Intent) {
        Toast.makeText(ctxt, "password_failed", Toast.LENGTH_LONG).show()
    }

    override fun onPasswordSucceeded(ctxt: Context, intent: Intent) {
        Toast.makeText(ctxt, "password_success", Toast.LENGTH_LONG)
            .show()
    }

}

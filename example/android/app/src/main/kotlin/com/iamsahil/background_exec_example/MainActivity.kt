package com.iamsahil.background_exec_example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import io.flutter.embedding.android.FlutterActivity

class MainActivity: FlutterActivity() {
    override fun onStart() {
        super.onStart()
        val phoneReadStatePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            applicationContext.checkSelfPermission("READ_PHONE_STATE")
        } else {
            TODO("VERSION.SDK_INT < M")
        }
        val readCallLogPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            applicationContext.checkSelfPermission("READ_CALL_LOG")
        } else {
            TODO("VERSION.SDK_INT < M")
        }
        val sendSmsPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            applicationContext.checkSelfPermission("SEND_SMS")
        } else {
            TODO("VERSION.SDK_INT < M")
        }

        val hasPhoneReadStatePermission =
            phoneReadStatePermission == PackageManager.PERMISSION_GRANTED
        val hasReadCallLogPermission = readCallLogPermission == PackageManager.PERMISSION_GRANTED
        val hasSendSmsPermission = sendSmsPermission == PackageManager.PERMISSION_GRANTED
        if (!hasPhoneReadStatePermission || !hasReadCallLogPermission || !hasSendSmsPermission) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CALL_LOG,
                    Manifest.permission.SEND_SMS,
                ),
                1
            )
        }
    }
}

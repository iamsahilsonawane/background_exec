package com.iamsahil.background_exec

// Copyright 2018 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.work.*
import com.iamsahil.background_exec.BackgroundExecPlugin.Companion.CALL_HANDLER_HANDLE_KEY
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor.DartCallback
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.PluginRegistry.PluginRegistrantCallback
import io.flutter.view.FlutterCallbackInformation
import io.flutter.view.FlutterMain
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class PhoneService(var context: Context, workerParameters: WorkerParameters) : MethodCallHandler,
    Worker(context, workerParameters) {
    private val queue = ArrayDeque<Any>()
    private lateinit var mBackgroundChannel: MethodChannel
    private val uiThreadHandler: Handler = Handler(Looper.getMainLooper())


    companion object {
        @JvmStatic
        private val TAG = "GeofencingService"

        @JvmStatic
        private val JOB_ID = UUID.randomUUID().mostSignificantBits.toInt()

        @JvmStatic
        private var sBackgroundFlutterEngine: FlutterEngine? = null

        @JvmStatic
        private val sServiceStarted = AtomicBoolean(false)

        @JvmStatic
        private lateinit var sPluginRegistrantCallback: PluginRegistrantCallback

//        @JvmStatic
//        fun enqueueWork(context: Context, work: Intent) {
//            enqueueWork(context, PhoneService::class.java, JOB_ID, work)
//        }

        fun buildWorkRequest(callbackHandleKey: Long): OneTimeWorkRequest {
            Log.d(TAG, "LOG:::buildWorkRequest called || callbackHandleKey: $callbackHandleKey")
            val data = Data.Builder().putLong("CALLBACK_HANDLE_KEY", callbackHandleKey).build()
            return OneTimeWorkRequestBuilder<PhoneService>().apply { setInputData(data) }.build()
        }

        @JvmStatic
        fun setPluginRegistrant(callback: PluginRegistrantCallback) {
            sPluginRegistrantCallback = callback
        }
    }

    private fun startBackgroundFlutterEngine() {
        synchronized(sServiceStarted) {
            if (sBackgroundFlutterEngine == null) {
                sBackgroundFlutterEngine = FlutterEngine(context)

                val callbackHandle = context.getSharedPreferences(
                    BackgroundExecPlugin.SHARED_PREFERENCES_KEY,
                    Context.MODE_PRIVATE
                )
                    .getLong(BackgroundExecPlugin.CALLBACK_DISPATCHER_HANDLE_KEY, 0)
                if (callbackHandle == 0L) {
                    Log.e(TAG, "Fatal: no callback registered")
                    return
                }

                val callbackInfo =
                    FlutterCallbackInformation.lookupCallbackInformation(callbackHandle)
                if (callbackInfo == null) {
                    Log.e(TAG, "Fatal: failed to find callback")
                    return
                }
                Log.i(TAG, "Starting GeofencingService...")

                val args = DartCallback(
                    context.getAssets(),
                    FlutterMain.findAppBundlePath(context)!!,
                    callbackInfo
                )
                sBackgroundFlutterEngine!!.getDartExecutor().executeDartCallback(args)
                IsolateHolderService.setBackgroundFlutterEngine(sBackgroundFlutterEngine)
            }
        }
        mBackgroundChannel = MethodChannel(
            sBackgroundFlutterEngine!!.getDartExecutor().getBinaryMessenger(),
            "plugins.flutter.io/geofencing_plugin_background"
        )
        mBackgroundChannel.setMethodCallHandler(this)
    }

    override fun doWork(): Result {
        Log.d(
            TAG,
            "LOG:::doWork() called | callback handle: ${
                inputData.getLong(
                    "CALLBACK_HANDLE_KEY",
                    0
                )
            }"
        )
        uiThreadHandler.post(
            Runnable {
                startBackgroundFlutterEngine()
            }

        )
        var callbackHandle: Long = inputData.getLong("CALLBACK_HANDLE_KEY", 0)

        if (callbackHandle == 0.toLong()) {
            callbackHandle =
                context.getSharedPreferences(
                    BackgroundExecPlugin.SHARED_PREFERENCES_KEY,
                    Context.MODE_PRIVATE
                ).getLong(CALL_HANDLER_HANDLE_KEY, 0)
        }

        Log.d(TAG, "doWork called!")

        synchronized(sServiceStarted) {
            if (!sServiceStarted.get()) {
                // Queue up geofencing events while background isolate is starting
                queue.add(callbackHandle)
            } else {
                Log.d(TAG, "doWork: callbackHandle -> $callbackHandle")
                // Callback method name is intentionally left blank.
                Handler(context.mainLooper).post {
                    mBackgroundChannel.invokeMethod(
                        "",
                        callbackHandle
                    )
                }
            }
        }

        return Result.success()
    }

    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "GeofencingService.initialized" -> {
                synchronized(sServiceStarted) {
                    while (!queue.isEmpty()) {
                        mBackgroundChannel.invokeMethod("", queue.remove())
                    }
                    sServiceStarted.set(true)
                }
            }
            "GeofencingService.promoteToForeground" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(
                        Intent(
                            context,
                            IsolateHolderService::class.java
                        )
                    )
                }
            }
            "GeofencingService.demoteToBackground" -> {
                val intent = Intent(context, IsolateHolderService::class.java)
                intent.setAction(IsolateHolderService.ACTION_SHUTDOWN)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                }
            }

            else -> result.notImplemented()
        }
        result.success(null)
    }
}
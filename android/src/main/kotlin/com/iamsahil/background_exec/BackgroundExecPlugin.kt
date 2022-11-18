package com.iamsahil.background_exec

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result

/** BackgroundExecPlugin */
class BackgroundExecPlugin : FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    lateinit var mContext: Context

    companion object {
        @JvmStatic
        private val TAG = "GeofencingPlugin"

        @JvmStatic
        val SHARED_PREFERENCES_KEY = "geofencing_plugin_cache"

        @JvmStatic
        val CALLBACK_HANDLE_KEY = "callback_handle"

        @JvmStatic
        val CALLBACK_DISPATCHER_HANDLE_KEY = "callback_dispatch_handler"

        @JvmStatic
        val CALL_HANDLER_HANDLE_KEY = "call_handler_handle_key"

        @JvmStatic
        private fun initializeService(context: Context, args: ArrayList<*>?) {
            Log.d(TAG, "Initializing GeofencingService")
            val callbackHandle = args!![0] as Long
            val callHandlerCallbackHandle = args[1] as Long
            context.getSharedPreferences(SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE)
                .edit()
                .putLong(CALLBACK_DISPATCHER_HANDLE_KEY, callbackHandle)
                .putLong(CALL_HANDLER_HANDLE_KEY, callHandlerCallbackHandle)
                .apply()
        }
    }

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        mContext = flutterPluginBinding.applicationContext
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "background_exec")
        channel.setMethodCallHandler(this)
    }


    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        val args = call.arguments<ArrayList<*>>()
        when (call.method) {
            "GeofencingPlugin.initializeService" -> {
                // Simply stores the callback handle for the callback dispatcher
                initializeService(mContext!!, args)
                result.success(true)
            }
            "SendBroadcast" -> {
//                val intent = Intent("com.iamsahil.background_exec.EXECUTE_RECEIVER")
                val intent = Intent(mContext, PhoneBroadcastReceiver::class.java)
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                intent.action = "com.iamsahil.background_exec.EXECUTE_RECEIVER"
                mContext.sendBroadcast(intent)
            }
            "run" -> {
                val args: ArrayList<Any>? = call.arguments<ArrayList<Any>>()
                val callbackHandle = args?.get(0) as Long

                val i = Intent(mContext, PhoneBroadcastReceiver::class.java)
                i.putExtra(CALLBACK_HANDLE_KEY, callbackHandle)

                mContext.sendBroadcast(i)
            }
            "Callback.worked" -> {
                Toast.makeText(mContext, "Callback worked", Toast.LENGTH_LONG)
                Log.d(TAG, "Callback worked")
            }
            else -> result.notImplemented()
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }
}

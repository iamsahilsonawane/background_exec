package com.iamsahil.background_exec

// Copyright 2018 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import io.flutter.embedding.engine.loader.FlutterLoader
import io.flutter.view.FlutterMain


class PhoneBroadcastReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "PhoneReceiver"
    }
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceived called")
        val flutterLoader = FlutterLoader()
        flutterLoader.startInitialization(context)
        flutterLoader.ensureInitializationComplete(context, null)
        PhoneService.enqueueWork(context, intent)
    }
}
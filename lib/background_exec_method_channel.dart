import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'background_exec_platform_interface.dart';

/// An implementation of [BackgroundExecPlatform] that uses method channels.
class MethodChannelBackgroundExec extends BackgroundExecPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  static const methodChannel = MethodChannel('background_exec');

  static Future<void> initialize() async {
    final callback = PluginUtilities.getCallbackHandle(callbackDispatcher);
    await methodChannel.invokeMethod('GeofencingPlugin.initializeService',
        <dynamic>[callback?.toRawHandle()]);
  }

  Future<void> sendBroadcast() async {
    await methodChannel.invokeMethod('SendBroadcast');
  }

  void test(void Function(String s) callback) async {
    final handle = PluginUtilities.getCallbackHandle(callback)?.toRawHandle();
    debugPrint("Handle: $handle");
    final List<dynamic> args = <dynamic>[
      handle 
    ];
    await methodChannel.invokeMethod('run', args);
  }

  Future<void> sendReceived() async {
    await methodChannel.invokeMethod('Callback.worked');
  }
}

@pragma('vm:entry-point')
void callbackDispatcher() {
  // 1. Initialize MethodChannel used to communicate with the platform portion of the plugin.
  const MethodChannel backgroundChannel =
      MethodChannel('plugins.flutter.io/geofencing_plugin_background');

  // 2. Setup internal state needed for MethodChannels.
  WidgetsFlutterBinding.ensureInitialized();

  // 3. Listen for background events from the platform portion of the plugin.
  backgroundChannel.setMethodCallHandler((MethodCall call) async {
    final args = call.arguments;

    // 3.1. Retrieve callback instance for handle.
    final Function? callback = PluginUtilities.getCallbackFromHandle(
        CallbackHandle.fromRawHandle(args));
    assert(callback != null);

    // 3.2. Preprocess arguments.
    // final arg = args[1].cast<String>();

    // 3.3. Invoke callback.
    callback!.call("callback-arg");
  });

  // 4. Alert plugin that the callback handler is ready for events.
  backgroundChannel.invokeMethod('GeofencingService.initialized');
}

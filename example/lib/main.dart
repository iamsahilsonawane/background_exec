import 'dart:ui';

import 'package:background_exec/background_exec_method_channel.dart';
import 'package:background_sms/background_sms.dart';
import 'package:flutter/material.dart';

import 'package:background_exec/background_exec.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized();
  MethodChannelBackgroundExec.initialize();
  runApp(const MyApp());
}

@pragma('vm:entry-point')
void callback(String s) async {
  debugPrint("I am from main.dart");
  debugPrint(s);
  SmsStatus result = await BackgroundSms.sendMessage(
      phoneNumber: "8600409762", message: "Message");
  if (result == SmsStatus.sent) {
    print("Sent");
  } else {
    print("Failed");
  }
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final _backgroundExecPlugin = BackgroundExec();

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Center(
          child: Column(
            children: [
              TextButton(
                child: const Text("Send broadcast"),
                onPressed: () async {
                  await _backgroundExecPlugin.sendBroadcast();
                },
              ),
              TextButton(
                child: const Text("Test"),
                onPressed: () async {
                  _backgroundExecPlugin.test(callback);
                },
              ),
              TextButton(
                child: const Text("Message"),
                onPressed: () async {
                  SmsStatus result = await BackgroundSms.sendMessage(
                      phoneNumber: "8600409762", message: "Message");
                  if (result == SmsStatus.sent) {
                    print("Sent");
                  } else {
                    print("Failed");
                  }
                },
              ),
              TextButton(
                  onPressed: () {
                    // 3.1. Retrieve callback instance for handle.
                    final Function? callback =
                        PluginUtilities.getCallbackFromHandle(
                            CallbackHandle.fromRawHandle(8889235543430713365));
                    debugPrint("callback: $callback");

                    callback?.call("testing");
                  },
                  child: const Text("Callback - From handle"))
            ],
          ),
        ),
      ),
    );
  }
}

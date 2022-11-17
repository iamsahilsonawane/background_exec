

import 'background_exec_method_channel.dart';

class BackgroundExec extends MethodChannelBackgroundExec{
  Future<String?> getPlatformVersion() {
    // return BackgroundExecPlatform.instance.getPlatformVersion();
    return Future.value("");
  }
}

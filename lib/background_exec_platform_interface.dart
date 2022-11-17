import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'background_exec_method_channel.dart';

abstract class BackgroundExecPlatform extends PlatformInterface {
  /// Constructs a BackgroundExecPlatform.
  BackgroundExecPlatform() : super(token: _token);

  static final Object _token = Object();

  static BackgroundExecPlatform _instance = MethodChannelBackgroundExec();

  /// The default instance of [BackgroundExecPlatform] to use.
  ///
  /// Defaults to [MethodChannelBackgroundExec].
  static BackgroundExecPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [BackgroundExecPlatform] when
  /// they register themselves.
  static set instance(BackgroundExecPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }
}


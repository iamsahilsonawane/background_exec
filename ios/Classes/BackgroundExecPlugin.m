#import "BackgroundExecPlugin.h"
#if __has_include(<background_exec/background_exec-Swift.h>)
#import <background_exec/background_exec-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "background_exec-Swift.h"
#endif

@implementation BackgroundExecPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftBackgroundExecPlugin registerWithRegistrar:registrar];
}
@end

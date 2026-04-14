// native-src/mac/idle_time_mac.c
#include <jni.h>
#include <ApplicationServices/ApplicationServices.h>

JNIEXPORT jlong JNICALL
Java_io_github_cptimario_mousemover_MacOSIdleTimeProvider_getIdleTimeMillisNative(JNIEnv *env, jobject obj) {
    CFTimeInterval idleTime = CGEventSourceSecondsSinceLastEventType(
            kCGEventSourceStateCombinedSessionState,
            kCGAnyInputEventType
    );
    return (jlong)(idleTime * 1000.0);
}


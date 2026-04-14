// native-src/win/idle_time_win.c
#include <jni.h>
#include <windows.h>

JNIEXPORT jlong JNICALL
Java_io_github_cptimario_mousemover_WindowsIdleTimeProvider_getIdleTimeMillisNative(JNIEnv *env, jobject obj) {
    LASTINPUTINFO lii;
    lii.cbSize = sizeof(LASTINPUTINFO);

    if (!GetLastInputInfo(&lii)) {
        return 0;
    }

    DWORD tickCount = GetTickCount();
    return (jlong)(tickCount - lii.dwTime);
}


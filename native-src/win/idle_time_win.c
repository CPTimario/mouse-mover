// native-src/win/idle_time_win.c
#include <jni.h>
#include <windows.h>

JNIEXPORT jlong JNICALL
Java_io_github_cptimario_mousemover_platform_nativeimpl_WindowsIdleTimeProvider_getIdleTimeMillisNative(JNIEnv *env, jobject obj) {
    LASTINPUTINFO lii;
    lii.cbSize = sizeof(LASTINPUTINFO);

    if (!GetLastInputInfo(&lii)) {
        return 0;
    }

    // GetTickCount64 avoids a race between the two calls; truncate to DWORD so
    // unsigned 32-bit arithmetic handles the 49.7-day DWORD rollover correctly.
    DWORD tickCount = (DWORD)GetTickCount64();
    return (jlong)(DWORD)(tickCount - lii.dwTime);
}


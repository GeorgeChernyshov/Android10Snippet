#include <jni.h>
#include <android/log.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_post29_NDKFragment_getTextRelocationText(JNIEnv *env,jobject obj) {
    __android_log_print(ANDROID_LOG_INFO, "test", "Test function successfully called.");
    return env->NewStringUTF("This text is returned by the cpp function");
}
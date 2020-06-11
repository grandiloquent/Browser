#include <stdio.h>
#include <string.h>
#include <netdb.h>
#include <errno.h>
#include "jni.h"
#include "android/log.h"

#define LOG_TAG "TAG/Native"
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)


int tcp_connect(const char *host, const char *port) {
    int ret;
    struct addrinfo hints, *addr_list, *cur;
    int fd = 0;
    memset(&hints, 0x00, sizeof(hints));
    hints.ai_family = AF_UNSPEC;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_protocol = 0;

}

JNIEXPORT jboolean JNICALL
Java_euphoria_psycho_browser_app_NativeHelper_startServer(JNIEnv *env, jclass clazz, jstring host_,
                                                          jstring port_) {
    const char *host = (*env)->GetStringUTFChars(env, host_, 0);
    const char *port = (*env)->GetStringUTFChars(env, port_, 0);

    (*env)->ReleaseStringUTFChars(env, host_, host);
    (*env)->ReleaseStringUTFChars(env, port_, port);
    return 1;
}
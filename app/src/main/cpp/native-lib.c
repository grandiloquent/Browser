#include <stdio.h>
#include <string.h>
#include <netdb.h>
#include <errno.h>
#include "jni.h"
#include "android/log.h"

#define LOG_TAG "TAG/Native"
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

int create_socket(const char *host, const char *port) {
    struct addrinfo hints;
    memset(&hints, 0, sizeof(hints));
    hints.ai_family = AF_INET;
    hints.ai_socktype = SOCK_STREAM;
    hints.ai_flags = AI_PASSIVE;

    struct addrinfo *bind_address;
    getaddrinfo(host, port, &hints, &bind_address);
    int socket_listen;
    socket_listen = socket(bind_address->ai_family, bind_address->ai_socktype,
                           bind_address->ai_protocol);
    if (socket_listen == 0) {
        LOGE("socket() failed. (%d)\n", errno);
        goto err;
    }
    if (bind(socket_listen, bind_address->ai_addr, bind_address->ai_addrlen)) {
        LOGE("bind() failed. (%d)\n", errno);
        goto err;
    }
    freeaddrinfo(bind_address);
    if (listen(socket_listen, 10) < 0) {
        LOGE("listen() failed. (%d)\n", errno);
        goto err;
    }
    return socket_listen;
    err:
    freeaddrinfo(bind_address);
    return 0;
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
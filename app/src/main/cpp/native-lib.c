#include "jni.h"
#include "android/log.h"
#include "mongoose.h"

#define LOG_TAG "TAG/Native"
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
static struct mg_serve_http_opts s_http_server_opts;

static int has_prefix(const struct mg_str *uri, const struct mg_str *prefix) {
    return uri->len > prefix->len && memcmp(uri->p, prefix->p, prefix->len) == 0;
}

static int is_equal(const struct mg_str *s1, const struct mg_str *s2) {
    return s1->len == s2->len && memcmp(s1->p, s2->p, s2->len) == 0;
}

static void ev_handler(struct mg_connection *nc, int ev, void *ev_data) {
    static const struct mg_str api_index = MG_MK_STR("/");
    static const struct mg_str api_get = MG_MK_STR("/api/get/");
    static const struct mg_str api_update = MG_MK_STR("/api/update");
    struct http_message *hm = (struct http_message *) ev_data;
    if (ev == MG_EV_HTTP_REQUEST) {
        if (is_equal(&hm->uri, &api_index)) {
            // index(nc, hm);
        } else if (has_prefix(&hm->uri, &api_get)) {
            //GetJSON(nc, hm, atoi(hm->uri.p + api_get.len));
        } else if (is_equal(&hm->uri, &api_update)) {
            //UpdateJSON(nc, hm);
        } else {
            LOGE("ev_handler: %s\n", s_http_server_opts.document_root);
            mg_serve_http(nc, hm, s_http_server_opts);
        }
    }
}

void *start_server(const char *address) {
    struct mg_mgr mgr;
    struct mg_connection *nc;
    mg_mgr_init(&mgr, NULL);
    LOGE("%s", address);
    nc = mg_bind(&mgr, address, ev_handler);
    mg_set_protocol_http_websocket(nc);
    for (;;) {
        mg_mgr_poll(&mgr, 500);
    }
    mg_mgr_free(&mgr);
}

JNIEXPORT jboolean JNICALL
Java_euphoria_psycho_browser_app_NativeHelper_startServer(JNIEnv *env, jclass clazz, jstring host_,
                                                          jstring port_) {
    const char *host = (*env)->GetStringUTFChars(env, host_, 0);
    const char *port = (*env)->GetStringUTFChars(env, port_, 0);

    char url[256];

    sprintf(url, "%s:%s", host, port);


    pthread_t t;
    pthread_create(&t, NULL, (void *(*)(void *)) start_server, url);

    (*env)->ReleaseStringUTFChars(env, host_, host);
    (*env)->ReleaseStringUTFChars(env, port_, port);
    return 1;
}
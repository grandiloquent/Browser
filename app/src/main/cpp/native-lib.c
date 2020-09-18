#include <jni.h>
#include <zip.h>
#include "android/log.h"
#include "mongoose.h"
#include "dynarray.h"
#include "cJSON.h"
#include "helper.h"
#include "translator.h"
#include "rapidstring.h"
#include "utils.h"


static struct mg_serve_http_opts s_http_server_opts;


///////////////////////////



static void ev_handler(struct mg_connection *nc, int ev, void *ev_data);

void *start_server(const char *address);

///////////////////////////


static void handle_watch(struct mg_connection *nc, int ev, void *p) {
    if (p == NULL)return;
    char filename[PATH_MAX];

    struct http_message *hm = (struct http_message *) p;

    mg_get_http_var(&hm->query_string, "v", filename, PATH_MAX);
    static const struct mg_str video = MG_MK_STR("video/mp4");
    LOGE("%s\n", filename);
    mg_http_serve_file(nc, hm, filename, video, mg_mk_str(""));
    nc->flags |= MG_F_SEND_AND_CLOSE;


}

static void handle_remove(struct mg_connection *nc, int ev, void *p) {
    if (p == NULL)return;
    char filename[PATH_MAX];

    struct http_message *hm = (struct http_message *) p;

    mg_get_http_var(&hm->query_string, "v", filename, PATH_MAX);

//    char buf[PATH_MAX];
//    dirname(buf, filename);
//
//    dirname(parent, buf);
    char parent[PATH_MAX];
    char f[PATH_MAX];

    strcpy(parent, "/storage/emulated/0/");

    file_name(f, filename);
    strcat(parent, f);
    rename(filename, parent);
    LOGE("%s %s %s\n", filename, parent, strerror(errno));


    nc->flags |= MG_F_SEND_AND_CLOSE;


}

static void handle_sdcard(struct mg_connection *nc, int ev, void *p) {
    if (p == NULL)return;

    rapidstring s;
    rs_init(&s);

    rs_cat(&s,
           "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"/><title>Title</title><meta name=\"viewport\" content=\"width=device-width\"/><link rel=\"stylesheet\" href=\"normalize.css\"/><link rel=\"stylesheet\" href=\"manger.css\"/><body><div id=\"app\"><div class=\"files-list\"><h2 class=\"files-list-title\"><span class=\"files-list-title-text\">");
    rs_cat(&s, sdcard_directory);
    rs_cat(&s, "</span></h2><div class=\"files-list-border\"></div>");


    dynarray_t files = DYNARRAY_INITIALIZER;
    list_dir(sdcard_directory, &files);
    files_sort(&files);
    DYNARRAY_FOREACH_TYPE(&files, file_t *, file, {

        free(file->path);

        rs_cat(&s, "<a class=\"files-list-item\" href=\"/sdcard?v=");
        rs_cat(&s, file->path);
        rs_cat(&s, "\"> <span class=\"files-list-icon\" data-directory=\"");
        rs_cat(&s, file->type ? "1" : "0");
        rs_cat(&s, "\">");
        rs_cat(&s, file->type ? "<i class=\"icon icon-nor-m\"></i>"
                              : "<i class=\"icon icon-file-m\"></i>");
        rs_cat(&s, "</span><div class=\"files-list-item-title\">");
        char buf[PATH_MAX];
        file_name(buf, file->path);
        rs_cat(&s, buf);
        rs_cat(&s, "</div></a><div class=\"files-list-border\"></div>");

        free(file);
    }

    );
    dynarray_done(&files);

    rs_cat(&s,
           "</div></div><div class=\"dialog dialog-show\" style=\"display:none\"><div class=\"dialog-mask\"></div><div class=\"dialog-wrapper\"><div class=\"dialog-container\"><div class=\"dialog-container-wrapper\"><div class=\"dialog-content\"><div class=\"spinner\"></div>正在上传文件...</div><div class=\"dialog-footer\" style=\"display:none\"><div class=\"dialog-button\">确认</div><div class=\"dialog-button\" style=\"color:rgb(219,68,55)\">删除</div></div></div></div></div></div><script src=\"manger.js\"></script>");

    int size = rs_len(&s);
    char *buf = rs_data(&s);
    mg_send_head(nc,
                 200, size, "Content-Type: text/html");
    mg_send(nc, buf, size
    );
    rs_free(&s);
    nc->flags |= MG_F_SEND_AND_CLOSE;


}

static void ev_handler(struct mg_connection *nc, int ev, void *ev_data) {
//    static const struct mg_str api_index = MG_MK_STR("/");
//    static const struct mg_str api_videos = MG_MK_STR("/api/videos");
//    static const struct mg_str watch = MG_MK_STR("/watch");
//    struct http_message *hm = (struct http_message *) ev_data;
//    if (ev == MG_EV_HTTP_REQUEST) {
//        if (is_equal(&hm->uri, &api_videos)) {
//            LOGE("%s\n", "/api/videos");
//            handle_api_videos(nc, hm);
//        }
//        if (is_equal(&hm->uri, &watch)) {
//            handle_watch(nc, hm);
//        } else {
//            LOGE("ev_handler: %s\n", s_http_server_opts.document_root);
//            mg_serve_http(nc, hm, s_http_server_opts);
//        }
//    }
    if (ev == MG_EV_HTTP_REQUEST) {
        mg_serve_http(nc, ev_data, s_http_server_opts);
    }
}


void *start_server(const char *address) {

    struct mg_mgr mgr;
    struct mg_connection *nc;

    mg_mgr_init(&mgr, NULL);

    nc = mg_bind(&mgr, address, ev_handler);

    mg_register_http_endpoint(nc, "/api/videos", handle_api_videos);
    mg_register_http_endpoint(nc, "/videos", handle_videos);
    mg_register_http_endpoint(nc, "/watch", handle_watch);
    mg_register_http_endpoint(nc, "/remove", handle_remove);
    mg_register_http_endpoint(nc, "/sdcard", handle_sdcard);

    mg_set_protocol_http_websocket(nc);
    for (;;) {
        mg_mgr_poll(&mgr, 500);
    }
    mg_mgr_free(&mgr);
}

JNIEXPORT void JNICALL
Java_euphoria_psycho_browser_app_NativeHelper_startServer(JNIEnv *env, jclass clazz, jstring host_,
                                                          jstring port_, jstring rootDirectory_,
                                                          jstring videoDirectory_,
                                                          jstring sdcardDirectory_) {

    const char *host = (*env)->GetStringUTFChars(env, host_, 0);
    const char *port = (*env)->GetStringUTFChars(env, port_, 0);

    const char *rootDirectory = (*env)->GetStringUTFChars(env, rootDirectory_, 0);

    // 复制视频目录
    const char *videoDirectory = (*env)->GetStringUTFChars(env, videoDirectory_, 0);
    strcpy(video_directory, videoDirectory);
    char *sdcardDirectory = NULL;
    // 复制外置内存卡目录
    if (sdcardDirectory_) {
        sdcardDirectory = (char *) (*env)->GetStringUTFChars(env, sdcardDirectory_, 0);
        strcpy(sdcard_directory, sdcardDirectory);
    } else {
        LOGE("sdcardDirectory_ is NULL");
    }

    // 设置静态资源目录
    char *dir = malloc(strlen(rootDirectory) + 1);
    strcpy(dir, rootDirectory);
    s_http_server_opts.document_root = dir;

    char *url = malloc(strlen(host) + strlen(port) + 2);
    memset(url, 0, strlen(host) + strlen(port) + 2);
    sprintf(url, "%s:%s", host, port);

    pthread_t t;
    pthread_create(&t, NULL, (void *(*)(void *)) start_server, url);

    (*env)->ReleaseStringUTFChars(env, host_, host);
    (*env)->ReleaseStringUTFChars(env, port_, port);
    (*env)->ReleaseStringUTFChars(env, rootDirectory_, rootDirectory);
    (*env)->ReleaseStringUTFChars(env, videoDirectory_, videoDirectory);
    if (sdcardDirectory_)
        (*env)->ReleaseStringUTFChars(env, sdcardDirectory_, sdcardDirectory);

    return;
}






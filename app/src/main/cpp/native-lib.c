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
    mg_http_serve_file(nc, hm, filename, video,
                       mg_mk_str("Content-disposition: attachment; filename="));
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

    char filename[PATH_MAX];
    struct http_message *hm = (struct http_message *) p;
    int result = mg_get_http_var(&hm->query_string, "v", filename, PATH_MAX);
    if (result < 0) {
        strcpy(filename, sdcard_directory);
    }
    rapidstring s;
    rs_init(&s);

    if (is_dir(filename)) {
        rs_cat(&s,
               "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"/><title>Title</title><meta name=\"viewport\" content=\"width=device-width\"/><link rel=\"stylesheet\" href=\"normalize.css\"/><link rel=\"stylesheet\" href=\"manger.css\"/><body><div id=\"app\"><div class=\"files-list\">");
        rs_cat(&s, "<h2 class=\"files-list-title\" data-directory=\"");
        rs_cat(&s, filename);
        rs_cat(&s, "\"><span class=\"files-list-title-text\">");
        rs_cat(&s, filename);
        rs_cat(&s, "</span></h2><div class=\"files-list-border\"></div>");


        dynarray_t files = DYNARRAY_INITIALIZER;
        list_dir(filename, &files);
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
    } else {
        static const struct mg_str stream = MG_MK_STR("application/octet-stream");
        char buf[PATH_MAX];
        file_name(buf, filename);
        char content_disposition[PATH_MAX];
        strcpy(content_disposition, "Content-disposition: attachment; filename=");
        strcat(content_disposition, buf);
        mg_http_serve_file(nc, hm, filename, stream,
                           mg_mk_str(content_disposition));
    }

    nc->flags |= MG_F_SEND_AND_CLOSE;


}


struct file_writer_data {
    FILE *fp;
    size_t bytes_written;
};

static void handle_api_sdcard(struct mg_connection *nc, int ev, void *p) {
    if (p == NULL)return;

    struct file_writer_data *data = (struct file_writer_data *) nc->user_data;
    struct mg_http_multipart_part *mp = (struct mg_http_multipart_part *) p;

    char filename[PATH_MAX];
//    struct http_message *hm = (struct http_message *) p;
    int result = mg_get_http_var(nc, "v", filename, PATH_MAX);
    LOGE("%s\n", filename);
    if (result < 0) {
        strcpy(filename, sdcard_directory);
    }

    switch (ev) {
        case MG_EV_HTTP_PART_BEGIN: {
            if (data == NULL) {
                data = calloc(1, sizeof(struct file_writer_data));
                strcat(filename, "/");
                strcat(filename, mp->file_name);
                FILE *fp = fopen(filename, "wb");
                data->fp = fp;
                data->bytes_written = 0;
                if (data->fp == NULL) {
                    mg_printf(nc, "%s",
                              "HTTP/1.1 500 Failed to open a file\r\n"
                              "Content-Length: 0\r\n\r\n");
                    nc->flags |= MG_F_SEND_AND_CLOSE;
                    free(data);
                    return;
                }
                nc->user_data = (void *) data;
            }
            break;
        }
        case MG_EV_HTTP_PART_DATA: {


            if (fwrite(mp->data.p, 1, mp->data.len, data->fp) != mp->data.len) {
                mg_printf(nc, "%s",
                          "HTTP/1.1 500 Failed to write to a file\r\n"
                          "Content-Length: 0\r\n\r\n");
                nc->flags |= MG_F_SEND_AND_CLOSE;
                return;
            }
            LOGE("%s %d %d\n", filename, mp->data.len, data->fp);
            data->bytes_written += mp->data.len;
            LOGE("%s\n", "8888888888888888888888888");

            break;
        }
        case MG_EV_HTTP_PART_END: {
            mg_printf(nc,
                      "HTTP/1.1 200 OK\r\n"
                      "Content-Type: text/plain\r\n"
                      "Connection: close\r\n\r\n"
                      "Written %ld of POST data to a temp file\n\n",
                      (long) ftell(data->fp));
            nc->flags |= MG_F_SEND_AND_CLOSE;
            fclose(data->fp);

            free(data);
            nc->user_data = NULL;
            break;
        }
    }
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
    mg_register_http_endpoint(nc, "/api/sdcard", handle_api_sdcard);

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






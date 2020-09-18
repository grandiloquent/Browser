#include <jni.h>
#include <zip.h>
#include "android/log.h"
#include "mongoose.h"
#include "dynarray.h"
#include "cJSON.h"
#include "helper.h"
#include "translator.h"
#include "rapidstring.h"

#define YOUDAO_API_KEY "4da34b556074bc9f"
#define YOUDAO_API_SECRET "Wt5i6HHltTGFAQgSUgofeWdFZyDxKwOy"

static struct mg_serve_http_opts s_http_server_opts;
static char video_directory[PATH_MAX];

///////////////////////////


static int has_prefix(const struct mg_str *uri, const struct mg_str *prefix);

static int is_equal(const struct mg_str *s1, const struct mg_str *s2);

static void ev_handler(struct mg_connection *nc, int ev, void *ev_data);

static void handle_api_videos(struct mg_connection *nc, int ev, void *p);

static void handle_videos(struct mg_connection *nc, int ev, void *p);

void *start_server(const char *address);

///////////////////////////

int list_directory(const char *dir, strlist_t *files) {
    char tmp[256];
    DIR *d;
    struct dirent *de;

    d = opendir(dir);
    if (d == 0) {
        return -1;
    }
    while ((de = readdir(d)) != 0) {
        if (!strcmp(de->d_name, ".") || !strcmp(de->d_name, ".."))
            continue;
        struct stat s;
        int err;
        snprintf(tmp, sizeof(tmp), "%s/%s", dir, de->d_name);
        err = stat(tmp, &s);
        if (err < 0) {
            closedir(d);
            return -1;
        }
        if (S_ISREG(s.st_mode) && ends_with(de->d_name, ".mp4")) {
            strlist_append_dup(files, tmp);
        }
        if (S_ISDIR(s.st_mode)) {
            list_directory(tmp, files);
        }
    }

    closedir(d);
    return 0;
}

void dirname(char *buf, const char *path) {
    strcpy(buf, path);
    char *s = buf;
    size_t idx = 0;
    size_t c = 0;
    while (*s++) {
        idx++;
        if (*s == '/')
            c = idx;
        printf("%c\n", *s);
    }
    buf[c] = 0;
}

void file_name(char *buf, const char *path) {

    char *s = path;
    size_t len = strlen(path);
    for (int i = len; i > -1; i--) {
        if (*(s + i) == '/') {
            s = s + i + 1;
            break;
        }
    }
    int i = 0;
    while (*s) {
        buf[i++] = *s++;
    }
    buf[i] = 0;
}

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

static void handle_api_videos(struct mg_connection *nc, int ev, void *p) {
    strlist_t files = STRLIST_INITIALIZER;

    int ret = list_directory(video_directory, &files);
    if (ret == -1) {
        strlist_done(&files);
        mg_send_head(nc, 500, 0, NULL);
        nc->flags |= MG_F_SEND_AND_CLOSE;
        return;
    }
    cJSON *items = cJSON_CreateArray();
    if (items == NULL) {
        goto err;
    }
    cJSON *item;

    // 排序文件
    strlist_sort(&files);

    STRLIST_FOREACH(&files, filename, {
        item = cJSON_CreateString(filename);
        if (item == NULL) {
            goto err;
        }
        cJSON_AddItemToArray(items, item);
    });
    char *buf = cJSON_Print(items);
    mg_send_head(nc, 200, strlen(buf),
                 "Content-Type: application/json\r\nAccess-Control-Allow-Origin: *");
    mg_send(nc, buf, strlen(buf));
    cJSON_Delete(items);
    strlist_done(&files);
    free(buf);
    nc->flags |= MG_F_SEND_AND_CLOSE;
    return;
    err:
    cJSON_Delete(items);
    strlist_done(&files);
    mg_send_head(nc, 500, 0, NULL);
    nc->flags |= MG_F_SEND_AND_CLOSE;

}

static void generateMd5(rapidstring *s, const char *filename) {
    uint8_t *buf = (uint8_t *) strdup(filename);
    MD5_CTX md5Ctx;
    MD5Init(&md5Ctx);
    MD5Update(&md5Ctx, buf, strlen((char *) buf));
    MD5Final(&md5Ctx);

    char md5string[33];
    for (int i = 0; i < 16; ++i)
        sprintf(&md5string[i * 2], "%02x", (unsigned int) md5Ctx.digest[i]);
    rs_cat(s, md5string);
    free(buf);
}

static void handle_videos(struct mg_connection *nc, int ev, void *p) {
    // 发送 videos.html 文件
//    const char *filename = "videos.html";
//    char path_buf[PATH_MAX];
//    strcpy(path_buf, s_http_server_opts.document_root);
//    strcat(path_buf, "/");
//    strcat(path_buf, filename);
//
//    char *buf = read_file(path_buf, &size);
//    if (buf == NULL) {
//        mg_send_head(nc, 500, 0, NULL);
//        return;
//    }
    rapidstring s;
    rs_init(&s);

    rs_cat(&s, "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"/><title>");
    rs_cat(&s, "视频");
    rs_cat(&s,
           "</title><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no\"/><link rel=\"stylesheet\" href=\"index.css\"/><script src=\"index.js\"></script><body><div class=\"app\"><div class=\"mobile-topbar-renderer sticky\"><header class=\"mobile-topbar-header cbox\"><button aria-label=\"YouTube\" role=\"link\" class=\"mobile-topbar-header-endpoint\"><div class=\"c3-icon mobile-topbar-logo ringo-logo\" id=\"home-icon\" flip-for-rtl=\"false\"><svg viewBox=\"0 0 380.9 85.1\" fill=\"\"><path fill=\"#FF0000\" d=\"M118.9 13.5c-1.4-5.2-5.5-9.3-10.7-10.7C98.7.3 60.7.3 60.7.3s-38 0-47.5 2.5C8 4.2 3.9 8.3 2.5 13.5 0 23 0 42.7 0 42.7s0 19.8 2.5 29.2c1.4 5.2 5.5 9.3 10.7 10.7 9.5 2.5 47.5 2.5 47.5 2.5s38 0 47.5-2.5c5.2-1.4 9.3-5.5 10.7-10.7 2.5-9.5 2.5-29.2 2.5-29.2s0-19.7-2.5-29.2z\"></path><path fill=\"#FFF\" d=\"M48.5 61l31.6-18.2-31.6-18.3z\"></path><path d=\"M147.1 55.5L133.5 6.2h11.9l4.8 22.3c1.2 5.5 2.1 10.2 2.7 14.1h.3c.4-2.8 1.3-7.4 2.7-14l5-22.4h11.9L159 55.5v23.7h-11.8l-.1-23.7zm29.2 22.1c-2.4-1.6-4.1-4.1-5.1-7.6-1-3.4-1.5-8-1.5-13.6v-7.7c0-5.7.6-10.3 1.7-13.8 1.2-3.5 3-6 5.4-7.6 2.5-1.6 5.7-2.4 9.7-2.4 3.9 0 7.1.8 9.5 2.4s4.1 4.2 5.2 7.6 1.7 8 1.7 13.8v7.7c0 5.7-.5 10.2-1.6 13.7-1.1 3.4-2.8 6-5.2 7.6-2.4 1.6-5.7 2.4-9.8 2.4-4.3-.1-7.6-.9-10-2.5zm13.5-8.3c.7-1.7 1-4.6 1-8.5V44.2c0-3.8-.3-6.6-1-8.4s-1.8-2.6-3.5-2.6c-1.6 0-2.8.9-3.4 2.6-.7 1.8-1 4.6-1 8.4v16.6c0 3.9.3 6.8 1 8.5.6 1.7 1.8 2.6 3.5 2.6 1.5 0 2.7-.9 3.4-2.6zm51.7-43.4v53.3h-9.4l-1-6.5h-.3c-2.5 4.9-6.4 7.4-11.5 7.4-3.5 0-6.1-1.2-7.8-3.5-1.7-2.3-2.5-5.9-2.5-10.9V25.9h12V65c0 2.4.3 4.1.8 5.1s1.4 1.5 2.6 1.5c1 0 2-.3 3-1 1-.6 1.7-1.4 2.1-2.4V25.9h12z\"></path><path d=\"M274.1 15.9h-11.9v63.3h-11.7V16h-11.9V6.4h35.5v9.5z\"></path><path d=\"M303 25.9v53.3h-9.4l-1-6.5h-.3c-2.5 4.9-6.4 7.4-11.5 7.4-3.5 0-6.1-1.2-7.8-3.5-1.7-2.3-2.5-5.9-2.5-10.9V25.9h12V65c0 2.4.3 4.1.8 5.1s1.4 1.5 2.6 1.5c1 0 2-.3 3-1 1-.6 1.7-1.4 2.1-2.4V25.9h12zm39.7 8.5c-.7-3.4-1.9-5.8-3.5-7.3s-3.9-2.3-6.7-2.3c-2.2 0-4.3.6-6.2 1.9-1.9 1.2-3.4 2.9-4.4 4.9h-.1V3.5h-11.6v75.7h9.9l1.2-5h.3c.9 1.8 2.3 3.2 4.2 4.3 1.9 1 3.9 1.6 6.2 1.6 4.1 0 7-1.9 8.9-5.6 1.9-3.7 2.9-9.6 2.9-17.5v-8.4c0-6.2-.4-10.8-1.1-14.2zm-11 21.7c0 3.9-.2 6.9-.5 9.1-.3 2.2-.9 3.8-1.6 4.7-.8.9-1.8 1.4-3 1.4-1 0-1.9-.2-2.7-.7-.8-.5-1.5-1.2-2-2.1V38.3c.4-1.4 1.1-2.6 2.1-3.6 1-.9 2.1-1.4 3.2-1.4 1.2 0 2.2.5 2.8 1.4.7 1 1.1 2.6 1.4 4.8.3 2.3.4 5.5.4 9.6l-.1 7zm29.1.4v2.7c0 3.4.1 6 .3 7.7.2 1.7.6 3 1.3 3.7.6.8 1.6 1.2 3 1.2 1.8 0 3-.7 3.7-2.1.7-1.4 1-3.7 1.1-7l10.3.6c.1.5.1 1.1.1 1.9 0 4.9-1.3 8.6-4 11s-6.5 3.6-11.4 3.6c-5.9 0-10-1.9-12.4-5.6-2.4-3.7-3.6-9.4-3.6-17.2v-9.3c0-8 1.2-13.8 3.7-17.5s6.7-5.5 12.6-5.5c4.1 0 7.3.8 9.5 2.3s3.7 3.9 4.6 7c.9 3.2 1.3 7.6 1.3 13.2v9.1h-20.1v.2zm1.5-22.4c-.6.8-1 2-1.2 3.7s-.3 4.3-.3 7.8v3.8h8.8v-3.8c0-3.4-.1-6-.3-7.8-.2-1.8-.7-3-1.3-3.7-.6-.7-1.6-1.1-2.8-1.1-1.3 0-2.3.4-2.9 1.1z\"></path></svg></div></button><div class=\"mobile-topbar-header-content cbox\"><button class=\"icon-button topbar-menu-button-avatar-button\" aria-label=\"在 YouTube 中搜索\" aria-haspopup=\"false\"><div class=\"c3-icon\" flip-for-rtl=\"false\"><svg viewBox=\"0 0 24 24\" fill=\"\"><path d=\"M15.5 14h-.79l-.28-.27A6.471 6.471 0 0016 9.5 6.5 6.5 0 109.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z\"></path></svg></div></button></div></header></div><div class=\"pivot-bar-renderer\"><div class=\"pivot-bar-item-renderer\"><div role=\"tab\" aria-selected=\"true\" class=\"pivot-bar-item-tab pivot-w2w\"><div class=\"c3-icon\" flip-for-rtl=\"false\"><svg viewBox=\"0 0 24 24\" fill=\"\"><path d=\"M10 20v-6h4v6h5v-8h3L12 3 2 12h3v8z\"></path></svg></div><div class=\"pivot-bar-item-title\">首页</div></div></div><div class=\"pivot-bar-item-renderer\"><div role=\"tab\" aria-selected=\"false\" class=\"pivot-bar-item-tab pivot-trending\"><div class=\"c3-icon\" flip-for-rtl=\"false\"><svg viewBox=\"0 0 24 24\" fill=\"\"><path d=\"M14.72 17.64c-.32.28-.83.56-1.23.69-1.14.38-2.27-.07-3.05-.71-.11-.09-.07-.26.06-.31 1.19-.38 1.89-1.3 2.09-2.22.2-.88-.16-1.64-.31-2.51-.12-.72-.11-1.34.12-2 .04-.11.2-.13.25-.02.71 1.59 2.72 2.29 3.07 4.04.03.16.05.32.05.48.03.94-.37 1.95-1.05 2.56m2.83-8.02c-.75-.7-1.63-1.2-2.36-1.93-1.49-1.51-2-3.64-1.34-5.66.11-.33-.2-.63-.51-.49-.71.31-1.39.76-1.98 1.24C8.38 5.2 7.27 9.26 8.65 12.92c.03.13.08.26.08.39 0 .26-.16.5-.39.6-.26.12-.54.04-.74-.15-.06-.06-.12-.12-.17-.19-.96-1.26-1.32-2.95-1.05-4.52.07-.4-.43-.62-.67-.31-1.21 1.57-1.81 3.67-1.69 5.65.04.59.13 1.18.29 1.75.2.71.49 1.4.88 2.03 1.21 2.01 3.34 3.46 5.63 3.75 2.43.31 5.06-.14 6.94-1.87 2.09-1.93 2.85-5 1.73-7.68-.04-.11-.09-.21-.14-.32-.25-.52-.55-1.01-.91-1.45-.27-.36-.57-.68-.89-.98z\"></path></svg></div><div class=\"pivot-bar-item-title\">时下流行</div></div></div><div class=\"pivot-bar-item-renderer\"><div role=\"tab\" aria-selected=\"false\" class=\"pivot-bar-item-tab pivot-subs\"><div class=\"c3-icon\" flip-for-rtl=\"false\"><svg viewBox=\"0 0 24 24\" fill=\"\"><path d=\"M20 8H4V6h16v2zm-2-6H6v2h12V2zm4 10v8c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2v-8c0-1.1.9-2 2-2h16c1.1 0 2 .9 2 2zm-6 4l-6-3.27v6.53L16 16z\"></path></svg></div><div class=\"pivot-bar-item-title\">订阅内容</div></div></div><div class=\"pivot-bar-item-renderer\"><div role=\"tab\" aria-selected=\"false\" class=\"pivot-bar-item-tab pivot-library\"><div class=\"c3-icon\" flip-for-rtl=\"false\"><svg viewBox=\"0 0 24 24\" fill=\"\"><path d=\"M4 6H2v14c0 1.1.9 2 2 2h14v-2H4V6zm16-4H8c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm-8 12.5v-9l6 4.5-6 4.5z\"></path></svg></div><div class=\"pivot-bar-item-title\">媒体库</div></div></div></div><div class=\"page-container\">");

    strlist_t files = STRLIST_INITIALIZER;

    int ret = list_directory(video_directory, &files);
    if (ret == -1) {
        rs_free(&s);
        strlist_done(&files);
        mg_send_head(nc, 500, 0, NULL);
        nc->flags |= MG_F_SEND_AND_CLOSE;
        return;
    }


    // 排序文件
    strlist_sort(&files);

    STRLIST_FOREACH(&files, filename, {
        rs_cat(&s,
               "<div class=\"item-section-renderer\"><div class=\"item\"><div class=\"large-media-item\"><a target=\"_blank\" href=\"/watch?v=");
        rs_cat(&s, filename);
        rs_cat(&s,
               "\"><div class=\"video-thumbnail-container-large center\"><div class=\"cover video-thumbnail-img video-thumbnail-bg\"></div><img alt=\"\" class=\"cover video-thumbnail-img\" src=\"/images/");
        generateMd5(&s, filename);
        rs_cat(&s,
               ".jpg\"/></div></a><div class=\"details\"><div class=\"large-media-item-info cbox\"><div class=\"large-media-item-metadata\"><a target=\"_blank\" href=\"/watch?v=");
        rs_cat(&s, filename);
        rs_cat(&s, "\"><h3><span aria-label=\"\" role=\"text\">");
        rs_cat(&s, strrchr(filename, '/') + 1);
        rs_cat(&s,
               "</span></h3></a></div><div class=\"menu-renderer large-media-item-menu\"><div class=\"menu\"><button class=\"icon-button\" aria-label=\"操作菜单\" aria-haspopup=\"true\"><div class=\"c3-icon\" flip-for-rtl=\"false\"><svg viewBox=\"0 0 24 24\" fill=\"\"><path d=\"M12 8c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2zm0 2c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2zm0 6c-1.1 0-2 .9-2 2s.9 2 2 2 2-.9 2-2-.9-2-2-2z\"></path></svg></div></button></div></div></div></div></div></div></div>");
    }

    );

    strlist_done(&files);

    rs_cat(&s,
           "</div><div hidden class=\"spinner\"></div></div><div id=\"menu\" class=\"menu-container\" style=\"display:none\"><div role=\"dialog\" aria-modal=\"true\" class=\"menu-content\" tabindex=\"-1\"><div class=\"menu-service-item-renderer\"><div class=\"menu-item\"><button class=\"menu-item-button\">删除</button></div></div><div class=\"menu-item\"><button class=\"menu-item-button\"><div class=\"menu-cancel-button\">取消</div></button></div></div><div class=\"c3-overlay\"><button class=\"hidden-button\" aria-label=\"close\"></button></div></div>");

    int size = rs_len(&s);
    char *buf = rs_data(&s);
    mg_send_head(nc,
                 200, size, "Content-Type: text/html");
    mg_send(nc, buf, size
    );
    rs_free(&s);
    nc->flags |= MG_F_SEND_AND_CLOSE;
}

static int has_prefix(const struct mg_str *uri, const struct mg_str *prefix) {
    return uri->len > prefix->len && memcmp(uri->p, prefix->p, prefix->len) == 0;
}

static int is_equal(const struct mg_str *s1, const struct mg_str *s2) {
    return s1->len == s2->len && memcmp(s1->p, s2->p, s2->len) == 0;
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


    mg_set_protocol_http_websocket(nc);
    for (;;) {
        mg_mgr_poll(&mgr, 500);
    }
    mg_mgr_free(&mgr);
}

JNIEXPORT void JNICALL
Java_euphoria_psycho_browser_app_NativeHelper_startServer(JNIEnv *env, jclass clazz, jstring host_,
                                                          jstring port_, jstring rootDirectory_,
                                                          jstring videoDirectory_) {
    const char *host = (*env)->GetStringUTFChars(env, host_, 0);
    const char *port = (*env)->GetStringUTFChars(env, port_, 0);
    const char *rootDirectory = (*env)->GetStringUTFChars(env, rootDirectory_, 0);
    // 视频目录
    const char *videoDirectory = (*env)->GetStringUTFChars(env, videoDirectory_, 0);

    strcpy(video_directory, videoDirectory);

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

    return;
}

JNIEXPORT jstring

JNICALL
Java_euphoria_psycho_browser_app_NativeHelper_youdao(JNIEnv *env, jclass clazz, jstring query_,
                                                     jboolean is_english_to_chinese,
                                                     jboolean is_translate) {

    const char *query = (*env)->GetStringUTFChars(env, query_, 0);
    size_t buf_body_len = 1024 << 2;
    char buf_body[buf_body_len];
    int ret = youdao(query, is_english_to_chinese, YOUDAO_API_KEY, YOUDAO_API_SECRET,
                     is_translate, buf_body, buf_body_len);
    jstring result = (*env)->NewStringUTF(env, buf_body);

    (*env)->ReleaseStringUTFChars(env, query_, query);

    return result;

}

JNIEXPORT jstring

JNICALL
Java_euphoria_psycho_browser_app_NativeHelper_google(JNIEnv *env, jclass clazz, jstring query_,
                                                     jboolean is_english_to_chinese) {

    const char *query = (*env)->GetStringUTFChars(env, query_, 0);
    size_t buf_body_len = 1024 << 2;
    char buf_body[buf_body_len];
    int ret = google(query, is_english_to_chinese, buf_body, buf_body_len);
    jstring result = (*env)->NewStringUTF(env, buf_body);
    (*env)->ReleaseStringUTFChars(env, query_, query);
    return result;
}

JNIEXPORT jboolean

JNICALL
Java_euphoria_psycho_browser_app_NativeHelper_deleteFileSystem(JNIEnv *env, jclass clazz,
                                                               jstring path_) {
    const char *path = (*env)->GetStringUTFChars(env, path_, 0);
    int ret;
    if (is_dir(path)) {
        char nameBuffer[PATH_MAX];
        struct stat statBuffer;
        ret = delete_directory(path, nameBuffer, &statBuffer);
        if (!ret) {
            ret = rmdir(path);
        }
    } else {
        ret = unlink(path);
    }
    (*env)->ReleaseStringUTFChars(env, path_, path);
    return ret == 0 ? true : false;
}


#define CLASS euphoria_psycho_browser_app_NativeHelper

JAVA_STATIC_METHOD(CLASS, dirSize, jlong, jstring path_) {
    const char *path = (*env)->GetStringUTFChars(env, path_, 0);
    int dirfd = open(path, O_DIRECTORY, O_RDONLY);
    (*env)->ReleaseStringUTFChars(env, path_, path);
    if (dirfd < 0) {
        return -1;
    } else {
        int64_t res = calculate_dir_size(dirfd);
        close(dirfd);
        return res;
    }
}

JAVA_STATIC_METHOD(CLASS, copyFile, jboolean, jstring source_, jstring target_) {
    const char *source = (*env)->GetStringUTFChars(env, source_, 0);
    const char *target = (*env)->GetStringUTFChars(env, target_, 0);

    int ret = copy_file(target, source, 0660);

    (*env)->ReleaseStringUTFChars(env, source_, source);
    (*env)->ReleaseStringUTFChars(env, target_, target);
    return ret == 0 ? true : false;
}

struct files {
    char **files_name;
    size_t capacity;
    size_t index;
};

int list_directory_files(char *path, struct files *list);

int list_directory_files(char *path, struct files *list) {
    DIR *dir = opendir(path);
    if (dir) {
        struct dirent *dp;
        struct stat s;
        while ((dp = readdir(dir))) {
            if (strcmp(dp->d_name, ".") == 0
                || strcmp(dp->d_name, "..") == 0)
                continue;
            size_t len = strlen(path) + 2 + strlen(dp->d_name);
            char *buf = malloc(len);
            memset(buf, 0, len);
            strcat(buf, path);
            strcat(buf, "/");
            strcat(buf, dp->d_name);
            if (stat(buf, &s) == -1) {
                free(buf);
                continue;
            }
            if (S_ISDIR(s.st_mode)) {
                list_directory(buf, list);
            } else {
                if (list->index + 1 >= list->capacity) {
                    list->capacity = list->capacity * 2;
                    list->files_name = realloc(list->files_name, sizeof(char *) * list->capacity);
                }
                //printf("%d %s\n", list->index, buf);
                *(list->files_name + list->index++) = buf;
            }
        }
        closedir(dir);
    }
    free(path);
    return 0;
}

JAVA_STATIC_METHOD(CLASS, createZipFromDirectory, void, jstring dir_, jstring filename_) {
    const char *dir = (*env)->GetStringUTFChars(env, dir_, 0);
    const char *filename = (*env)->GetStringUTFChars(env, filename_, 0);
    struct zip_t *zip = zip_open(filename, ZIP_DEFAULT_COMPRESSION_LEVEL, 'w');
    size_t cap = 128;
    struct files list = {
            .files_name = malloc(sizeof(char *) * cap),
            .capacity = cap,
            .index = 0,
    };
    char *p = strdup(dir);
    list_directory_files(p, &list);
    for (size_t i = 0; i < list.index; i++) {
        zip_entry_open(zip, *(list.files_name + i) + strlen(dir) + 1);
        zip_entry_fwrite(zip, *(list.files_name + i));
        zip_entry_close(zip);
        free(*(list.files_name + i));
    }
    free(list.files_name);
    zip_close(zip);
    (*env)->ReleaseStringUTFChars(env, dir_, dir);
    (*env)->ReleaseStringUTFChars(env, filename_, filename);
}

JAVA_STATIC_METHOD(CLASS, extractToDirectory, void, jstring filename_, jstring directory_) {
    const char *filename = (*env)->GetStringUTFChars(env, filename_, 0);
    const char *directory = (*env)->GetStringUTFChars(env, directory_, 0);
    zip_extract(filename, directory, NULL, NULL);
    (*env)->ReleaseStringUTFChars(env, filename_, filename);
    (*env)->ReleaseStringUTFChars(env, directory_, directory);
}

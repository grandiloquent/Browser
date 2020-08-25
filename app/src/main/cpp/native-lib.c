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
    rs_cat(&s,
           "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"UTF-8\"/><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no\"/><title>");
    rs_cat(&s, "视频");
    rs_cat(&s,
           "</title><link rel=\"stylesheet\" href=\"video.css\"/><script src=\"share.js\"></script><body><div id=\"app\"><div class=\"page-container\"><div class=\"player-size\"></div><div class=\"single-column\"><div class=\"item-section-renderer\"><div class=\"autonav-bar cbox\"><h3 class=\"autonav-title\">接下来播放</h3><div class=\"autonav-toggle-wrapper cbox\"><div class=\"autonav-toggle-description\" aria-hidden=\"true\">自动播放</div><c3-material-toggle-button class=\"ytm-autonav-toggle\"> <button class=\"material-toggle-button\" aria-label=\"自动播放\" aria-pressed=\"true\"><div class=\"material-toggle-button-track\"></div><div class=\"material-toggle-button-circle\"></div></button> </c3-material-toggle-button></div></div></div>");

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
               "<div class=\"item\"><div class=\"compact-media-item\"><a class=\"compact-media-item-image\" aria-hidden=\"true\" href=\"/watch?v=");
        rs_cat(&s, filename);
        rs_cat(&s,
               "\"><div class=\"video-thumbnail-container-compact center\"><div class=\"cover video-thumbnail-img video-thumbnail-bg\"></div><img class=\"cover video-thumbnail-img\" alt=\"\" src=\"/images/");

        MD5_CTX md5Ctx;
        MD5Init(&md5Ctx);
        MD5Update(&md5Ctx, filename, strlen(filename));
        MD5Final( &md5Ctx);

        char md5string[33];
        for (int i = 0; i < 16; ++i)
            sprintf(&md5string[i * 2], "%02x", (unsigned int) md5Ctx.digest[i]);
        rs_cat(&s, md5string);

        rs_cat(&s,
               ".jpg\"/><div class=\"video-thumbnail-overlay-bottom-group\"><div class=\"thumbnail-overlay-time-status-renderer\" data-style=\"DEFAULT\"><span role=\"text\"></span></div></div></div></a><div class=\"compact-media-item-metadata\" data-has-badges=\"false\"><a class=\"compact-media-item-metadata-content\" href=\"/watch?v=");
        rs_cat(&s, filename);
        rs_cat(&s, "\"><h4 class=\"compact-media-item-headline\"><span role=\"text\">");
        rs_cat(&s, strrchr(filename, '/') + 1);
        rs_cat(&s, "</span></h4></a></div></div></div>");
    }

    );

    strlist_done(&files);

    rs_cat(&s,
           "</div></div></div><div class=\"player-container\"><div id=\"player\" class=\"player-api player-size\"><div class=\"html5-video-player\"><video class=\"html5-main-video video-stream\" controlslist=\"nodownload\"></video></div></div><div class=\"player-control-container\"><div id=\"player-control-overlay\" class=\"animation-enabled fadein\"><div class=\"player-controls-content\"><div class=\"player-controls-top\"><button class=\"icon-button\"><div class=\"icon\"><svg viewBox=\"0 0 20 20\" preserveAspectRatio=\"xMidYMid meet\" fill=\"\"><path d=\"M15.95 10.78c.03-.25.05-.51.05-.78s-.02-.53-.06-.78l1.69-1.32c.15-.12.19-.34.1-.51l-1.6-2.77c-.1-.18-.31-.24-.49-.18l-1.99.8c-.42-.32-.86-.58-1.35-.78L12 2.34c-.03-.2-.2-.34-.4-.34H8.4c-.2 0-.36.14-.39.34l-.3 2.12c-.49.2-.94.47-1.35.78l-1.99-.8c-.18-.07-.39 0-.49.18l-1.6 2.77c-.1.18-.06.39.1.51l1.69 1.32c-.04.25-.07.52-.07.78s.02.53.06.78L2.37 12.1c-.15.12-.19.34-.1.51l1.6 2.77c.1.18.31.24.49.18l1.99-.8c.42.32.86.58 1.35.78l.3 2.12c.04.2.2.34.4.34h3.2c.2 0 .37-.14.39-.34l.3-2.12c.49-.2.94-.47 1.35-.78l1.99.8c.18.07.39 0 .49-.18l1.6-2.77c.1-.18.06-.39-.1-.51l-1.67-1.32zM10 13c-1.65 0-3-1.35-3-3s1.35-3 3-3 3 1.35 3 3-1.35 3-3 3z\">\r\n                                </path></svg></div></button></div><div class=\"player-controls-middle center\"><button class=\"icon-button icon-disable\"><div class=\"icon\"><svg viewBox=\"0 0 36 36\" preserveAspectRatio=\"xMidYMid meet\" fill=\"none\"><path d=\"M9,9 L12,9 L12,27 L9,27 L9,9 Z M14.25,18 L27,27 L27,9 L14.25,18 Z\"></path><polygon points=\"0 0 36 0 36 36 0 36\"></polygon></svg></div></button> <button class=\"icon-button player-control-play-pause-icon\"><div class=\"icon\"><svg viewBox=\"0 0 56 56\" preserveAspectRatio=\"xMidYMid meet\" fill=\"none\"><polygon fill=\"#FFFFFF\" points=\"18.6666667 11.6666667 18.6666667 44.3333333 44.3333333 28\"></polygon><polygon points=\"0 0 56 0 56 56 0 56\"></polygon></svg></div></button> <button class=\"icon-button button-next\"><div class=\"icon\"><svg viewBox=\"0 0 36 36\" preserveAspectRatio=\"xMidYMid meet\" fill=\"none\"><path d=\"M9,27 L21.75,18 L9,9 L9,27 Z M24,9 L24,27 L27,27 L27,9 L24,9 Z\"></path><polygon points=\"0 0 36 0 36 36 0 36\"></polygon></svg></div></button></div><div class=\"player-controls-bottom\"><div class=\"time-display\"><div class=\"time-display-content cbox\"><span class=\"time-first\">0:00</span> <span class=\"time-delimiter\">/</span> <span class=\"time-second\"></span></div></div><div class=\"progress-bar\"><div class=\"progress-bar-line\"><div class=\"progress-bar-background\"></div><div class=\"progress-bar-loaded\"></div><div class=\"progress-bar-played\"></div></div><div class=\"progress-bar-playhead-wrapper\"><div class=\"progress-bar-playhead\"><div class=\"progress-bar-playhead-dot\"></div></div></div></div><button class=\"icon-button\"><div class=\"icon\"><svg viewBox=\"0 0 24 24\" preserveAspectRatio=\"xMidYMid meet\" fill=\"\"><path d=\"M7 14H5v5h5v-2H7v-3zm-2-4h2V7h3V5H5v5zm12 7h-3v2h5v-5h-2v3zM14 5v2h3v3h2V5h-5z\">\r\n                                </path></svg></div></button></div></div></div></div></div><script src=\"video.js\"></script>");

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

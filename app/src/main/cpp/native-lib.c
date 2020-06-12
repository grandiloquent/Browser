#include "jni.h"
#include "android/log.h"
#include "mongoose.h"
#include "dynarray.h"
#include "cJSON.h"
#include "helper.h"


static struct mg_serve_http_opts s_http_server_opts;

///////////////////////////
bool ends_with(const char *s1, const char *s2) {
    size_t s1_length = strlen(s1);
    size_t s2_length = strlen(s2);
    if (s2_length > s1_length) {
        return false;
    }
    const char *start = s1 + (s1_length - s2_length);
    return strncmp(start, s2, s2_length) == 0;
}


static int has_prefix(const struct mg_str *uri, const struct mg_str *prefix);

static int is_equal(const struct mg_str *s1, const struct mg_str *s2);

static void ev_handler(struct mg_connection *nc, int ev, void *ev_data);

static void handle_api_videos(struct mg_connection *nc, const struct http_message *hm);

static void handle_video(struct mg_connection *nc, const struct http_message *hm);

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
        if (!strcmp(de->d_name, ".") || !strcmp(de->d_name, "..")
                )
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

static void handle_watch(struct mg_connection *nc, struct http_message *hm) {
    char filename[PATH_MAX];
    mg_get_http_var(&hm->query_string, "v", filename, PATH_MAX);
    static const struct mg_str video = MG_MK_STR("video/mp4");
    mg_http_serve_file(nc, hm, filename, video, mg_mk_str(""));
}

static void ev_handler(struct mg_connection *nc, int ev, void *ev_data) {
    static const struct mg_str api_index = MG_MK_STR("/");
    static const struct mg_str api_videos = MG_MK_STR("/api/videos");
    static const struct mg_str watch = MG_MK_STR("/watch");
    static const struct mg_str video = MG_MK_STR("/video");
    struct http_message *hm = (struct http_message *) ev_data;
    if (ev == MG_EV_HTTP_REQUEST) {
        if (is_equal(&hm->uri, &api_videos)) {
            LOGE("%s\n", "/api/videos");
            handle_api_videos(nc, hm);
        }
        if (is_equal(&hm->uri, &watch)) {
            handle_watch(nc, hm);
        } else if (is_equal(&hm->uri, &video)) {

            handle_video(nc, hm);
        } else {
            LOGE("ev_handler: %s\n", s_http_server_opts.document_root);
            mg_serve_http(nc, hm, s_http_server_opts);
        }
    }
}

static void handle_api_videos(struct mg_connection *nc, const struct http_message *hm) {
    char path_buf[PATH_MAX];
    dirname(path_buf, s_http_server_opts.document_root);
    strcat(path_buf, "/Videos");
    strlist_t files = STRLIST_INITIALIZER;
    LOGE("list_directory(): %s\n", path_buf);

    int ret = list_directory(path_buf, &files);
    if (ret == -1) {
        LOGE("list_directory(): %s\n", path_buf);
        goto err;
    }
    cJSON *items = cJSON_CreateArray();
    if (items == NULL) {
        goto err;
    }
    cJSON *item;

    STRLIST_FOREACH(&files, filename, {
        item = cJSON_CreateString(filename);
        if (item == NULL) {
            goto err;
        }
        cJSON_AddItemToArray(items, item);
    });
    char *buf = cJSON_Print(items);
    mg_send_head(nc, 200, strlen(buf), "Content-Type: application/json");
    mg_send(nc, buf, strlen(buf));
    cJSON_Delete(items);
    strlist_done(&files);
    free(buf);
    return;
    err:
    cJSON_Delete(items);
    strlist_done(&files);
    mg_send_head(nc, 500, 0, NULL);
    return;

}

static void handle_video(struct mg_connection *nc, const struct http_message *hm) {
    const char *filename = "videos.html";
    char path_buf[PATH_MAX];
    strcpy(path_buf, s_http_server_opts.document_root);
    strcat(path_buf, "/");
    strcat(path_buf, filename);
    int size = 0;
    char *buf = read_file(path_buf, &size);
    if (buf == NULL) {
        mg_send_head(nc, 500, 0, NULL);
        return;
    }
    mg_send_head(nc, 200, size, "Content-Type: text/html");
    mg_send(nc, buf, size);
    free(buf);
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
                                                          jstring port_, jstring rootDirectory_) {
    const char *host = (*env)->GetStringUTFChars(env, host_, 0);
    const char *port = (*env)->GetStringUTFChars(env, port_, 0);
    const char *rootDirectory = (*env)->GetStringUTFChars(env, rootDirectory_, 0);

    char *dir = malloc(strlen(rootDirectory) + 1);

    strcpy(dir, rootDirectory);
    s_http_server_opts.document_root = dir;

    char *url = malloc(strlen(host) + strlen(port) + 2);
    memset(url, 0, strlen(host) + strlen(port) + 2);
    sprintf(url, "%s:%s", host, port);
    LOGE("%s", url);

    pthread_t t;
    pthread_create(&t, NULL, (void *(*)(void *)) start_server, url);

    (*env)->ReleaseStringUTFChars(env, host_, host);
    (*env)->ReleaseStringUTFChars(env, port_, port);
    (*env)->ReleaseStringUTFChars(env, rootDirectory_, rootDirectory);

    return 1;
}


#ifndef TRANSLATOR_H__
#define TRANSLATOR_H__

#include <stdlib.h>
#include <string.h>

#include "tmd5/tmd5.h"

#define ENSURE_NOT_BIG()  if (buf_body_read_len >= buf_body_len) {    \
            close(fd);    \
            return 1;   \
        }
#define SOCKET_INIT(X, Y) int ret, fd;    \
    {    \
        struct addrinfo hints, *cur;    \
        memset(&hints, 0x00, sizeof(hints));    \
        hints.ai_family = AF_UNSPEC;    \
        hints.ai_socktype = SOCK_STREAM;    \
        hints.ai_protocol = IPPROTO_TCP;    \
        ret = getaddrinfo(X, Y, &hints, &cur);    \
        if (ret) {    \
            freeaddrinfo(cur);    \
            return 1;    \
        }    \
        fd = socket(cur->ai_family, cur->ai_socktype, cur->ai_protocol);    \
        if (fd < 0) {    \
            freeaddrinfo(cur);    \
            return 1;    \
        }    \
        if (connect(fd, cur->ai_addr, cur->ai_addrlen) != 0) {    \
            freeaddrinfo(cur);    \
            return 1;    \
        }    \
        freeaddrinfo(cur);    \
    }

#define URL_ENCODE(X) char buf_encode[strlen(X) * 3 + 1];    \
    const char *path_str = X;    \
    size_t buf_encode_index = 0;    \
    while (*path_str) {    \
        if (isalnum(*path_str) || *path_str == '-' || *path_str == '_' || *path_str == '.' ||    \
            *path_str == '~') {    \
            buf_encode[buf_encode_index] = *path_str;    \
            buf_encode_index = buf_encode_index + 1;    \
        } else if (*path_str == ' ') {    \
            buf_encode[buf_encode_index] = '+';    \
            buf_encode_index = buf_encode_index + 1;    \
        } else {    \
            buf_encode[buf_encode_index] = '%';    \
            buf_encode_index = buf_encode_index + 1;    \
            buf_encode[buf_encode_index] = HEX_ARRAY[*path_str >> 4 & 15];    \
            buf_encode_index = buf_encode_index + 1;    \
            buf_encode[buf_encode_index] = HEX_ARRAY[*path_str & 15 & 15];    \
            buf_encode_index = buf_encode_index + 1;    \
        }    \
        path_str++;    \
    }    \
    buf_encode[buf_encode_index] = 0

#define  SEND_HEADER() ret = send(fd, buf_header, strlen(buf_header), 0);    \
    if (ret <= 0) {    \
        close(fd);    \
        return 1;    \
    }

static const char HEX_ARRAY[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                                 'A', 'B', 'C', 'D', 'E', 'F'};

static int indexof(const char *s, const char *find) {
    char c, sc;
    size_t len;
    int count = 0;

    if ((c = *find++) != 0) {
        len = strlen(find);
        do {
            do {
                if ((sc = *s++) == 0)
                    return -1;
                count++;
            } while (sc != c);
        } while (strncmp(s, find, len) != 0);
        count--;
    }
    return count;
}

int youdao(const char *word, bool english_to_chinese, const char *api_key, char *api_secret,
           bool translate, char *buf_body, size_t buf_body_len) {
    const char *from = english_to_chinese ? "EN" : "zh-CHS";
    const char *to = english_to_chinese ? "zh-CHS" : "EN";


    SOCKET_INIT("openapi.youdao.com", "80");
    URL_ENCODE(word);

    size_t buf_path_len = strlen(api_key) + (strlen(word) << 1) +
                          strlen(api_secret) + 60;;
    char buf_path[buf_path_len];
    memset(buf_path, 0, buf_path_len);
    int salt = time(NULL);
    snprintf(buf_path, buf_path_len, "%s%s%d%s", api_key, word, salt,
             api_secret);
    char md5_buf[33];
    MD5_CTX md5_ctx;
    MD5Init(&md5_ctx);
    MD5Update(&md5_ctx, buf_path, strlen(buf_path));
    MD5Final(&md5_ctx);
    for (int i = 0, j = 0; i < 16; i++) {
        uint8_t t = md5_ctx.digest[i];
        md5_buf[j++] = HEX_ARRAY[t / 16];
        md5_buf[j++] = HEX_ARRAY[t % 16];
    }
    md5_buf[32] = 0;
    memset(buf_path, 0, buf_path_len);
    snprintf(buf_path, buf_path_len, "/api?q=%s&salt=%d&sign=%s&from=%s&appKey=%s&to=%s",
             buf_encode, salt, md5_buf, from, api_key, to);;
    size_t buf_header_len = strlen(buf_path) + 50;
    char buf_header[buf_header_len];
    buf_header[0] = 0;
    strcat(buf_header, "GET ");
    strcat(buf_header, buf_path);
    strcat(buf_header, " HTTP/1.1\r\n");
    strcat(buf_header, "Host: openapi.youdao.com\r\n");
    strcat(buf_header, "\r\n");;
    ret = send(fd, buf_header, strlen(buf_header), 0);
    if (ret <= 0) {
        close(fd);
        return 1;
    };

    size_t buf_body_read_len = 0;

    memset(buf_body, 0, buf_body_len);
    do {

        ENSURE_NOT_BIG();
        while ((ret = read(fd, buf_body + buf_body_read_len, buf_body_len - buf_body_read_len)) ==
               -1 && errno == EINTR);

        if (ret <= 0) {
            close(fd);
            return 1;
        }
        if (indexof(buf_body, "0\r\n\r\n") != -1) { break; }
        if (indexof(buf_body, "400 Bad Request") != -1) {
            close(fd);
            return 1;
        }
        buf_body_read_len += ret;
    } while (1);


    char *y = strstr(buf_body, "\r\n\r\n");
    if (y == NULL || strlen(y) <= 4) {
        close(fd);
        return 1;
    }
    y = y + 4;
    char *body = strstr(y, "\r\n");
    if (body == NULL) {
        close(fd);
        return 1;
    };

    if (translate) {
        cJSON *json = cJSON_Parse(body);
        memset(buf_body, 0, buf_body_len);
        if (json == NULL) {
            const char *error_ptr = cJSON_GetErrorPtr();
            if (error_ptr != NULL) {
                cJSON_Delete(json);
                close(fd);
                return 1;
            }
        }
        const cJSON *translation = cJSON_GetObjectItem(json, "translation");
        if (translation == NULL) {
            cJSON_Delete(json);
            close(fd);
            return 1;
        }
        const cJSON *t = NULL;

        cJSON_ArrayForEach(t, translation) {
            strcat(buf_body, t->valuestring);
            strcat(buf_body, "\n");
        };
    } else {
        cJSON *json = cJSON_Parse(body);
        memset(buf_body, 0, buf_body_len);
        if (json == NULL) {
            const char *error_ptr = cJSON_GetErrorPtr();
            if (error_ptr != NULL) {
                cJSON_Delete(json);
                close(fd);
                return 1;
            }
        }
        const cJSON *basic = cJSON_GetObjectItem(json, "basic");
        const cJSON *explains = cJSON_GetObjectItem(basic, "explains");
        const cJSON *explain = NULL;
        cJSON_ArrayForEach(explain, explains) {
            strcat(buf_body, explain->valuestring);
            strcat(buf_body, "\n");
        };
        const cJSON *web = cJSON_GetObjectItem(json, "web");
        const cJSON *w = NULL;
        cJSON_ArrayForEach(w, web) {
            strcat(buf_body, cJSON_GetObjectItem(w, "key")->valuestring);
            const cJSON *values = cJSON_GetObjectItem(w, "value");
            const cJSON *value = NULL;
            strcat(buf_body, " ");
            cJSON_ArrayForEach(value, values) {
                strcat(buf_body, value->valuestring);
                strcat(buf_body, ",");
            }
            buf_body[strlen(buf_body) - 1] = '\n';
        }
        cJSON_Delete(json);
    }

    close(fd);
    return 0;
}

int google(const char *word, bool english_to_chinese, char *buf_body, size_t buf_body_len) {
    const char *to = english_to_chinese ? "zh" : "en";

    SOCKET_INIT("translate.google.cn", "80");
    URL_ENCODE(word);


    const char *url_format = "/translate_a/single?client=gtx&sl=auto&tl=%s&dt=t&dt=bd&ie=UTF-8&oe=UTF-8&dj=1&source=icon&q=%s";

    size_t buf_path_len = strlen(buf_encode) + strlen(url_format) + 10;
    char buf_path[buf_path_len];
    memset(buf_path, 0, buf_path_len);

    snprintf(buf_path, buf_path_len,
             url_format, to, buf_encode);

    size_t buf_header_len = strlen(buf_path) + 100;
    char buf_header[buf_header_len];
    buf_header[0] = 0;
    strcat(buf_header, "GET ");
    strcat(buf_header, buf_path);
    strcat(buf_header, " HTTP/1.1\r\n");
    strcat(buf_header, "Accept: application/json, text/javascript, */*; q=0.01\r\n");
    strcat(buf_header, "Host: translate.google.cn\r\n");
    strcat(buf_header,
           "User-Agent: Mozilla/4.0\r\n");
    strcat(buf_header, "\r\n");


    SEND_HEADER();

    size_t buf_body_read_len = 0, header_end = 0;
    memset(buf_body, 0, buf_body_len);
    do {
        ENSURE_NOT_BIG();


        while ((ret = read(fd, buf_body + buf_body_read_len, buf_body_len - buf_body_read_len)) ==
               -1 && errno == EINTR);

        if (ret <= 0) {
            close(fd);
            return 1;
        }


        if (indexof(buf_body, "0\r\n\r\n") != -1) {
            break;
        }
//        LOGE("%d %d", ret, buf_body_read_len);
//
        //if (indexof(buf_body, "0\r\n\r\n") != -1) { break; }
        if (indexof(buf_body, "200 OK") == -1) {
            close(fd);
            return 1;
        }

        buf_body_read_len += ret;


    } while (1);


    char *y = strstr(buf_body, "\r\n\r\n");
    if (y == NULL || strlen(y) <= 4) {
        close(fd);
        return 1;
    }
    y = y + 4;
    char *body = strstr(y, "\r\n");
    if (body == NULL) {
        close(fd);
        return 1;
    };

    cJSON *json = cJSON_Parse(body);
    if (json == NULL) {
        close(fd);
        return 1;
    }


    cJSON *sentences = cJSON_GetObjectItem(json, "sentences");
    if (sentences == NULL) {
        cJSON_Delete(json);
        close(fd);
        return 1;
    }
    const cJSON *t = NULL;
    memset(buf_body, 0, buf_body_len);

    cJSON_ArrayForEach(t, sentences) {
        strcat(buf_body, cJSON_GetObjectItem(t, "trans")->valuestring);
    }
    close(fd);
    return 0;
}

#endif

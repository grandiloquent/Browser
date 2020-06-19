#ifndef HELPER_H__
#define  HELPER_H__

#include <stdlib.h>

#define LOG_TAG "TAG/Native"
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#define ARRAY_COUNT(array) (sizeof((array)) / sizeof((array)[0]))

#define JAVA_METHOD(object, name, type, ...) \
  JNIEXPORT type JNICALL Java_ ## object ## _ ## name ( \
    JNIEnv *env, jobject this, ## __VA_ARGS__ \
  )


long parse_range(const char *s) {

    if (s == NULL)return 0;
    for (; *s && *s != '='; ++s);
    if (*s)s++;
    char *end;
    return strtol(s, &end, 10);
}

char *read_file(const char *filename, int *size) {
    FILE *f = fopen(filename, "rb");
    char *buf;
    int i;
    if (!f) {
        perror("Error opening input file");
        return NULL;
    }
    fseek(f, 0, SEEK_END);
    *size = ftell(f);
    rewind(f);
    if ((*size > 0x100000) || (*size < 0)) {
        if (*size < 0)
            perror("ftell failed");
        else
            fprintf(stderr, "File seems unreasonably large\n");
        fclose(f);
        return NULL;
    }
    buf = (char *) malloc(*size);
    if (!buf) {
        fprintf(stderr, "Unable to allocate buffer.\n");
        fclose(f);
        return NULL;
    }
    printf("Reading %d bytes from %s...\n", *size, filename);
    i = fread(buf, 1, *size, f);
    fclose(f);
    if (i != *size) {
        perror("Error reading file");
        free(buf);
        return NULL;
    }
    return buf;
}

void base64enc(char *out, const char *in) {
    const char code[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
    int i = 0, x = 0, l = 0;

    for (; *in; in++) {
        x = x << 8 | *in;
        for (l += 8; l >= 6; l -= 6) {
            out[i++] = code[(x >> (l - 6)) & 0x3f];
        }
    }
    if (l > 0) {
        x <<= 6 - l;
        out[i++] = code[x & 0x3f];
    }
    for (; i % 4;) {
        out[i++] = '=';
    }
    out[i] = '\0';
}

int delete_directory(const char *path, char *nameBuffer, struct stat *statBuffer) {
    DIR *dir;
    struct dirent *de;
    int ret;
    dir = opendir(path);
    if (dir == NULL) {
        LOGE("opendir() error on '%s' '%s'\n", path, strerror(errno));
        return 1;
    }

    char *filenameOffset;
    strcpy(nameBuffer, path);
    strcat(nameBuffer, "/");
    filenameOffset = nameBuffer + strlen(nameBuffer);
    for (;;) {
        de = readdir(dir);
        if (de == NULL) {
            break;
        }
        if (0 == strcmp(de->d_name, ".")
            || 0 == strcmp(de->d_name, "..")
                ) {
            continue;
        }
        strcpy(filenameOffset, de->d_name);
        ret = lstat(nameBuffer, statBuffer);
        if (ret != 0) {
            LOGE("lstat() error on '%s' '%s'\n", nameBuffer, strerror(errno));
            return 1;
        }
        if (S_ISDIR(statBuffer->st_mode)) {
            char *newpath = strdup(nameBuffer);
            delete_directory(newpath, nameBuffer, statBuffer);
            ret = rmdir(newpath);
            if (ret != 0) {
                LOGE("rmdir() error on '%s' '%s'\n", newpath, strerror(errno));
                free(newpath);
                return 1;
            }
#if 0
            LOGE("rmdir() on '%s'\n", newpath);
#endif
            free(newpath);
        } else {
            ret = unlink(nameBuffer);
            if (ret != 0) {
                LOGE("unlink() error on '%s' '%s'\n", nameBuffer, strerror(errno));
                return 1;
            }
        }
    }
    closedir(dir);
    return rmdir(path);
}

bool is_dir(const char *pathname) {
    struct stat info;
    if (stat(pathname, &info) == -1) {
        return false;
    }
    return S_ISDIR(info.st_mode);
}

#endif


#include <stdlib.h>
#include <stdio.h>
#include <errno.h>
#include <string.h>
#include <dirent.h>
#include <fcntl.h>
#include <stdbool.h>
#include "helper.h"


static int parse_size(const char *size_str, size_t *size) {
    static const size_t MAX_SIZE_T = ~(size_t) 0;
    size_t mult;
    unsigned long long int value;
    const char *end;
    errno = 0;
    value = strtoull(size_str, (char **) &end, 10);
    if (errno != 0 || end == size_str || value > MAX_SIZE_T)
        return -1;
    if (*end == '\0') {
        *size = value;
        return 0;
    }
    if (!strcmp(end, "c"))
        mult = 1;
    else if (!strcmp(end, "w"))
        mult = 2;
    else if (!strcmp(end, "b"))
        mult = 512;
    else if (!strcmp(end, "kB"))
        mult = 1000;
    else if (!strcmp(end, "K"))
        mult = 1024;
    else if (!strcmp(end, "MB"))
        mult = (size_t) 1000 * 1000;
    else if (!strcmp(end, "M"))
        mult = (size_t) 1024 * 1024;
    else if (!strcmp(end, "GB"))
        mult = (size_t) 1000 * 1000 * 1000;
    else if (!strcmp(end, "G"))
        mult = (size_t) 1024 * 1024 * 1024;
    else
        return -1;
    if (value > MAX_SIZE_T / mult)
        return -1;
    *size = value * mult;
    return 0;
}

int64_t stat_size(struct stat *s) {
    int64_t blksize = s->st_blksize;
    // count actual blocks used instead of nominal file size
    int64_t size = s->st_blocks * 512;
    if (blksize) {
        /* round up to filesystem block size */
        size = (size + blksize - 1) & (~(blksize - 1));
    }
    return size;
}

int64_t calculate_dir_size(int dfd) {
    int64_t size = 0;
    struct stat s;
    DIR *d;
    struct dirent *de;
    d = fdopendir(dfd);
    if (d == NULL) {
        close(dfd);
        return 0;
    }
    while ((de = readdir(d))) {
        const char *name = de->d_name;
        if (fstatat(dfd, name, &s, AT_SYMLINK_NOFOLLOW) == 0) {
            size += stat_size(&s);
        }
        if (de->d_type == DT_DIR) {
            int subfd;
            /* always skip "." and ".." */
            if (name[0] == '.') {
                if (name[1] == 0)
                    continue;
                if ((name[1] == '.') && (name[2] == 0))
                    continue;
            }
            subfd = openat(dfd, name, O_RDONLY | O_DIRECTORY);
            if (subfd >= 0) {
                size += calculate_dir_size(subfd);
            }
        }
    }
    closedir(d);
    return size;
}

/*
 int dirfd = open(path.c_str(), O_DIRECTORY, O_RDONLY);
    if (dirfd < 0) {
        PLOG(WARNING) << "Failed to open " << path;
        return -1;
    } else {
        uint64_t res = calculate_dir_size(dirfd);
        close(dirfd);
        return res;
    }
 * */
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


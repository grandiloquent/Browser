#ifndef HELPER_H__
#define HELPER_H__

#include <android/log.h>
#include <stdint.h>
#include <sys/stat.h>
#include <ctype.h>

#define LOG_TAG "TAG/Native"
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

#define ARRAY_COUNT(array) (sizeof((array)) / sizeof((array)[0]))

#define JAVA_METHOD(object, name, type, ...) \
  JNIEXPORT type JNICALL Java_ ## object ## _ ## name \
  (JNIEnv *env, ## __VA_ARGS__)

#define JAVA_INSTANCE_METHOD(object, name, type, ...) \
  JAVA_METHOD(object, name, type, jobject this, ## __VA_ARGS__)

#define JAVA_STATIC_METHOD(object, name, type, ...) \
  JAVA_METHOD(object, name, type, jclass class, ## __VA_ARGS__)

#define COPY_READ_ERROR (-2)
#define COPY_WRITE_ERROR (-3)

bool ends_with(const char *s1, const char *s2);

int64_t stat_size(struct stat *s);

int64_t calculate_dir_size(int dfd);

long parse_range(const char *s);

char *read_file(const char *filename, int *size);

void base64enc(char *out, const char *in);

int delete_directory(const char *path, char *nameBuffer, struct stat *statBuffer);

bool is_dir(const char *pathname);

int starts_with(const char *str, const char *prefix);

int istarts_with(const char *str, const char *prefix);

int copy_fd(int ifd, int ofd);

int copy_file(const char *dst, const char *src, int mode);

void copy_directory(const char *src, const char *dest);

#endif
# Browser 文件管理器

* [ ] Web 服务器
* [x] 有道字典

## 引用

- https://github.com/DaveGamble/cJSON
- https://github.com/cesanta/mongoose
- https://github.com/tronkko/dirent
- https://github.com/EZLippi/Tinyhttpd
- https://github.com/SheetJS/js-crc32


## 参考

- https://www.man7.org/linux/man-pages/man3/getaddrinfo.3.html

## 错误

## `IPELINE_ERROR_DECODE: Failed to send audio packet for decoding`

注释掉 `mongoose.c`中的：

```c
#if _FILE_OFFSET_BITS == 64 || _POSIX_C_SOURCE >= 200112L || \
    _XOPEN_SOURCE >= 600
                fseeko(pd->file.fp, r1, SEEK_SET);
```


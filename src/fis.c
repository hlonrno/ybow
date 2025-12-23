#ifndef FIS_C
#define FIS_C
#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>

typedef struct {
    FILE *file;
    char *buf;
    size_t bufi;
    size_t file_size;
    size_t buf_len;
    bool closed;
} FIS; // File Input Stream;

bool fis_open(FIS *fis, const char *file_name) {
    fis->file = fopen(file_name, "rb");
    if (!fis->file) {
        goto err;
    }

    fis->buf_len = 256;
    fis->buf = malloc(fis->buf_len);
    if (!fis->buf) {
        goto err;
    }

    if (fseek(fis->file, 0, SEEK_END)) {
        goto err;
    }
    fis->file_size = ftell(fis->file);
    rewind(fis->file);

    fis->buf_len = fread(fis->buf, 1, fis->buf_len, fis->file);
    fis->closed = false;
    return false;

err:
    if (fis->file)
        fclose(fis->file);
    fis->file = NULL;
    if (fis->buf)
        free(fis->buf);
    return true;
}

void fis_close(FIS *fis) {
    if (fis->file)
        fclose(fis->file);
    fis->file = NULL;
    if (fis->buf)
        free(fis->buf);
    fis->closed = true;
}

bool fis_read_byte(FIS *fis, char *c) {
    if (fis->closed) {
        *c = '\0';
        return true;
    }

    *c = fis->buf[fis->bufi++];
    if (fis->bufi >= fis->buf_len) {
        fis->buf_len = fread(fis->buf, 1, fis->buf_len, fis->file);
        fis->bufi = 0;
    }
    if (!fis->buf_len) {
        fis_close(fis);
        return true;
    }
    // printf("buf:%zu %c\n", fis->bufi - 1, *b);
    return false;
}

#endif // FIS_C

#ifndef FIS_C
#define FIS_C
#include "std.c"
#include <stdio.h>

#ifndef BUF_SIZE
#define BUF_SIZE 255
#endif

typedef struct {
    FILE *file;
    char *buf;
    u16 buf_len;
    u16 bufi;
} FIS;

void fis_init(FIS *this, Alloc *allo, FILE *file) {
    this->file = file;
    this->buf = (char *)aalloc(allo, BUF_SIZE);
    this->buf_len = BUF_SIZE;
    this->bufi = BUF_SIZE;
}

int fis_readc(FIS *this, char *c) {
    if (this->bufi >= this->buf_len) {
        this->bufi = 0;
        this->buf_len = fread(
                this->buf,
                1,
                this->buf_len,
                this->file);
    }
    if (this->buf_len == 0)
        return *c = 0;
    *c = this->buf[this->bufi++];
    return 1;
}

void fis_offset(FIS *this, i32 offset) {
    if (abs(offset) <= this->bufi) {
        this->bufi += offset;
        return;
    }
    fseek(this->file, this->bufi + offset, SEEK_CUR);
    this->bufi = 0;
    this->buf_len = fread(this->buf, 1, BUF_SIZE, this->file);
}

#endif // FIS_C

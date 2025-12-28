#ifndef ALLOC_C
#define ALLOC_C
#include <stdlib.h>

#ifndef ARENA_SIZE
#define ARENA_SIZE 4096
#endif // ARENA_SIZE

typedef struct {
    void *start;
    void *current;
} Alloc;

void ainit(Alloc *this) {
    this->start = malloc(ARENA_SIZE);
    this->current = this->start + sizeof(Alloc);
    *(Alloc *)this->start = (Alloc){0};
}

void *aalloc(Alloc *this, size_t size) {
    void *loc = this->current;
    if (loc - this->start + size >= ARENA_SIZE) {
        if (this->start == 0)
            ainit(this->start);
        return aalloc(this->start, size);
    }
    this->current += size;
    return loc;
}

void afree(Alloc *this) {
    if (this->start)
        afree(this->start);
    free(this->start);
}

#endif // ALLOC_C

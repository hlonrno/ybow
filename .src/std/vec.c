#ifndef VEC_C
#define VEC_C
#include <stdlib.h>

typedef struct {
    size_t len;
    size_t capacity;
    char *data;
} Vec;

#define vec_init(v)                    \
    (v)->len = 0;                      \
    (v)->capacity = 32;                \
    (v)->data = malloc((v)->capacity);

#define vec_add(v, t)                                  \
    if (++(v)->len >= (v)->capacity) {                 \
        (v)->capacity += (v)->capacity >> 1;           \
        (v)->data = realloc((v)->data, (v)->capacity); \
    }                                                  \
    memcpy((v)->data + ((v)->len - 1) * sizeof(*(t)), (t), sizeof(*(t)));

#endif // VEC_C

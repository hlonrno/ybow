#ifndef VEC_C
#define VEC_C
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct {
    size_t size;
    size_t capacity;
    size_t elem_size;
    char *data;
} Vec;

#define vec_new(name, esize) \
    Vec name = {0};          \
    vec_init(&name, esize);

#define vec_init(v, esize)                                \
    if (!(v)->capacity)                                   \
        (v)->capacity = 32;                               \
    (v)->elem_size = (esize);                             \
    if (!(v)->data)                                       \
        (v)->data = malloc((v)->capacity * (v)->elem_size);

#define vec_add(v, e)                                                    \
    if ((v)->size >= (v)->capacity) {                                    \
        (v)->capacity += (v)->capacity >> 1;                             \
        (v)->data = realloc((v)->data, (v)->capacity * (v)->elem_size);  \
    }                                                                    \
    memcpy((v)->data + (v)->size++ * (v)->elem_size, (e), (v)->elem_size);

#define vec_shrink(v)                                 \
    (v)->capacity = ((v)->size - 1) * (v)->elem_size; \
    (v)->data = realloc((v)->data, (v)->capacity);

#endif // VEC_C

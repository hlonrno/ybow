#ifndef HASHMAP_C
#define HASHMAP_C
#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#ifndef HASHFUNCTION
#define HASHFUNCTION hashmap_hash_djb2_xor
#endif // HASHFUNCTION

size_t hashmap_hash_sdbm(char *key, size_t size) {
    size_t hash = 0;
    while (--size > 0)
        hash = *key++ + (hash << 6) + (hash << 16) - hash;
    return hash;
}

size_t hashmap_hash_djb2(char *key, size_t size) {
    size_t hash = 5381;
    while (--size > 0)
        hash = (hash << 5) + hash + *key++;
    return hash;
}

size_t hashmap_hash_djb2_xor(char *key, size_t size) {
    size_t hash = 5381;
    while (--size > 0)
        hash = ((hash << 5) + hash) ^ *key++;
    return hash;
}

typedef struct Bucket {
    struct Bucket *next;
    char state;
    void *key;
    void *val;
} Bucket;

typedef struct {
    size_t full;
    size_t capacity;
    size_t key_size;
    size_t val_size;
    Bucket *data;
} HashMap;

#define hashmap_init(this, key_t, val_t)                      \
    (this)->full = 0;                                         \
    (this)->capacity = 32;                                    \
    (this)->key_size = sizeof(key_t);                         \
    (this)->val_size = sizeof(val_t);                         \
    (this)->data = malloc(sizeof(Bucket) * (this)->capacity);

#define hashget(this, hash) (this->data + (hash % this->capacity) * sizeof(Bucket))

Bucket *hashmap_get_buck(HashMap *this, void *key) {
    Bucket *cell = (Bucket *)hashget(this, HASHFUNCTION(key, this->key_size));
    if (cell != NULL)
        while (cell->state & 1 || memcmp(cell->key, key, this->key_size) != 0) {
            cell = cell->next;
            if (cell == NULL)
                break;
        }
    return cell;
}


void hashmap_put(HashMap *this, void *key, void *val) {
    Bucket *cell = hashmap_get_buck(this, key);
    if (cell) {
        this->full += cell->state & 1;
        cell->state = 3;
        cell->key = key;
        cell->val = val;
    } else
        printf("AH!\n");
}

#undef hashmapget

#endif // HASHMAP_C

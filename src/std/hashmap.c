#ifndef HASHMAP_C
#define HASHMAP_C
#include "alloc.c"
#include "string.c"
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
    char occupied;
    StringView *key;
    StringView *value;
} Bucket;

typedef struct {
    size_t full;
    size_t capacity;
    Bucket *data;
} HashMap;

#define hashmap_init(this)                                    \
    (this)->full = 0;                                         \
    (this)->capacity = 32;                                    \
    (this)->data = calloc((this)->capacity, sizeof(Bucket));

char keys_equal(StringView *a, StringView *b) {
    if (a->len != b->len)
        return 0;
    for (size_t i = 0; i < a->len; ++i)
        if (a->str[i] != b->str[i])
            return 0;
    return 1;
}

Bucket *hashmapget_buck(HashMap *this, StringView *key) {
    Bucket *cell = this->data +
        sizeof(Bucket) *
        (HASHFUNCTION(key->str, key->len) % this->capacity);
    do {
        if (!cell->occupied)
            return cell;
        if (keys_equal(cell->key, key))
            return cell;
        cell = cell->next;
    } while (1);
}

void hashmap_put(HashMap *this, Alloc *allo, void *key, void *value);

void hashmap_realloc(HashMap *this, Alloc *allo, size_t capacity) {
    HashMap new = {0};
    new.capacity = capacity;
    new.data = calloc(capacity, sizeof(Bucket));

    for (size_t i = 0; i < this->capacity; ++i) {
        Bucket *cell = this->data + i * this->capacity;
        while (cell->occupied) {
            hashmap_put(&new, allo, cell->key, cell->value);
            cell = cell->next;
        }
    }

    free(this->data);
    this->capacity = capacity;
    this->data = new.data;
}

void *hashmap_get(HashMap *this, void *key) {
    Bucket *buck = hashmapget_buck(this, key);
    if (buck->occupied)
        return buck->value;
    return NULL;
}

void hashmap_put(HashMap *this, Alloc *allo, void *key, void *value) {
    if ((float)this->full / this->capacity > 0.75f)
        hashmap_realloc(this, allo, this->capacity << 1);

    Bucket *buck = hashmapget_buck(this, key);
    if (buck->occupied) {
        buck->value = value;
        return;
    }
    buck->occupied = 1;
    buck->key = key;
    buck->value = value;
    buck->next = aalloc(allo, sizeof(Bucket));
    ++this->full;
}

#endif // HASHMAP_C

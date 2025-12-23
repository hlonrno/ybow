#ifndef LEXER_C
#define LEXER_C
#include "vec.c"
#include "fis.c"
#include <stdbool.h>
#include <stdlib.h>

typedef enum {
    SpecialCharacter,
    NumberLiteral,
    CharacterLiteral,
    StringLiteral,
    IdentifierLiteral
} TokenType;

typedef struct {
    TokenType type;
    char *value;
    size_t line;
    size_t end_line;
    size_t begin;
    size_t end;
} Token;

typedef struct {
    char *err_message;
    bool err;
    bool res;
} ErrRes;

typedef struct {
    Token ctok;
    ErrRes res;
    FIS *fis;
    char cc;
} Lexer;

bool lex_init(Lexer *lex, FIS *fis) {
    lex->fis = fis;
    lex->ctok.end_line = 1;
    lex->ctok.end = 1;
    return fis_read_byte(fis, &lex->cc);
}

#define is_whitespace(c) \
      (c == ' '          \
    || c == '\n'         \
    || c == '\t'         \
    || c == '\r'         \
    || c == '\b')

#define safe_advance                       \
    ++lex->ctok.end;                       \
    if (fis_read_byte(lex->fis, &lex->cc))

#define SYMBOWLS ";@=:|<->()_?.#,[{]}+%&^~!*/"
#define NUMBERS "0123456789"
#define HEX NUMBERS"abcdefABCDEF"
 
void lex_get_token(Lexer *lex) {
    if (lex->fis->closed)
        return;

    lex->res.err = false;
    lex->res.res = false;
    while (!lex->fis->closed) {
        if (lex->cc == '\n') {
            ++lex->ctok.end_line;
            lex->ctok.end = 0;
            safe_advance return;
        }
        if (!is_whitespace(lex->cc))
            break;
        safe_advance return;
    }

    lex->ctok.begin = lex->ctok.end;
    lex->ctok.line = lex->ctok.end_line;
    if (lex->cc == '`') {
        safe_advance return;
        lex->ctok.type = CharacterLiteral;
        if (strchr("\n\t\r\b", lex->cc)) {
            lex->res.err = true;
            lex->res.err_message = "characters \"\\n\\t\\b\\r\" not allowed in character expresions.";
            return;
        }

        if (lex->cc == '\\') {
            char chr = '\\';
            vec_new(v, sizeof(char));
            vec_add(&v, &chr);
            safe_advance return;
            bool skip = false;
            if (strchr("nrtbxo", lex->cc)) {
                vec_add(&v, &lex->cc);
                skip = lex->cc != 'x' || lex->cc != 'o';
                safe_advance {}
            }
            if (!skip)
                while (strchr(HEX, lex->cc)) {
                    vec_add(&v, &lex->cc);
                    safe_advance return;
                }
            chr = '\0';
            vec_add(&v, &chr);
            vec_shrink(&v);
            lex->ctok.value = v.data;
        } else {
            lex->ctok.value    = malloc(2);
            lex->ctok.value[0] = lex->cc;
            lex->ctok.value[1] = '\0';
            safe_advance {}
        }
        lex->res.res = true;
        return;
    }

    if (lex->cc == '"') {
        safe_advance return;
        lex->ctok.type = StringLiteral;
        bool multiline = lex->cc == '\n';
        vec_new(v, sizeof(char));

        while (lex->cc != '"') {
            if (lex->cc == '\\') {
                vec_add(&v, &lex->cc);
                safe_advance break;
            }
            if (lex->cc == '\n') {
                ++lex->ctok.end_line;
                if (!multiline) {
                    lex->res.err = true;
                    lex->res.err_message = "string is not multiline.";
                    return;
                }
            }
            vec_add(&v, &lex->cc);
            safe_advance break;
        }

        char chr = '\0';
        vec_add(&v, &chr);
        vec_shrink(&v);
        lex->ctok.value = v.data;
        lex->res.res = true;
        safe_advance {}
        return;
    }

    if (strchr(NUMBERS, lex->cc)) {
        lex->ctok.type = NumberLiteral;
        vec_new(v, sizeof(char));
        char dot_count = 0;
        while (strchr(NUMBERS".", lex->cc)) {
            if (lex->cc == '.') {
                ++dot_count;
                if (dot_count > 1)
                    break;
            }
            vec_add(&v, &lex->cc);
            safe_advance break;
        }
        if (dot_count < 2) {
            if (lex->cc == 'u' || lex->cc == 'U') {
                vec_add(&v, &lex->cc);
                safe_advance {}
            }
            if (strchr("flFL", lex->cc)) {
                vec_add(&v, &lex->cc);
                safe_advance {}
            }
        }

        dot_count = '\0';
        vec_add(&v, &dot_count);
        vec_shrink(&v);
        lex->ctok.value = v.data;
        lex->res.res = true;
        return;
    }

    bool ran = false;
    vec_new(v, sizeof(char));
    while (strchr(SYMBOWLS, lex->cc)) {
        ran = true;
        vec_add(&v, &lex->cc);
        safe_advance break;
    }
    if (ran) {
        lex->ctok.type = SpecialCharacter;
        char chr = '\0';
        vec_add(&v, &chr);
        vec_shrink(&v);
        lex->ctok.value = v.data;
        lex->res.res = true;
        return;
    }

    while (!strchr(NUMBERS"`\""SYMBOWLS" \n\t\r\b", lex->cc)) {
        ran = true;
        vec_add(&v, &lex->cc);
        safe_advance break;
    }
    if (ran) {
        lex->ctok.type = IdentifierLiteral;
        char chr = '\0';
        vec_add(&v, &chr);
        vec_shrink(&v);
        lex->ctok.value = v.data;
        lex->res.res = true;
        return;
    }

    free(v.data);
    lex->res.err = true;
    lex->res.err_message = "unexpected character. (wtf??\?)";
    safe_advance {}
}

#undef safe_advance
#undef is_whitespace
#undef SYMBOWLS
#undef NUMBERS
#undef HEX
#endif // LEXER_C

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
    return fis_read_byte(fis, &lex->cc);
}

#define is_whitespace(c) \
      (c == ' '          \
    || c == '\n'         \
    || c == '\t'         \
    || c == '\r'         \
    || c == '\b')

#define safe_advance { if (fis_read_byte(lex->fis, &lex->cc)) goto err; ++lex->ctok.end; }

void lex_get_token(Lexer *lex) {
    if (lex->fis->closed)
        return;

    while (!lex->fis->closed) {
        if (lex->cc == '\n') {
            ++lex->ctok.end_line;
            lex->ctok.end = 0;
        }
        if (!is_whitespace(lex->cc))
            break;
        safe_advance;
    }

    lex->ctok.begin = lex->ctok.end;
    lex->ctok.line = lex->ctok.end_line;
    if (lex->cc == '`') {
        lex->ctok.type = CharacterLiteral;
        safe_advance;
        if (lex->cc == '\n') {
            lex->res.err = true;
            lex->res.err_message = "character `\\n` not allowed in character expresions.";
        }
        if (lex->cc != '\\') {
            lex->ctok.value = malloc(sizeof(lex->cc) << 1);
            lex->ctok.value[0] = lex->cc;
            lex->ctok.value[1] = '\0';
            lex->res.res = true;
            safe_advance;
            return;
        } else {
            // ESC SEQ
            safe_advance;
            const char *chars = "nrtb1234567890abcdefABCDEFxo";
            vec_new(v, sizeof(char));
            char chr;
            chr = '\\';
            vec_add(&v, &chr);
            while (strchr(chars, lex->cc)) {
                vec_add(&v, &lex->cc);
                safe_advance;
            }
            chr = '\0';
            vec_add(&v, &chr);
            vec_shrink(&v);
            lex->ctok.value = v.data;
            lex->res.res = true;
            return;
        }
    } else if (lex->cc == '"') {
        lex->ctok.type = StringLiteral;
        safe_advance;
        bool multiline = lex->cc == '\n';
        // TODO: include begining \n or not
        if (multiline) {
            ++lex->ctok.end_line;
            safe_advance;
        }
        vec_new(v, sizeof(char));
        while (lex->cc != '"') {
            if (lex->cc == '\\') {
                vec_add(&v, &lex->cc);
                safe_advance;
            }
            if (lex->cc == '\n') {
                ++lex->ctok.end_line;
                if (!multiline) {
                    lex->res.err = true;
                    lex->res.err_message = "this string is not multiline!";
                    return;
                }
            }
            vec_add(&v, &lex->cc);
            safe_advance;
        }
        char chr = '\0';
        vec_add(&v, &chr);
        vec_shrink(&v);
        lex->ctok.value = v.data;
        lex->res.res = true;
        safe_advance;
    } else {
        lex->res.err = true;
        lex->res.err_message = "unexpected character.";
        return;
    }

    // TODO: NumberLiteral
    // CharacterLiteral
    // StringLiteral
    // TODO: IdentifierLiteral 
err:
    lex->res.err = true;
}

/*
ErrRes tokenize(FIS *fis) {
    char c;
    Token tok = {0};
    vec_new(toks, sizeof(Token));
    tok.end_line = 1;
    fis_read_byte(fis, &c);
    while (!fis->closed) {
        if (c == '\n') {
            ++tok.end_line;
            tok.end = 0;
            safe_advance;
            continue;
        }
        if (is_whitespace(c)) {
            safe_advance;
            continue;
        }

        tok.begin = tok.end;
        tok.line = tok.end_line;
        if (c == '`') {
            tok.type = CharacterLiteral;
            safe_advance;
            if (c == '\n') {
                return err("character `\\n` not allowed in character expresions.");
            }
            if (c != '\\') {
                tok.value = malloc(sizeof(c) << 1);
                tok.value[0] = c;
                tok.value[1] = '\0';
                vec_add(&toks, &tok);
                safe_advance;
            } else {
                // ESC SEQ
                safe_advance;
                const char *chars = "nrtb1234567890abcdefABCDEFxo";
                vec_new(v, sizeof(char));
                char chr;
                chr = '\\';
                vec_add(&v, &chr);
                while (strchr(chars, c)) {
                    vec_add(&v, &c);
                    safe_advance;
                }
                chr = '\0';
                vec_add(&v, &chr);
                vec_shrink(&v);
                tok.value = v.data;
                vec_add(&toks, &tok);
            }
        } else if (c == '"') {
            tok.type = StringLiteral;
            safe_advance;
            bool multiline = c == '\n';
            // TODO: include begining \n or not
            if (multiline) {
                ++tok.end_line;
                safe_advance;
            }
            vec_new(v, sizeof(char));
            while (c != '"') {
                if (c == '\\') {
                    vec_add(&v, &c);
                    safe_advance;
                }
                if (c == '\n') {
                    ++tok.end_line;
                    if (!multiline) {
                        return err("this string is not multiline!");
                    }
                }
                vec_add(&v, &c);
                safe_advance;
            }
            char chr = '\0';
            vec_add(&v, &chr);
            vec_shrink(&v);
            tok.value = v.data;
            vec_add(&toks, &tok);
            safe_advance;
        } else {
            return err("unexpected character.");
        }

        // TODO: NumberLiteral
        // CharacterLiteral
        // StringLiteral
        // TODO: IdentifierLiteral 
    }
    return (ErrResult) { .tokens = toks };
}
*/
#undef safe_advance
#undef err
#undef is_whitespace
#endif // LEXER_C

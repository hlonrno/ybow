#ifndef LEXER_C
#define LEXER_C
#include <string.h>
#include "std.c"
#include "fis.c"

typedef enum {
    SpecialChar,
    Identifier,
    StringLiteral,
    CharLiteral,
    IntLiteral,
    FloatLiteral,
} TokenType;

typedef struct {
    TokenType type;
    char *repr;
    u32 start_line;
    u16 start_char;
    u32 end_line;
    u16 end_char;
    union {
        StringView s;
        u8 c;
        u64 n;
        f64 d;
    } value;
} Token;

typedef struct {
    Token tok;
    FIS *fis;
    const char *err;
    char cc;
} Lexer;

void lexer_init(Lexer *this, FIS *fis) {
    this->fis = fis;
    this->tok.end_line = 1;
}

#define advance           \
    ++this->tok.end_char; \
    if (!fis_readc(this->fis, &this->cc))

#define WHITESPACE " \n\t\b\r"
#define NUMBERS "0123456789"
#define SPECIAL_CHARS "{}[]().:;=-+*/%|&^!~"

u64 lexerParse_number(Lexer *this) {
    this->err = "lexerParse_number is not implemented";
    advance {}
    return 0;
}

u16 lexerParse_str_char(Lexer *this) {
    if (this->cc != '\\') {
        return this->cc;
    }
    advance return 0;
    switch (this->cc) {
        case 'n':
            return '\n';
        case 't':
            return '\t';
        case 'b':
            return '\b';
        case 'r':
            return '\r';
        case '0':
            return lexerParse_number(this);
    }
    this->err = "unknown escape sequence.";
    advance {}
    return 256;
}

int lexerString_literal_parse(Lexer *this, Alloc *allo) {
    Vec v = {0};
    Vec repr = {0};
    vec_init(&v);
    vec_init(&repr);
    char c = '"';
    vec_add(&repr, &this->cc);
    advance return 0;
    u8 multiline = this->cc == '\n';
    while (this->cc != '"') {
        if (this->cc == '\n') {
            if (multiline)
                ++this->tok.end_line;
            else {
                free(v.data);
                free(repr.data);
                this->err = "string is not multiline.";
                advance {}
                return 0;
            }
        }
        u16 temp = lexerParse_str_char(this);
        if (temp == 256)
            return 0;
        c = temp;
        vec_add(&v, &c);
        if (strchr("\t\b\r\0", this->cc) || (!multiline && this->cc == '\n')) {
            c = '\\';
            vec_add(&repr, &c);
        } else {
            vec_add(&repr, &c);
        }
        advance break;
    }
    c = '"';
    vec_add(&repr, &c);
    c = '\0';
    vec_add(&repr, &c);
    this->tok.type = StringLiteral;
    this->tok.value.s.len = v.len;
    this->tok.value.s.str = aalloc(allo, v.len);
    memcpy(this->tok.value.s.str, v.data, v.len);
    this->tok.repr = aalloc(allo, repr.len);
    memcpy(this->tok.repr, repr.data, repr.len);
    free(v.data);
    free(repr.data);
    advance {}
    return 1;
}

int lexerChar_literal_parse(Lexer *this, Alloc *allo) {
    advance return 0;
    u16 temp = lexerParse_str_char(this);
    if (temp == 256)
        return 0;
    this->tok.type = CharLiteral;
    this->tok.value.c = temp;
    Vec repr = {0};
    vec_init(&repr);
    char c = '`';
    vec_add(&repr, &c);
    if (strchr("\n\t\b\r\0", this->tok.value.c))
        c = '\\';
    else
        c = this->tok.value.c;
    vec_add(&repr, &c);
    this->tok.repr = aalloc(allo, repr.len);
    memcpy(this->tok.repr, repr.data, repr.len);
    advance {}
    return 1;
}

int lexerNum_literal_parse(Lexer *this, Alloc *allo) {
    Vec repr = {0};
    vec_init(&repr);
    u64 num = 0;
    u64 dotdigitcount = 1;
    u8 dotcount = 0;
    while (strchr(NUMBERS".", this->cc)) {
        vec_add(&repr, &this->cc);
        if (this->cc == '.') {
            ++dotcount;
            if (dotcount > 1)
                break;
        } else {
            num *= 10;
            num += this->cc - '0';
            if (dotcount > 0)
                dotdigitcount *= 10;
        }
        advance break;
    }
    this->tok.type = IntLiteral;
    this->tok.value.n = num;
    if (dotcount > 0) {
        this->tok.type = FloatLiteral;
        this->tok.value.d = (num / (f64)dotdigitcount);
    }
    char c = '\0';
    vec_add(&repr, &c);
    this->tok.repr = aalloc(allo, repr.len);
    memcpy(this->tok.repr, repr.data, repr.len);
    return 1;
}

int lexerSpecial_char_parse(Lexer *this, Alloc *allo) {
    this->tok.type = SpecialChar;

    if (strchr("()[]{}.;", this->cc)) {
        this->tok.repr = aalloc(allo, 2);
        this->tok.repr[0] = this->cc;
        this->tok.repr[1] = 0;
        this->tok.value.s.len = 1;
        this->tok.value.s.str = this->tok.repr;
        advance {}
        return 1;
    }

    this->err = "unknown operator.";
    advance {}
    return 0;
}

int lexerIdentifier_parse(Lexer *this, Alloc *allo) {
    this->tok.type = Identifier;
    Vec v = {0};
    vec_init(&v);
    while (!strchr("\"'"SPECIAL_CHARS WHITESPACE,  this->cc)) {
        vec_add(&v, &this->cc);
        advance break;
    }
    char c = '\0';
    vec_add(&v, &c);
    this->tok.value.s.len = v.len - 1;
    this->tok.value.s.str = aalloc(allo, v.len);
    memcpy(this->tok.value.s.str, v.data, v.len);
    this->tok.repr = this->tok.value.s.str;
    free(v.data);
    return 1;
}

int lexer_next_token(Lexer *this, Alloc *allo) {
    this->err = NULL;
    while (strchr(WHITESPACE, this->cc)) {
        if (this->cc == '\n') {
            ++this->tok.end_line;
            this->tok.end_char = 0;
        }
        /*
        if (this->cc == '/') {
            advance return 0;
            if (this->cc == '/') {
                while (this->cc != '\n') {
                    advance return 0;
                }
            } else {
                fis_offset(this->fis, -1);
                break;
            }
        }
        */
        advance return 0;
    }
    this->tok.start_line = this->tok.end_line;
    this->tok.start_char = this->tok.end_char;
    if (this->cc == '"') {
        return lexerString_literal_parse(this, allo);
    }
    if (this->cc == '`') {
        return lexerChar_literal_parse(this, allo);
    }
    if (strchr(NUMBERS, this->cc)) {
        return lexerNum_literal_parse(this, allo);
    }
    if (strchr(SPECIAL_CHARS, this->cc)) {
        return lexerSpecial_char_parse(this, allo);
    }
    return lexerIdentifier_parse(this, allo);
}

#endif // LEXER_C

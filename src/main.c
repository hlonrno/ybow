#include "fis.c"
#include "lexer.c"
#include <stdio.h>
#include <stdbool.h>

int main(int argc, char **argv) {
    if (argc < 2) {
        printf("No args provided.\n");
        return -1;
    }

    FIS fis = {0};
    const char *file_name = argv[1];
    if (fis_open(&fis, file_name)) {
        printf("Couldn't open fis.\n");
        goto err;
    }

    Lexer lex = {0};
    if (lex_init(&lex, &fis)) {
        printf("Couldn't init lex.\n");
        goto err;
    }

    do {
        lex_get_token(&lex);
        if (lex.res.res) {
            const char *ttype;
            switch (lex.ctok.type) {
                case SpecialCharacter:  ttype = "spc"; break;
                case NumberLiteral:     ttype = "num"; break;
                case CharacterLiteral:  ttype = "chr"; break;
                case StringLiteral:     ttype = "str"; break;
                case IdentifierLiteral: ttype = "idn"; break;
                default:                ttype = "wtf";
            }
            printf("%s:%zu:%zu: (%s) %s\n",
                file_name, lex.ctok.line, lex.ctok.begin, ttype, lex.ctok.value);
        }
        if (lex.res.err)
            goto err;
    } while (!lex.fis->closed);

    /*
    ErrResult res = tokenize(&fis);
    Vec toks = res.tokens;
    
    if (res.err) {
        printf("%s:%zu:%zu: \33[31mError:\33[0m %s\n",
            file_name, res.line, res.column, res.message);
    }
    if (res.res) {
        printf("%s: total %zu\n", file_name, toks.size);
        for (size_t i = 0; i < toks.size; i++) {
            Token tok = ((Token *)toks.data)[i];
            const char *ttype;
            switch (tok.type) {
                case SpecialCharacter:  ttype = "spc"; break;
                case NumberLiteral:     ttype = "num"; break;
                case CharacterLiteral:  ttype = "chr"; break;
                case StringLiteral:     ttype = "str"; break;
                case IdentifierLiteral: ttype = "idn"; break;
                default:                ttype = "wtf";
            }
            printf("%s:%zu:%zu: (%s) %s\n",
                    file_name, tok.line, tok.begin, ttype, tok.value);
        }
    }
    return 0;
    */

err:
    if (!fis.closed)
        fis_close(&fis);
    if (lex.res.err) {
        printf("%s:%zu:%zu: \33[31mError:\33[0m %s\n",
            file_name, lex.ctok.line, lex.ctok.begin, lex.res.err_message);
    }
    return -1;
}


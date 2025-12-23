#include "fis.c"
#include "lexer.c"
#include <stdio.h>
#include <stdbool.h>
#include <stdlib.h>

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
        printf("%s: \33[31mError:\33[0m Couldn't init lexer. Perhaps, it's an empty file?\n",
            file_name);
        goto err;
    }

    int err_count = 0;
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
            if (lex.ctok.type == StringLiteral && lex.ctok.value[0] == '\0') {
                lex.ctok.value = realloc(lex.ctok.value, 3);
                lex.ctok.value[0] = '\\';
                lex.ctok.value[1] = '0';
                lex.ctok.value[2] = '\0';
            }
            printf("%s:%zu:%zu: (%s) %s",
                file_name, lex.ctok.line, lex.ctok.begin, ttype, lex.ctok.value);
            free(lex.ctok.value);
        }
        if (lex.res.err) {
            printf("%s:%zu:%zu: \33[31mError:\33[0m %s",
                file_name, lex.ctok.line, lex.ctok.begin, lex.res.err_message);
            ++err_count;
        }
        printf("\n");
    } while (!lex.fis->closed);
    printf("%d errors.\n", err_count);
    return 0;

err:
    if (!fis.closed)
        fis_close(&fis);
    return -1;
}


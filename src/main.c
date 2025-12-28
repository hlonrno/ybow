#include "std.c"
#include "fis.c"
#include "lexer.c"

int main(int argc, char **argv) {
    if (argc == 1) {
        return -1;
    }
    Alloc allo = {0};
    ainit(&allo);

    const char *file_name = argv[1];
    FILE *file = fopen(file_name, "rb");
    FIS fis = {0}; fis_init(&fis, &allo, file);
    Lexer lex = {0}; lexer_init(&lex, &fis);

    while (lex.fis->buf_len != 0) {
        lexer_next_token(&lex, &allo);
        if (lex.err) {
            printf("%s:%u:%u: \33[31merror:\33[0m %s\n",
                file_name, lex.tok.start_line, lex.tok.start_char,
                lex.err);
            break;
        }
        const char *ttype = "???";
        switch (lex.tok.type) {
            case SpecialChar:   ttype = "spc"; break;
            case Identifier:    ttype = "idn"; break;
            case StringLiteral: ttype = "str"; break;
            case CharLiteral:   ttype = "chr"; break;
            case IntLiteral:    ttype = "int"; break;
            case FloatLiteral:  ttype = "flt"; break;
        }
        printf("%d:%d: (%3s) %s\n",
            lex.tok.start_line, lex.tok.start_char,
            ttype, lex.tok.repr);
    }
    
    fclose(file);
    afree(&allo);
}

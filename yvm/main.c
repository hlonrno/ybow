#include <stdio.h>

int main(int argc, char **argv) {
    if (argc == 1) {
        printf("Usage: yvm file.yb");
        return 1;
    }

    char *file_name = argv[1];
    printf("file: \"%s\"\n", file_name);

    return 0;
}

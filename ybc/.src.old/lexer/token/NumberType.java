package src.lexer.token;

public enum NumberType {
    Byt('b'),
    Srt('s'),
    Int('i'),
    Lng('l'),
    Flt('f'),
    Dub('d');

    public final char extension;
    private NumberType(char ext) {
        extension = ext;
    }
}

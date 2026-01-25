package src.lexer.token;

import src.lexer.NumberParsingException;

public class NumberToken extends Token {
    public NumberType numberType;
    public long literalInt;
    public float literalFlt;
    public double literalDub;

    public NumberToken(Token tok) throws NumberParsingException {
        type = TokenType.NumberLiteral;
        line = tok.line;
        column = tok.column;
        value = tok.value;
        try {
            if (value.indexOf('.') > -1) {
                if (value.length() - value.indexOf('.') > 8) {
                    numberType = NumberType.Dub;
                    literalDub = Double.parseDouble(value);
                } else {
                    numberType = NumberType.Flt;
                    literalFlt = Float.parseFloat(value);
                }
            } else {
                literalInt = Long.parseLong(value);
                numberType = NumberType.Byt;
                if (literalInt > Byte.MAX_VALUE)
                    numberType = NumberType.Srt;
                if (literalInt > Short.MAX_VALUE)
                    numberType = NumberType.Int;
                if (literalInt > Integer.MAX_VALUE)
                    numberType = NumberType.Lng;
            }
        } catch (NumberFormatException e) {
            throw new NumberParsingException("Number is too large to parse: " + tok.value);
        }
    }

    @Override
    public String toString() {
        return String.format("Noken %3d:%-3d %-16s %s%s",
            line, column, type.toString(), value, numberType.extension);
    }
}

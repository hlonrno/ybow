package src.lexer;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import src.Result;
import src.lexer.exception.*;

public class Lexer implements Iterator<Result<Token, Exception>> {
    public static final String SYMBOWLS = "@{}()[].,;>-+*/%=!<~&|^:";
    public static final String WHITESPACE = " \n\t\r\b\f";
    public static final String NOT_IDENTIFIER = SYMBOWLS + WHITESPACE;
    public final String SOURCE;
    private final Reader reader;
    private Token tok;
    private boolean eos, err;
    private char cc;

    public Lexer(final Reader reader, final String source) throws IOException {
        this.reader = reader;
        this.SOURCE = source;
        tok = new Token();
        tok.line = 1;
        eos = err = false;
        advanceEOS();
        while (WHITESPACE.indexOf(cc) > -1)
            advanceEOS();
    }

    private void advanceEOS() throws IOException {
        int c = reader.read();
        if (c == '\n') {
            tok.line++;
            tok.column = 0;
        }
        tok.column++;
        if (c == -1) {
            eos = true;
            cc = 0;
        } else
            cc = (char)c;
    }

    private void advance() throws IOException, LexerException {
        int c = reader.read();
        if (cc == '\n') {
            tok.line++;
            tok.column = 0;
        }
        tok.column++;
        if (c == -1) {
            eos = err = true;
            throw new UnexpectedException("unexpected EOS.", SOURCE, tok);
        }
        else
            cc = (char)c;
    }

    public boolean hasNext() {
        return !(err || eos);
    }

    public Result<Token, Exception> next() {
        try {
            if (cc == '`')
                tokenizeCharacter();
            else if (cc == '"')
                tokenizeString();
            else if ("0123456789".indexOf(cc) > -1)
                tokenizeNumber();
            else if (SYMBOWLS.indexOf(cc) > -1)
                tokenizeSymbowl();
            else
                tokenizeIdentifier();

            while (WHITESPACE.indexOf(cc) > -1)
                advanceEOS();
        } catch (Exception e) {
            err = true;
            return Result.ofError(e);
        }
        return Result.ofValue(tok);
    }

    private void tokenizeCharacter() throws IOException, LexerException {
        // `#c`
        tok.type = TokenType.LCharacter;
        advance();
        tok.image = "`" + parseEscapeSequence() + "`";
        if (cc != '`') {
            throw new UnexpectedException("expected \"`\", got \"" + cc + "\".", SOURCE, tok);
        }
        advanceEOS();
    }

    private void tokenizeString() throws IOException, LexerException {
        // "#c*"
        tok.type = TokenType.LString;
        tok.image = "\"";
        advance();
        while (cc != '"') {
            tok.image += parseEscapeSequence();
        }
        advanceEOS();
        tok.image += '"';
    }

    private void tokenizeNumber() throws IOException, LexerException {
        tok.type = TokenType.LNumber;
        tok.image = parseSimpleNumber(); // 0-9
        if (tok.image.equals("0") && "box".indexOf(cc) > -1) {
            tok.image += parseBasedNumber(false);
            // [u][zsil]
            if (cc == 'u' || cc == 'U' ) {
                tok.image += cc;
                advanceEOS();
            }
            if ("zsilZSIL".indexOf(cc) > -1) {
                tok.image += cc;
                advanceEOS();
            }
            return;
        }
        boolean flt = false;
        if (cc == '.') {
            advance();
            tok.image += '.' + parseSimpleNumber();
            flt = true;
        }
        // e[+-]0-9 + d
        if (cc == 'e' || cc == 'E') {
            tok.image += cc;
            advance();
            if (cc == '+') {
                tok.image += cc;
                advance();
            } else if (cc == '-') {
                tok.image += cc;
                advance();
            }
            tok.image += parseSimpleNumber();
            return;
        }
        // [u][bsil]
        if (!flt && (cc == 'u' || cc == 'U' )) {
            tok.image += cc;
            advanceEOS();
        }
        if (!flt && "zsilZSIL".indexOf(cc) > -1) {
            tok.image += cc;
            advanceEOS();
            return;
        }
        // [fd]
        if ("fdFD".indexOf(cc) > -1) {
            tok.image += cc;
            advanceEOS();
        }
    }

    private void tokenizeSymbowl() throws Exception, Error {
        tok.type = switch (cc) {
            case '(' -> TokenType.SOpenBracket;
            case ')' -> TokenType.SClosedBracket;
            case '[' -> TokenType.SOpenSquareBracket;
            case ']' -> TokenType.SClosedSquareBracket;
            case '{' -> TokenType.SOpenCurlyBracket;
            case '}' -> TokenType.SClosedCurlyBracket;
            case '.' -> TokenType.SDot;
            case ',' -> TokenType.SComma;
            case '+' -> TokenType.SPlus;
            case '-' -> TokenType.SMinus;
            case '*' -> TokenType.SStar;
            case '/' -> TokenType.SSlash;
            case '%' -> TokenType.SPercent;
            case '&' -> TokenType.SAnd;
            case '|' -> TokenType.SPipe;
            case '^' -> TokenType.SArrow;
            case '~' -> TokenType.STilde;
            case '!' -> TokenType.SBang;
            case ':' -> TokenType.SColon;
            case ';' -> TokenType.SSemiColon;
            case '=' -> TokenType.SEquals;
            case '<' -> TokenType.SLess;
            case '>' -> TokenType.SGreater;
            case '@' -> TokenType.SMonkeyA;
            default -> throw new UnreachableError();
        };
        tok.image = String.valueOf(cc);
        advanceEOS();
        if (eos)
            return;
        if (cc == '/') {
            tok.type = TokenType.Comment;
            tok.image += cc;
            advanceEOS();
            while (cc != '\n' && !eos) {
                tok.image += cc;
                if (cc == '/') {
                    advanceEOS();
                    tok.image += cc;
                    if (cc == '/') {
                        advanceEOS();
                        return;
                    }
                }
                advanceEOS();
            }
        }
    }

    private void tokenizeIdentifier() throws IOException, LexerException {
        tok.image = String.valueOf(cc);
        advanceEOS();
        while (!(NOT_IDENTIFIER.indexOf(cc) > -1 || eos)) {
            tok.image += cc;
            advanceEOS();
        }
        if (cc == '!') {
            tok.image += cc;
            advanceEOS();
        }
        tok.type = switch (tok.image) {
            case "switch" -> TokenType.KSwitch;
            case "case" -> TokenType.KCase;
            case "if" -> TokenType.KIf;
            case "else" -> TokenType.KElse;
            case "loop" -> TokenType.KLoop;
            case "break" -> TokenType.KBreak;
            case "continue" -> TokenType.KContinue;
            case "return" -> TokenType.KReturn;
            case "breakif" -> TokenType.KBreakif;
            case "continueif" -> TokenType.KContinueif;
            case "returnif" -> TokenType.KReturnif;
            case "assert!" -> TokenType.KAssertB;
            case "assert" -> TokenType.KAssert;
            case "this" -> TokenType.KThis;
            case "This" -> TokenType.KCThis;
            case "native" -> TokenType.KNative;
            case "pub" -> TokenType.KPub;
            case "priv" -> TokenType.KPriv;
            case "final" -> TokenType.KFinal;
            case "fn" -> TokenType.KFn;
            case "class" -> TokenType.KClass;
            case "primitive" -> TokenType.KPrimitive;
            case "enum" -> TokenType.KEnum;
            case "intef" -> TokenType.KInterface;
            case "impl" -> TokenType.KImplement;
            case "for" -> TokenType.KFor;
            case "cast" -> TokenType.KCast;
            case "new" -> TokenType.KNew;
            case "is" -> TokenType.KIs;
            case "void" -> TokenType.KVoid;
            case "auto" -> TokenType.KAuto;
            case "byte" -> TokenType.KByte;
            case "short" -> TokenType.KShort;
            case "int" -> TokenType.KInt;
            case "long" -> TokenType.KLong;
            case "ubyte" -> TokenType.KUbyte;
            case "ushort" -> TokenType.KUshort;
            case "uint" -> TokenType.KUint;
            case "ulong" -> TokenType.KUlong;
            case "float" -> TokenType.KFloat;
            case "double" -> TokenType.KDouble;
            case "null" -> TokenType.KNull;
            case "true" -> TokenType.KTrue;
            case "false" -> TokenType.KFalse;
            default -> TokenType.Identifier;
        };
    }

    private String parseEscapeSequence() throws IOException, LexerException {
        if (cc != '\\') {
            char c = cc;
            advance();
            return String.valueOf(c);
        }
        String s = String.valueOf(cc);
        advance();
        if ("ntbrf\"`\\".indexOf(cc) > -1) {
            s += cc;
            advance();
            return s;
        }
        if (cc == '0') {
            advanceEOS();
            return parseBasedNumber(true);
        }
        if ("123456789".indexOf(cc) > -1) {
            return parseSimpleNumber();
        }
        throw new UnkownEscapeSequenceException("unknown escape sequence: \"" + cc + "\".", SOURCE, tok);
    }

    private String parseBasedNumber(boolean doLimit) throws IOException, LexerException {
        String s = String.valueOf(cc);
        String chars;
        int limit;
        if (cc == 'x') {
            chars = "0123456789abcdefABCDEF";
            limit = 2;
        } else if (cc == 'o') {
            chars = "01234567";
            limit = 3;
        } else if (cc == 'b') {
            chars = "01";
            limit = 8;
        } else {
            throw new UnkownEscapeSequenceException("0x, 0o, or 0b expected, got unknown \"" + cc + "\".", SOURCE, tok);
        }
        advanceEOS();
        for (int i = 0; (i < limit || !doLimit) && !eos; i++) {
            if (chars.indexOf(cc) < 0)
                break;
            s += cc;
            advanceEOS();
        }
        return s;
    }

    private String parseSimpleNumber() throws IOException, LexerException {
        String num = "";
        while ("0123456789".indexOf(cc) > -1) {
            num += cc;
            advanceEOS();
        }
        return num;
    }
}

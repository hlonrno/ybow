package src.lexer;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import src.lexer.token.*;

public class Lexer {
    private static final HashMap<String, Boolean> keywords = new HashMap<>();
    private static final ArrayList<String> operators = new ArrayList<>();
    private static final String SPECIAL_CHARS = "()[]{}.,+-*/%&|^~!:=;<>@";
    private static final String WHITESPACE_CHARS = " \n\t\r\b\f";
    private static final String IDENTIFIER_CHARS = SPECIAL_CHARS + WHITESPACE_CHARS + "\"`";
    private Reader stream;
    private int column = 0;
    private int line = 1;
    private boolean eof = false;
    private char cc;

    public Lexer(Reader stream) throws IOException {
        this.stream = stream;
        advance();
    }

    private void advance() throws IOException {
        int c = stream.read();
        if (c == -1) {
            eof = true;
            this.cc = 0;
            return;
        }
        if (cc == '\n') {
            line += 1;
            column = 0;
        }
        ++column;
        this.cc = (char)c;
    }

    public Optional<Token> getNextToken() throws LexerException, IOException {
        if (eof) return Optional.empty();

        while (!eof) {
            if (WHITESPACE_CHARS.indexOf(cc) > -1) {
                advance();
                continue;
            }

            Token tok = new Token();
            tok.column = column;
            tok.line = line;
            if (cc == '`') {
                parseCharacterLiteral(tok);
                tok = new CharacterToken(tok);
            } else if (cc == '"') {
                parseStringLiteral(tok);
                tok = new StringToken(tok);
            } else if ("0123456789".indexOf(cc) > -1) {
                parseNumberLiteral(tok);
                tok = new NumberToken(tok);
            } else if (SPECIAL_CHARS.indexOf(cc) > -1) {
                parseSpecialCharacter(tok);
                if (tok.value.equals("//")) {
                    while (!(cc == '\n' || eof)) {
                        tok.value += cc;
                        advance();
                        if (tok.value.endsWith("//") && tok.value.length() > 3)
                            break;
                    }
                    tok = new CommentToken(tok);
                }
            } else {
                parseIdentifier(tok);
            }
            return Optional.of(tok);
        }
        return Optional.empty();
    }

    private String parseStringEscapeSequence() throws StringParsingException, IOException {
        if (cc != '\\')
            return String.valueOf(cc);

        advance();
        StringBuilder str = new StringBuilder('\\');
        str.append('\\');
        if ("ntbrfs\\\"\n".indexOf(cc) > -1) {
            str.append(cc);
            return str.toString();
        }
        if ("0123456789".indexOf(cc) > -1) {
            str.append(cc);
            String chars = "0123456789";
            int maxcount = 3;
            advance();
            for (int i = 0; i < maxcount && chars.indexOf(cc) > -1; i++) {
                str.append(cc);
                advance();
            }
            if (!eof) str.append(cc);
            return str.toString();
        }
        throw new StringParsingException("Unknown escape sequence.");
    }

    public static String literalizeString(String str) {
        if (str.charAt(0) == '\n')
            str.indent(str.lastIndexOf('\n') - str.length() + 1);

        StringBuilder literal = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) != '\\') {
                literal.append(str.charAt(i));
                continue;
            }

            i++;
            char c = switch (str.charAt(i)) {
                case 'n' -> '\n';
                case 't' -> '\t';
                case 'b' -> '\b';
                case 'r' -> '\r';
                case 'f' -> '\f';
                case 's' -> ' ';
                case '\\' -> '\\';
                case '"' -> '"';
                case '\n' -> '\n';
                default -> 0;
            };
            if (c != 0) {
                literal.append(c);
                continue;
            }

            String chars = "0123456789";
            int chr = 0;
            for (int j = 0; j < 3 && i < str.length(); j++, i++) {
                if (chars.indexOf(str.charAt(i)) < 0) {
                    literal.append(str.charAt(i));
                    break;
                }
                int v = str.charAt(i) - '0';
                chr *= 10;
                chr += v;
            }
            literal.append((char)(chr & 255));
        }

        return literal.toString();
    }

    private Token parseCharacterLiteral(Token tok) throws StringParsingException, IOException {
        tok.type = TokenType.CharacterLiteral;
        advance();
        if (cc == '\n')
            throw new StringParsingException("Illegal character.");

        tok.value = parseStringEscapeSequence();
        advance();
        return tok;
    }

    private Token parseStringLiteral(Token tok) throws StringParsingException, IOException {
        tok.type = TokenType.StringLiteral;
        advance();

        StringBuilder str = new StringBuilder();
        while (!(cc == '"' || eof)) {
            str.append(parseStringEscapeSequence());
            advance();
        }

        advance();
        tok.value = str.toString();
        return tok;
    }

    private Token parseNumberLiteral(Token tok) throws IOException {
        tok.type = TokenType.NumberLiteral;

        byte dotCount = 0;
        StringBuilder str = new StringBuilder();
        while ("0123456789.".indexOf(cc) > -1) {
            if (cc == '.') {
                dotCount++;
                if (dotCount > 1)
                    break;
            }
            str.append(cc);
            advance();
        }

        tok.value = str.toString();
        return tok;
    }

    private Token parseSpecialCharacter(Token tok)
        throws UnknownOperatorException, IOException
    {
        tok.type = TokenType.SpecialChar;
        tok.value = String.valueOf(cc);

        while (true) {
            boolean isValid = false;
            for (var op : operators) {
                isValid = op.indexOf(tok.value) == 0;
                if (isValid) break;
            }
            if (!isValid) break;
            advance();
            tok.value += cc;
        }

        tok.value = tok.value.substring(0, tok.value.length() - 1);
        if (tok.value.length() == 0)
            throw new UnknownOperatorException("Unknown operator: '" + tok.value + "'");
        return tok;
    }

    private Token parseIdentifier(Token tok) throws IOException {
        tok.type = TokenType.Identifier;

        StringBuilder str = new StringBuilder();
        while (!(IDENTIFIER_CHARS.indexOf(cc) > -1 || eof)) {
            str.append(cc);
            advance();
        }

        tok.value = str.toString();
        if (Lexer.keywords.getOrDefault(tok.value, false)) {
            tok.type = TokenType.Keyword;
            // NOTE: parse assert!
            if (tok.value.equals("assert") && cc == '!') {
                tok.value += cc;
                advance();
            }
        }
        return tok;
    }

    static {
        keywords.put("switch", true);
        keywords.put("if", true);
        keywords.put("else", true);
        keywords.put("loop", true);
        keywords.put("break", true);
        keywords.put("breakif", true);
        keywords.put("continue", true);
        keywords.put("continueif", true);
        keywords.put("return", true);

        // assert!
        keywords.put("assert", true);
        keywords.put("new", true);
        keywords.put("this", true);
        keywords.put("This", true);
        // native "addi" add(a: i32, b: i32) i32;
        // // "math" -> "libmath.so"/"math.dll"
        // native "addf" : "math" (C) add(a: f32, b: f32) f32;
        keywords.put("native", true);

        keywords.put("pub", true);
        keywords.put("priv", true);
        keywords.put("final", true);
        keywords.put("struct", true);
        keywords.put("class", true);
        keywords.put("enum", true);
        keywords.put("interface", true);
        keywords.put("implement", true);
        keywords.put("for", true);

        keywords.put("cast", true);
        keywords.put("is", true);   // instanceof
        keywords.put("void", true);
        keywords.put("byte", true);
        keywords.put("short", true);
        keywords.put("int", true);
        keywords.put("long", true);
        keywords.put("ubyte", true);
        keywords.put("ushort", true);
        keywords.put("uint", true);
        keywords.put("ulong", true);
        keywords.put("float", true);
        keywords.put("double", true);

        keywords.put("null", true);
        keywords.put("true", true);
        keywords.put("false", true);

        // comm
        operators.add("@");
        operators.add("{");
        operators.add("}");
        operators.add("(");
        operators.add(")");
        operators.add("[");
        operators.add("]");
        operators.add(".");
        operators.add(",");
        operators.add(";");
        operators.add("->");
        // arith
        operators.add("+");
        operators.add("-");
        operators.add("*");
        operators.add("**"); // Math.pow
        operators.add("/");
        // compare
        operators.add("==");
        operators.add("!=");
        operators.add("<");
        operators.add(">");
        operators.add("<=");
        operators.add(">=");
        // bitwise
        operators.add("~"); // not
        operators.add("&"); // and
        operators.add("|"); // or
        operators.add("^"); // xor
        operators.add("~&"); // nand
        operators.add("~|"); // nor
        operators.add("~^"); // xnor
        operators.add("<<"); // left shift
        operators.add(">>"); // right shift
        operators.add("|<"); // left roll
        operators.add("|>"); // right roll
        operators.add("<<<"); // left shift, w/o sign
        operators.add(">>>"); // right shift, w/o sign
        operators.add("|<<"); // left roll, w/o sign
        operators.add("|>>"); // right roll, w/o sign
        // "bool"
        operators.add("!");  // not
        operators.add("&&"); // and
        operators.add("||"); // or
        operators.add("^^"); // xor
        operators.add("!&"); // nand
        operators.add("!|"); // nor
        operators.add("!^"); // xnor
        // inc, dec
        operators.add("++");
        operators.add("--");

        // assign
        operators.add(":");
        operators.add("=");
        // assign/arith
        operators.add("+=");
        operators.add("-=");
        operators.add("*=");
        operators.add("**=");
        operators.add("/=");
        // assign/bitwise
        operators.add("&=");
        operators.add("|=");
        operators.add("^=");
        operators.add("~&=");
        operators.add("~|=");
        operators.add("~^=");
        operators.add("<<=");
        operators.add(">>=");
        operators.add("|<=");
        operators.add("|>=");
        operators.add("<<<=");
        operators.add(">>>=");
        operators.add("|<<=");
        operators.add("|>>=");
        // assign/"bool"
        operators.add("&&=");
        operators.add("||=");
        operators.add("^^=");
        operators.add("!&=");
        operators.add("!|=");
        operators.add("!^=");

        // for comment parsing
        operators.add("//");

        operators.sort(String::compareTo);
    }
}

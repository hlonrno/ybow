package src.parser;

import java.io.Reader;
import java.util.ArrayList;

import src.Result;
import src.lexer.Lexer;
import src.lexer.Token;
import src.lexer.TokenType;
import src.lexer.UnreachableError;
import src.lexer.exception.UnexpectedException;

import static src.parser.Ast.*;

public class Parser {
    private static final Trie trie = new Trie();
    private Lexer lexer;
    private Token[] lookahead;
    private Token ctok;

    public Parser(final Reader reader, final String source) throws Exception {
        this(new Lexer(reader, source));
    }

    public Parser(final Lexer lexer) throws Exception {
        this.lexer = lexer;
        lookahead = new Token[5];
        for (int i = 0; i < lookahead.length; i++)
            lookahead[i] = parseToken();
        ctok = lookahead[0];
    }

    private Token parseToken() throws Exception {
        Token tok = null;
        while (lexer.hasNext()) {
            var res = lexer.next();
            if (res.hasError())
                throw res.getError();
            if (res.hasValue())
                tok = res.getValue();
            else // It'd be funny if this actually happens
                throw new UnexpectedException("Missing token, but no error provided.", lexer.SOURCE, ctok);
            if (tok.getType() != TokenType.Comment)
                break;
        }
        return tok;
    }

    private void advance() throws Exception {
        advanceEOS();
        if (ctok == null)
            throw new UnexpectedException("Unexpected EOS.", lexer.SOURCE, ctok);
    }

    private void advanceEOS() throws Exception {
        System.arraycopy(lookahead, 1, lookahead, 0, lookahead.length - 1);
        ctok = lookahead[0];
        lookahead[lookahead.length - 1] = parseToken();
    }

    private void expect(TokenType type) throws Exception {
        if (ctok.getType() == type)
            return;
        // eg.: unexpected token (expected LIdentifier).
        throw new UnexpectedException("unexpected token (expected " + type + ").", lexer.SOURCE, ctok);
    }

    private void expect(TokenType... type) throws Exception {
        for (int i = 0; i < type.length; i++)
            if (ctok.getType() == type[i])
                return;
        String types = "";
        for (int i = 0; i < type.length; i++)
            types += type[i] + ", ";
        if (type.length > 0)
            types = types.substring(0, types.length() - 2);
        // eg.: unexpected token (expected LIdentifier, SColon).
        throw new UnexpectedException("unexpected token (expected " + types + ").", lexer.SOURCE, ctok);
    }

    public Result<Program, Exception> parseProgram() {
        try {
            var body = new ArrayList<Stmt>();
            while (ctok != null) {
                body.add(parseStmt());
            }
            return Result.ofValue(new Program(lexer.SOURCE, toArray(body)));
        } catch (Exception e) {
            return Result.ofError(e);
        }
    }

    private Stmt parseStmt() throws Exception {
        Stmt stmt = switch (ctok.getType()) {
            case SOpenCurlyBracket -> parseBlock();
            case KSwitch -> parseSwitch();
            case KIf -> parseIf();
            case KLoop -> parseLoop();
            case KBreak -> parseBreak();
            case KContinue -> parseContinue();
            case KReturn -> parseReturn();
            case KBreakif -> parseBreakif();
            case KContinueif -> parseContinueif();
            case KReturnif -> parseReturnif();
            case KAssertB -> parseAssertB();
            case KAssert -> parseAssert();
            case KClass -> parseClass();
            case KFn -> parseFn();
            case KPrimitive -> prasePrimitive();
            case KEnum -> parseEnum();
            case KInterface -> parseInterface();
            case KImplement -> parseImplement();
            case SSemiColon -> new EmptyStmt();
            case Identifier, Comment, LString, LCharacter, LNumber, SOpenBracket,
                 SClosedBracket, SOpenSquareBracket, SClosedSquareBracket,
                 SClosedCurlyBracket, SDot, SComma, SPlus, SMinus, SStar, SSlash,
                 SPercent, SAnd, SPipe, SArrow, STilde, SBang, SColon, SEquals,
                 SLess, SGreater, SMonkeyA, KCase, KElse, KThis, KCThis, KNative,
                 KPub, KPriv, KFinal, KFor, KCast, KNew, KIs, KVoid, KAuto,
                 KByte, KShort, KInt, KLong, KUbyte, KUshort, KUint, KUlong,
                 KFloat, KDouble, KNull, KTrue, KFalse -> parseExprStmt();
        };
        advanceEOS();

        return stmt;
    }

    private Block parseBlock() throws Exception {
        advance();
        ArrayList<Stmt> body = new ArrayList<>();
        while (ctok.getType() != TokenType.SClosedCurlyBracket) {
            body.add(parseStmt());
        }
        return new Block(toArray(body));
    }

    private Stmt parseSwitch() throws Exception {
        throw new Exception("Not Implemented."); // TODO: not implamented.
    }
    private Stmt parseIf() throws Exception {
        throw new Exception("Not Implemented."); // TODO: not implamented.
    }
    private Stmt parseLoop() throws Exception {
        throw new Exception("Not Implemented."); // TODO: not implamented.
    }
    private Stmt parseBreak() throws Exception {
        throw new Exception("Not Implemented."); // TODO: not implamented.
    }
    private Stmt parseContinue() throws Exception {
        throw new Exception("Not Implemented."); // TODO: not implamented.
    }
    private Stmt parseReturn() throws Exception {
        throw new Exception("Not Implemented."); // TODO: not implamented.
    }
    private Stmt parseBreakif() throws Exception {
        throw new Exception("Not Implemented."); // TODO: not implamented.
    }
    private Stmt parseContinueif() throws Exception {
        throw new Exception("Not Implemented."); // TODO: not implamented.
    }
    private Stmt parseReturnif() throws Exception {
        throw new Exception("Not Implemented."); // TODO: not implamented.
    }
    private Stmt parseAssertB() throws Exception {
        throw new Exception("Not Implemented."); // TODO: not implamented.
    }
    private Stmt parseAssert() throws Exception {
        throw new Exception("Not Implemented."); // TODO: not implamented.
    }
    private Stmt parseClass() throws Exception {
        throw new Exception("Not Implemented."); // TODO: not implamented.
    }
    private Stmt parseFn() throws Exception {
        throw new Exception("Not Implemented."); // TODO: not implamented.
    }
    private Stmt prasePrimitive() throws Exception {
        throw new Exception("Not Implemented."); // TODO: not implamented.
    }
    private Stmt parseEnum() throws Exception {
        throw new Exception("Not Implemented."); // TODO: not implamented.
    }
    private Stmt parseInterface() throws Exception {
        throw new Exception("Not Implemented."); // TODO: not implamented.
    }
    private Stmt parseImplement() throws Exception {
        throw new Exception("Not Implemented."); // TODO: not implamented.
    }
    private Stmt parseExprStmt() throws Exception {
        return new ExprStmt(parseExpr());
    }

    private Expr parseExpr() throws Exception {
        throw new Exception("Not Implemented."); // TODO: not implamented.
    }

    /** @return null if list.size() == 0 else (T[])list.toArray().*/
    public static <T> T[] toArray(ArrayList<T> list) {
        if (list.size() == 0)
            return null;
        @SuppressWarnings("unchecked")
        T[] arr = (T[])list.toArray();
        return arr;
    }
    {
        // AST.OpType
        String[] operators = new String[] { 
            "@", ",", ";", "{", "}", "->", ":", "=",
            "||=", "!||=", "&&=", "!&&=", "^^=", "!^^=",
            "**=", "*=", "/=", "+=", "-=", "|=",
            "~|=", "&=", "~&=", "^=", "~^=",
            "<<=", ">>=", "|<<=", "|>>=",
            "<<<=", ">>>=", "|<<<=", "|>>>=",
            "(", ")", ".", "::", "[", "]",
            "++", "--", "!", "~", // "is",
            "==", "!=", "<=", ">=", "<", ">",
            "||", "!||", "&&", "!&&", "^^", "!^^",
            "**", "*", "/", "+", "-",
            "|", "~|", "&", "~&", "^", "~^",
            "<<", ">>", "|<<", "|>>",
            "<<<", ">>>", "|<<<", "|>>>"
        };
        for (var op : operators)
            trie.add(op);
    }
}

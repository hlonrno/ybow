package src.parser;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

import src.Result;
import src.lexer.Lexer;
import src.lexer.Token;
import src.lexer.TokenType;
import src.lexer.UnreachableError;
import src.lexer.exception.UnexpectedException;

import static src.parser.Node.*;

public class Parser {
    private static final Trie trie = new Trie();
    private static final HashMap<String, Op> compoundMap = new HashMap<>();
    private static String SYMBOLS = "";
    private Lexer lexer;
    private Token[] lookahead;
    private Token ctok;

    private static interface ExprParser {
        public Expr parse(Parser parser) throws Exception;
    }

    private static enum OpMap {
        // parseSimpleExpr
        // parseMethodOrArrayExpr
        // parsePostfixExpr
        Root((p) -> p.parsePrefixExpr()),
        Declare   (Root,       Map.of(":", Op.Declare)),
        Assign    (Declare,    Map.of(":=", Op.DeclAssign, "=", Op.Assign,
                                      "<=>", Op.Swap)),
        CompAssign(Assign,     compoundMap),
        Member    (CompAssign, Map.of(".", Op.Member)),
        Compare   (Member,     Map.of("==", Op.Eq, "!=", Op.Ne, "<=", Op.Le,
                                      ">=", Op.Ge, "<", Op.Lt, ">", Op.Gt)),
        And       (Compare,    Map.of("&&", Op.And, "!&&", Op.Nand)),
        Xor       (And,        Map.of("^^", Op.Xor, "!^^", Op.Xnor)),
        Or        (Xor,        Map.of("||", Op.Or, "!||", Op.Nor)),
        Exp       (Or,         Map.of("**", Op.Pow)),
        Mult      (Exp,        Map.of("*", Op.Mul, "/", Op.Div)),
        Additive  (Mult,       Map.of("+", Op.Add, "-", Op.Sub)),
        Bitwise   (Additive,   Map.of("<<", Op.LeftShift, ">>", Op.RightShift,
                                      "<<<", Op.LeftRoll, ">>>", Op.RightRoll)),
        BwAnd     (Bitwise,    Map.of("&", Op.BwAnd, "~&", Op.BwNand)),
        BwXor     (BwAnd,      Map.of("^", Op.BwXor, "~^", Op.BwXnor)),
        BwOr      (BwXor,      Map.of("|", Op.BwOr, "~|", Op.BwNor));

        final ExprParser fn;
        final Map<String, Op> map;

        private OpMap(OpMap next, Map<String, Op> map) {
            fn = (p) -> Parser.parseBinOpExpr(
                p,
                next.fn,
                map
            );
            this.map = map;
        }

        private OpMap(ExprParser next) {
            fn = next;
            this.map = Map.of();
        }
    }

    public Parser(final Reader reader, final String source) throws Exception {
        this(new Lexer(reader, source));
    }

    public Parser(final Lexer lexer) throws Exception {
        this.lexer = lexer;
        lookahead = new Token[5];
        for (int i = 0; i < lookahead.length; i++) {
            lookahead[i] = parseToken();
            if (lookahead[i] == null)
                continue;
            lookahead[i] = lookahead[i].cloneToken();
        }
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
            if (tok == null || tok.getType() != TokenType.Comment)
                break;
        }
        return tok;
    }

    private Token advanceTok() throws Exception {
        Token tok = ctok;
        advance();
        return tok;
    }

    private void advance() throws Exception {
        advanceEOS();
        if (ctok == null)
            throw new UnexpectedException("unexpected EOS.", lexer.SOURCE, null);
    }

    private void advanceEOS() throws Exception {
        System.arraycopy(lookahead, 1, lookahead, 0, lookahead.length - 1);
        ctok = lookahead[0];
        Token tok = parseToken();
        lookahead[lookahead.length - 1] = tok == null ? null : tok.cloneToken();
    }

    private void expect(TokenType type) throws Exception {
        if (ctok == null)
            throw new UnexpectedException("unexpected EOS (expected " + type + ").", lexer.SOURCE, null);
        if (ctok.getType() == type)
            return;
        // eg.: unexpected token (expected LIdentifier).
        throw new UnexpectedException("unexpected token (expected " + type + ").", lexer.SOURCE, ctok);
    }

    private void expect(TokenType... type) throws Exception {
        if (ctok == null)
            throw new UnexpectedException("unexpected EOS (expected " + type + ").", lexer.SOURCE, null);
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
            return Result.ofValue(new Program(lexer.SOURCE, body.toArray(new Stmt[0])));
        } catch (Exception e) {
            return Result.ofError(e);
        }
    }

    private Stmt parseStmt() throws Exception {
        return switch (ctok.getType()) {
            case SOpenCurlyBracket -> parseBlock();
            case KSwitch -> parseSwitch();
            case KIf -> parseIf();
            case KLoop -> parseLoop();
            case KLoopWhile -> parseLoopWhile();
            case KBreak -> parseBreak();
            case KContinue -> parseContinue();
            case KReturn -> parseReturn();
            case KBreakif -> parseBreakif();
            case KContinueif -> parseContinueif();
            case KReturnif -> parseReturnif();
            case KAssert -> parseAssert();
            case KClass -> parseClass();
            case KFn -> parseFn();
            case KPrimitive -> prasePrimitive();
            case KEnum -> parseEnum();
            case KInterface -> parseInterface();
            case KImplement -> parseImplement();
            case SSemicolon -> {
                advanceEOS();
                yield new EmptyStmt();
            }
            default -> parseExprStmt();
        };
    }

    private BlockStmt parseBlock() throws Exception {
        advance();
        ArrayList<Stmt> body = new ArrayList<>();
        while (ctok.getType() != TokenType.SClosedCurlyBracket) {
            body.add(parseStmt());
        }
        advanceEOS();
        return new BlockStmt(body.toArray(new Stmt[0]));
    }

    private Stmt parseSwitch() throws Exception {
        throw new Exception("Not Implemented."); // TODO: Not Implemented.
    }

    private Stmt parseIf() throws Exception {
        throw new Exception("Not Implemented."); // TODO: Not Implemented.
    }

    private Stmt parseLoop() throws Exception {
        throw new Exception("Not Implemented."); // TODO: Not Implemented.
    }

    private Stmt parseLoopWhile() throws Exception {
        throw new Exception("Not Implemented."); // TODO: Not Implemented.
    }

    private Stmt parseBreak() throws Exception {
        throw new Exception("Not Implemented."); // TODO: Not Implemented.
    }

    private Stmt parseContinue() throws Exception {
        throw new Exception("Not Implemented."); // TODO: Not Implemented.
    }

    private Stmt parseReturn() throws Exception {
        throw new Exception("Not Implemented."); // TODO: Not Implemented.
    }

    private Stmt parseBreakif() throws Exception {
        throw new Exception("Not Implemented."); // TODO: Not Implemented.
    }

    private Stmt parseContinueif() throws Exception {
        throw new Exception("Not Implemented."); // TODO: Not Implemented.
    }

    private Stmt parseReturnif() throws Exception {
        throw new Exception("Not Implemented."); // TODO: Not Implemented.
    }

    private Stmt parseAssert() throws Exception {
        throw new Exception("Not Implemented."); // TODO: Not Implemented.
    }

    private Stmt parseClass() throws Exception {
        throw new Exception("Not Implemented."); // TODO: Not Implemented.
    }

    private Stmt parseFn() throws Exception {
        throw new Exception("Not Implemented."); // TODO: Not Implemented.
    }

    private Stmt prasePrimitive() throws Exception {
        throw new Exception("Not Implemented."); // TODO: Not Implemented.
    }

    private Stmt parseEnum() throws Exception {
        throw new Exception("Not Implemented."); // TODO: Not Implemented.
    }

    private Stmt parseInterface() throws Exception {
        throw new Exception("Not Implemented."); // TODO: Not Implemented.
    }

    private Stmt parseImplement() throws Exception {
        throw new Exception("Not Implemented."); // TODO: Not Implemented.
    }

    private Stmt parseExprStmt() throws Exception {
        Stmt stmt = new ExprStmt(parseExpr());
        expect(TokenType.SSemicolon);
        advanceEOS();
        return stmt;
    }

    private Expr parseExpr() throws Exception {
        return Parser.parseBinOpExpr(
            this,
            OpMap.BwOr.fn,
            OpMap.BwOr.map
        );
    }

    private static Expr parseBinOpExpr(Parser p, ExprParser next, Map<String, Op> operators) throws Exception {
        Expr left = next.parse(p);
        while (true) {
            Op op = operators.get(p.getOperatorNoAdvance());
            if (op == null)
                return left;
            p.getOperator();
            left = new BinOpExpr(op, left, next.parse(p));
        }
    }

    private Expr parsePrefixExpr() throws Exception {
        ArrayList<Op> ops = new ArrayList<>();
        while (true) {
            Op op = switch (getOperatorNoAdvance()) {
                case "" -> null;
                case "+" -> Op.Pass;
                case "-" -> Op.Negative;
                case "!" -> Op.Not;
                case "~" -> Op.BwNot;
                case "++" -> Op.PreInc;
                case "--" -> Op.PreDec;
                default -> throw new UnexpectedException("unexpected operator.", lexer.SOURCE, ctok);
            };
            if (op == null)
                break;
            ops.add(op);
            getOperator();
        }
        Expr expr = parsePostfixExpr();
        for (int i = 0; i < ops.size(); i++)
            expr = new UnaryOpExpr(ops.get(i), expr);
        return expr;
    }

    private Expr parsePostfixExpr() throws Exception {
        Expr expr = parseMethodOrArrayExpr();
        while (true) {
            Op op = switch (getOperatorNoAdvance()) {
                case "++" -> Op.PostInc;
                case "--" -> Op.PostDec;
                case "!" -> Op.Error;
                default -> null;
            };
            if (op == null)
                break;
            getOperator();
            expr = new UnaryOpExpr(op, expr);
        }
        return expr;
    }

    private Expr parseMethodOrArrayExpr() throws Exception {
        Expr left = parseSimpleExpr();
        while (true) {
            if (ctok.getType() == TokenType.SOpenBracket) {
                advance();
                ArrayList<Expr> list = new ArrayList<>();
                while (ctok.getType() != TokenType.SClosedBracket) {
                    list.add(parseExpr());
                    if (ctok.getType() == TokenType.SComma) {
                        advance();
                    } else {
                        break;
                    }
                }
                expect(TokenType.SClosedBracket);
                advance();
                left = new MethodCallExpr(left, list.toArray(new Expr[0]));
            } else if (ctok.getType() == TokenType.SOpenSquareBracket) {
                advance();
                left = new BinOpExpr(Op.ArrIndex, left, parseExpr());
                expect(TokenType.SClosedSquareBracket);
                advance();
            } else {
                break;
            }
        }
        return left;
    }

    private Expr parseSimpleExpr() throws Exception {
        return switch (ctok.getType()) {
            case Identifier, KCast, KClass, SQuestion -> new IdentifierL(advanceTok());
            case LString -> new StringL(ctok, parseStringLiteral(advanceTok()));
            case LCharacter -> new CharL(ctok, parseCharacterLiteral(advanceTok()));
            case LNumber -> new NumberL(advanceTok());
            case SOpenBracket -> {
                advance();
                Expr e = parseExpr();
                expect(TokenType.SClosedBracket);
                advance();
                yield e;
            }
            case SOpenSquareBracket -> parseArrayL();
            case SOpenCurlyBracket -> parseBlockExpr();
            case SMonkeyA -> {
                advance();
                yield parseTypeExpr();
            }
            case KSwitch -> parseSwitchExpr();
            case KIf -> parseIfExpr();
            case KLoop -> parseLoopExpr();
            case KLoopWhile -> parseLoopWhileExpr();
            case KBreak -> parseBreakExpr();
            case KContinue -> parseContinueExpr();
            case KReturn -> parseReturnExpr();
            case KBreakif -> parseBreakifExpr();
            case KContinueif -> parseContinueifExpr();
            case KReturnif -> parseReturnifExpr();
            case KThis -> new IdentifierL(advanceTok());
            case KCThis -> new IdentifierL(advanceTok());
            case KFn -> parseFnExprOrType();
            case KFinal, KVoid, KAuto, KByte, KShort, KInt, KLong, KUbyte, KUshort,
                 KUint, KUlong, KFloat, KDouble -> parseTypeExpr();
            case KNull -> new NullL(advanceTok());
            case KTrue -> new BoolL(ctok, advanceTok().getType() == TokenType.KTrue);
            case KFalse -> new BoolL(ctok, advanceTok().getType() == TokenType.KTrue);
            case Comment -> throw new UnreachableError();
            default -> throw new UnexpectedException("unexpected token.", lexer.SOURCE, ctok);
        };
    }

    private Expr parseArrayL() throws Exception {
        ArrayList<Expr> list = new ArrayList<>();
        advance(); // [ //
        while (ctok.getType() != TokenType.SClosedSquareBracket)
            list.add(parseExpr());
        advance(); // ] //
        return new ArrayL(list.toArray(new Expr[0]));
    }

    private Expr parseBlockExpr() throws Exception {
        throw new Exception("Not Implemented."); // TODO: Not Implemented.
    }

    private Expr parseSwitchExpr() throws Exception {
        throw new Exception("Not Implemented."); // TODO: Not Implemented.
    }

    private Expr parseIfExpr() throws Exception {
        throw new Exception("Not Implemented."); // TODO: Not Implemented.
    }

    private Expr parseLoopExpr() throws Exception {
        throw new Exception("Not Implemented."); // TODO: Not Implemented.
    }

    private Expr parseLoopWhileExpr() throws Exception {
        throw new Exception("Not Implemented."); // TODO: Not Implemented.
    }

    private Expr parseBreakExpr() throws Exception {
        throw new Exception("Not Implemented."); // TODO: Not Implemented.
    }

    private Expr parseContinueExpr() throws Exception {
        throw new Exception("Not Implemented."); // TODO: Not Implemented.
    }

    private Expr parseReturnExpr() throws Exception {
        throw new Exception("Not Implemented."); // TODO: Not Implemented.
    }

    private Expr parseBreakifExpr() throws Exception {
        throw new Exception("Not Implemented."); // TODO: Not Implemented.
    }

    private Expr parseContinueifExpr() throws Exception {
        throw new Exception("Not Implemented."); // TODO: Not Implemented.
    }

    private Expr parseReturnifExpr() throws Exception {
        throw new Exception("Not Implemented."); // TODO: Not Implemented.
    }

    private Expr parseFnExprOrType() throws Exception {
        advance();
        if (ctok.getType() == TokenType.SColon) {
            Arg[] args = parseArgList();
            TypeExpr type = parseTypeExpr();
            return new FnExpr(args, type, parseStmt());
        }
        expect(TokenType.SOpenBracket);
        Arg[] args = parseArgList();
        return new FnT(args, parseTypeExpr());
    }

    private TypeExpr parseTypeExpr() throws Exception {
        throw new Exception("Not Implemented."); // TODO: Not Implemented.
    }

    private String parseStringLiteral(Token tok) throws Exception {
        throw new Exception("Not Implemented."); // TODO: Not Implemented.
    }

    private char parseCharacterLiteral(Token tok) throws Exception {
        throw new Exception("Not Implemented."); // TODO: Not Implemented.
    }

    private Arg[] parseArgList() throws Exception {
        ArrayList<Arg> list = new ArrayList<>();
        advance(); // ( //
        IdentifierL name = new IdentifierL(advanceTok());
        expect(TokenType.SColon);
        advance(); // : //
        list.add(new Arg(name, parseTypeExpr()));
        expect(TokenType.SClosedBracket);
        advance(); // ) //
        return list.toArray(new Arg[0]);
    }

    private String getOperator() throws Exception {
        String s = getOperatorNoAdvance();
        for (int i = 0; i < s.length(); i++)
            advance();
        return s;
    }

    private String getOperatorNoAdvance() throws Exception {
        String s = "";
        int i = 0; 
        for (; i < lookahead.length - 1; i++) {
            if (lookahead[i] == null || SYMBOLS.indexOf(lookahead[i].getImage()) < 0)
                break;
            s += lookahead[i].getImage();
            if (!trie.partialMatch(s))
                break;
        }
        if (s.isEmpty())
            return s;
        while (!trie.match(s) && s.length() > 0)
            s = s.substring(0, s.length() - 1);
        return s;
    }

    {
        String[] operators = new String[] { 
            ":", ".", "=", "<=>", ":=",
            "||=", "!||=", "&&=", "!&&=", "^^=", "!^^=",
            "**=", "*=", "/=", "+=", "-=", "|=",
            "~|=", "&=", "~&=", "^=", "~^=",
            "<<=", ">>=", "<<<=", ">>>=",
            "++", "--", "!", "~",
            "==", "!=", "<=", ">=", "<", ">",
            "||", "!||", "&&", "!&&", "^^", "!^^",
            "**", "*", "/", "+", "-",
            "|", "~|", "&", "~&", "^", "~^",
            "<<", ">>", "<<<", ">>>",
        };
        for (var op : operators) {
            trie.add(op);
            for (int i = 0; i < op.length(); i++)
                if (SYMBOLS.indexOf(op.charAt(i)) < 0)
                    SYMBOLS += op.charAt(i);
        }

        compoundMap.put("<=>", Op.Swap);
        compoundMap.put("||=", Op.OrAssign);
        compoundMap.put("!||=", Op.NorAssign);
        compoundMap.put("&&=", Op.AndAssign);
        compoundMap.put("!&&=", Op.NandAssign);
        compoundMap.put("^^=", Op.XorAssign);
        compoundMap.put("!^^=", Op.XnorAssign);
        compoundMap.put("**=", Op.PowAssign);
        compoundMap.put("*=", Op.MulAssign);
        compoundMap.put("/=", Op.DivAssign);
        compoundMap.put("+=", Op.AddAssign);
        compoundMap.put("-=", Op.SubAssign);
        compoundMap.put("|=", Op.BwOrAssign);
        compoundMap.put("~|=", Op.BwNorAssign);
        compoundMap.put("&=", Op.BwAndAssign);
        compoundMap.put("~&=", Op.BwNandAssign);
        compoundMap.put("^=", Op.BwXorAssign);
        compoundMap.put("~^=", Op.BwXnorAssign);
        compoundMap.put("<<=", Op.LeftShiftAssign);
        compoundMap.put(">>=", Op.RightShiftAssign);
        compoundMap.put("<<<=", Op.LeftRollAssign);
        compoundMap.put(">>>=", Op.RightRollAssign);
    }
}

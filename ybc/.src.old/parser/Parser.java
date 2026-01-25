package src.parser;

import java.util.ArrayList;
import java.util.Optional;

import java.io.IOException;
import java.rmi.UnexpectedException;

import src.lexer.Lexer;
import src.lexer.LexerException;
import src.lexer.token.*;

import src.parser.ast.*;
import src.parser.ast.constructs.*;
import src.parser.ast.expr.*;
import src.parser.ast.literal.*;
import src.parser.ast.stmt.*;
import src.parser.ast.type.*;

public class Parser {
    private Lexer lexer;
    private Token ctok;
    private Token ptok;

    public Parser(Lexer lexer) throws LexerException, IOException {
        this.lexer = lexer;
        advance();
    }

    private void advance() throws LexerException, IOException {
        do {
            ptok = ctok;
            Optional<Token> ntok = lexer.getNextToken();
            if (ntok.isPresent())
                ctok = ntok.get();
            else
                ctok = null;
        } while (ctok != null && ctok.type == TokenType.Comment);
    }

    private Token advanceReturnPrevious() throws LexerException, IOException {
        Token tok = ctok;
        advance();
        ptok = tok;
        return tok;
    }

    private void expectedSpecial(String value) throws LexerException, IOException, UnexpectedException {
        expected(TokenType.SpecialChar, value);
    }

    private void expected(TokenType type)
            throws LexerException, IOException, UnexpectedException
    {
        if (ctok == null)
            throw new UnexpectedException(
                    String.format("expected type %s, got eof. [p: %s]",
                        type, ptok));
        if (ctok.type != type)
            throw new UnexpectedException(
                    String.format("expected type %s, got %s. [%s]",
                        type, ctok.type, ctok));
    }

    private void expected(TokenType type, String value) throws LexerException, IOException, UnexpectedException {
        if (ctok == null)
            throw new UnexpectedException(
                    String.format("expected value '%s' type %s, got eof. [p: %s]",
                        value, type.toString(), ptok));
        if (ctok.type != type)
            throw new UnexpectedException(
                    String.format("expected value '%s' type %s, got %s. [%s]",
                        value, type, ctok.type, ctok));
        if (!ctok.value.equals(value))
            throw new UnexpectedException(
                    String.format("expected value '%s', got '%s'. [%s]",
                        value, ctok.value, type, ctok));
    }

    public Node getAST() throws Exception {
        BlockExpr program = new BlockExpr();
        program.body = new ArrayList<>();
        while (ctok != null) {
            program.body.add(parseStatement());
            if (program.body.getLast() == null) {
                program.body.removeLast();
                return program;
            }
        }
        return program;
    }

    private Statement parseStatement() throws Exception {
        return switch (ctok.type) {
            case Keyword -> switch (ctok.value) {
                case "struct"     -> parseStruct(); 
                case "class"      -> parseClass(); 
                case "enum"       -> parseEnum(); 
                case "interface"  -> parseInterface(); 
                case "implement"  -> parseImplement(); 
                case "continue"   -> parseContinue();
                case "continueif" -> parseContinueIf();
                case "return"     -> parseReturn();
                default -> parseExpressionAsStatement();
            };
            default -> parseExpressionAsStatement();
        };
    }

    private Visibility parseVisibility() throws Exception {
        if (ctok.type == TokenType.Keyword)
            return switch (ctok.value) {
                case "pub"  -> { advance(); yield Visibility.Pub; }
                case "priv" -> { advance(); yield Visibility.Priv; }
                default     -> Visibility.Prot;
            };
        return Visibility.Prot;
    }

    private TypeExpr parseType() throws Exception {
        TypeExpr type = switch (ctok.type) {
            case Identifier, Keyword -> {
                if (ctok.value.equals("final")) {
                    advance(); // final
                    yield new FinalType(parseType());
                }
                yield new BaseType(new IdentifierLiteral(advanceReturnPrevious()));
            }
            default -> null;
        };

        return continueTypeParsing(type);
    }

    private TypeExpr continueTypeParsing(TypeExpr child) throws Exception {
        boolean run = true;
        while (run && ctok.type == TokenType.SpecialChar)
            child = switch (ctok.value) {
                case "*" -> {
                    advance();
                    yield new FreeType(child);
                }
                case "<" -> parseGenericType(child);
                case "[" -> parseArrayType(child);
                case "++" -> { advance(); yield new ExtendType(child, parseType()); }
                default -> {
                    run = false;
                    yield child;
                }
            };
        return child;
    }

    private GenericType parseGenericType(TypeExpr child) throws Exception {
        GenericType type = new GenericType(child);
        type.generics = new ArrayList<>();
        advance();
        do {
            type.generics.add(parseType());
        } while (ctok.value == ",");
        expectedSpecial(">");
        advance();

        return type;
    }

    private ArrayType parseArrayType(TypeExpr child) throws Exception {
        ArrayType type = new ArrayType();
        type.child = child;
        type.size = null;
        advance(); // [
        while (!ctok.value.equals("]")) {
            if (ctok.type == TokenType.NumberLiteral) {
                if (type.size != null) {
                    ArrayType newType = new ArrayType();
                    newType.child = type;
                    type = newType;
                }
                type.size = new NumberLiteral((NumberToken)advanceReturnPrevious());
            }
        }
        advance();

        return type;
    }

    private ArrayList<Pair<IdentifierLiteral, TypeExpr>> parseMethodArgs() throws Exception {
        var list = new ArrayList<Pair<IdentifierLiteral, TypeExpr>>();
        advance(); // (
        while (!ctok.value.equals(")")) {
            list.add(new Pair<>());
            expected(TokenType.Identifier);
            list.getLast().a = new IdentifierLiteral(advanceReturnPrevious());
            expectedSpecial(":");
            advance(); // :
            list.getLast().b = parseType();
            if (ctok.value.equals(",")) {
                advance(); // ,
                continue;
            }
            expectedSpecial(")");
            break;
        }
        advance(); // )
        return list;
    }

    private void parseNativeModifierFor(ClassMethod method) throws Exception {
        advance(); // native
        method.isNative = true;
        if (ctok.value.equals("(")) {
            advance(); // (
            method.abi = "";
            while (!ctok.value.equals(")")) {
                method.abi += advanceReturnPrevious().value;
            }
            advance(); // )
        }
        if (ctok.type == TokenType.StringLiteral) {
            expected(TokenType.StringLiteral);
            method.nativeName = new StringLiteral((StringToken)advanceReturnPrevious());
        }
        if (ctok.value.equals(":")) {
            advance(); // :
            expected(TokenType.StringLiteral);
            method.nativeLib = new StringLiteral((StringToken)advanceReturnPrevious());
        }
    }

    private StructStmt parseStruct() throws Exception {
        var stmt = new StructStmt();
        advance(); // struct
        stmt.visibility = parseVisibility();
        stmt.name = parseType();
        expectedSpecial("{");
        advance(); // {
        stmt.fields = new ArrayList<>();
        while (!ctok.value.equals("}")) {
            expectedSpecial(".");
            advance(); // .
            Field field = new Field();
            expected(TokenType.Identifier);
            field.name = new IdentifierLiteral(advanceReturnPrevious());
            if (ctok.value.equals(":")) {
                advance(); // :
                if (!ctok.value.equals("=")) {
                    field.type = parseType();
                }
            }
            if (ctok.value.equals("=")) {
                advance(); // =
                field.init = parseExpression();
            }
            expectedSpecial(";");
            advance();
            stmt.fields.add(field);
        }
        advance(); // }
        return stmt;
    }

    // TODO: fix this stupid, main.yb:12 breaks it
    private ClassMethod parseClassMethod() throws Exception {
        ClassMethod method = new ClassMethod();
        method.isStatic = true;
        if (ctok.value.equals(".")) {
            advance(); // .
            method.isStatic = false;
        }
        method.visibility = parseVisibility();
        method.isFinal = false;
        if (ctok.value.equals("final")) {
            advance(); // final
            method.isFinal = true;
        }
        method.isNative = false;
        if (ctok.value.equals("native")) {
            parseNativeModifierFor(method);
        }
        if (ctok.type == TokenType.Identifier) {
            method.name = new IdentifierLiteral(advanceReturnPrevious());
        } else {
            expectedSpecial("(");
            ctok.type = TokenType.StringLiteral;
            ctok.value = "<init>";
            method.name = new IdentifierLiteral(ctok);
        }
        method.args = parseMethodArgs();
        method.returnType = parseType();
        if (!method.isNative)
            method.exec = parseExpression();
        if (!(method.exec instanceof BlockExpr)) {
            expectedSpecial(";");
            advance(); // ;
        }
        return method;
    }

    private ClassStmt parseClass() throws Exception {
        var stmt = new ClassStmt();
        advance(); // class
        stmt.visibility = parseVisibility();
        stmt.isFinal = false;
        if (ctok.value.equals("final")) {
            advance();
            stmt.isFinal = true;
        }
        stmt.name = parseType();
        stmt.fields = new ArrayList<>();
        stmt.methods = new ArrayList<>();
        advance(); // {
        while (!ctok.value.equals("}")) {
            ClassField field = new ClassField();
            field.isStatic = true;
            if (ctok.value.equals(".")) {
                advance();
                field.isStatic = false;
            }
            field.visibility = parseVisibility();
            if (!(ctok.type == TokenType.Identifier || ctok.type == TokenType.Keyword))
                throw new UnexpectedException(
                        String.format("expected Identifier or Keyword, got %s value '%s'. [p: %s]",
                            ctok.type, ctok.value, ctok));
            field.name = new IdentifierLiteral(advanceReturnPrevious());
            if (ctok.value.equals(":")) {
                advance(); // :
                field.type = parseType();
            }
            if (ctok.value.equals("=")) {
                advance(); // =
                field.init = parseExpression();
            }
            expectedSpecial(";");
            advance(); // ;
            stmt.fields.add(field);
        }
        advance(); // }

        advance(); // {
        while (!ctok.value.equals("}")) {
            stmt.methods.add(parseClassMethod());
        }
        advance(); // }
        return stmt;
    }


    private EnumStmt parseEnum() throws Exception {
        var stmt = new EnumStmt();
        advance(); // enum
        stmt.visibility = parseVisibility();
        stmt.name = parseType();
        if (ctok.value.equals("(")) {
            advance(); // (
            stmt.fieldType = parseType();
            expectedSpecial(")");
            advance(); // )
        }
        advance(); // {
        stmt.fields = new ArrayList<>();
        while (!ctok.value.equals("}")) {
            Field field = new Field();
            expected(TokenType.Identifier);
            field.name = new IdentifierLiteral(advanceReturnPrevious());
            field.type = stmt.fieldType;
            stmt.fields.add(field);
            if (!ctok.value.equals("="))
                continue;
            advance(); // =
            field.init = parseExpression();
            expectedSpecial(";");
            advance(); // ;
        }
        advance(); // }
        return stmt;
    }

    private InterfaceStmt parseInterface() throws Exception {
        var stmt = new InterfaceStmt();
        advance(); // interface
        stmt.visibility = parseVisibility();
        stmt.name = parseType();
        stmt.methods = new ArrayList<>();
        expectedSpecial("{");
        advance(); // { //
        while (!ctok.value.equals("}")) {
            Method method = new Method();
            method.isStatic = false;
            if (ctok.value.equals(".")) {
                advance(); // . //
                method.isStatic = true;
            }
            if (!(ctok.type == TokenType.Identifier || ctok.type == TokenType.Keyword))
                throw new UnexpectedException(
                        String.format("expected Identifier or Keyword, got %s value '%s'. [%s]",
                            ctok.type, ctok.value, ctok));
            method.name = new IdentifierLiteral(advanceReturnPrevious());
            expectedSpecial("(");
            method.args = parseMethodArgs();
            method.returnType = parseType();
            expectedSpecial(";");
            advance(); // ;
            stmt.methods.add(method);
        }
        advance(); // } //
        return stmt;
    }

    private ImplementStmt parseImplement() throws Exception {
        var stmt = new ImplementStmt();
        advance(); // implement
        stmt.name = parseType();
        expected(TokenType.Keyword, "for");
        advance();
        stmt.interf = parseType();
        stmt.methods = new ArrayList<>();
        expectedSpecial("{");
        advance(); // { //
        while (!ctok.value.equals("}")) {
            stmt.methods.add(parseClassMethod());
        }
        advance(); // } //
        return stmt;
    }

    private ContinueIfStmt parseContinue() throws Exception {
        advance(); // continue
        Expression expr = parseExpression();
        expectedSpecial(";");
        advance(); // ;
        return new ContinueIfStmt(null, expr);
    }

    private ContinueIfStmt parseContinueIf() throws Exception {
        advance(); // continueif
        expectedSpecial("(");
        advance(); // (
        Expression cond = null;
        if (!ctok.value.equals(")"))
            cond = parseExpression();
        expectedSpecial(")");
        advance(); // )
        Expression expr = parseExpression();
        expectedSpecial(";");
        advance(); // ;
        return new ContinueIfStmt(cond, expr);
    }

    private ReturnStmt parseReturn() throws Exception {
        advance(); // return
        return new ReturnStmt(parseExpression());
    }

    private Statement parseExpressionAsStatement() throws Exception {
        Expression expr = parseExpression();
        if (ctok != null && ctok.value.equals(";")) {
            advance();
        } else
            return new ImplicitReturnStmt(expr);
        return expr;
    }

    private IfExpr parseIfExpr() throws Exception {
        Expression cond, iftrue, iffalse = null;
        advance(); // if
        expectedSpecial("(");
        advance(); // (
        cond = parseExpression();
        expectedSpecial(")");
        advance(); // )
        iftrue = parseExpression();
        if (ctok.value.equals("else")) {
            advance(); // else
            iffalse = parseExpression();
        }
        return new IfExpr(cond, iftrue, iffalse);
    }

    // TODO: parseSwitchExpr
    private SwitchExpr parseSwitchExpr() throws Exception {
        // public Expression match;
        // public ArrayList<Pair<Pair<OperationType, ArrayList<Expression>>, Expression>> cases;
        return null;
    }

    private LoopExpr parseLoopExpr() throws Exception {
        Expression cond = null, body;
        advance(); // loop
        if (ctok.value.equals("(")) {
            advance();
            cond = parseExpression();
            advance(); // )
        }
        body = parseExpression();
        return new LoopExpr(cond, body);
    }

    private BlockExpr parseBlockExpr() throws Exception {
        BlockExpr block = new BlockExpr();
        block.body = new ArrayList<>();
        advance(); // {
        while (!ctok.value.equals("}")) {
            block.body.add(parseStatement());
        }
        advance(); // }
        return block;
    }

    private ArrayLiteral parseArrayLiteral() throws Exception {
        var value = new ArrayList<Expression>();
        advance(); // [
        while (!ctok.value.equals("]")) {
            value.add(parseExpression());
        }
        advance(); // ]
        return new ArrayLiteral(value);
    }

    private Expression advanceParseExpression() throws Exception {
        advance(); // 
        return parseExpression();
    }

    private MethodExpr parseMethodExpr() throws Exception {
        var expr = new MethodExpr();
        expr.args = new ArrayList<>();
        advance(); // (
        while (!ctok.value.equals(")")) {
            expr.args.add(parseExpression());
            if (ctok.value.equals(","))
                advance();
            else
                expectedSpecial(")");
        }
        advance(); // )
        return expr;
    }

    private Expression parseExpression() throws Exception {
        return parseExpressionWith(null);
    }

    private Expression parseExpressionWith(Expression node) throws Exception {
        if (ctok == null)
            return null;
        if (node == null)
            node = switch (ctok.type) {
                case CharacterLiteral -> new NumberLiteral(new NumberToken(new Token(
                                null, "" + (int)((CharacterToken)advanceReturnPrevious()).literal)));
                case StringLiteral -> new StringLiteral((StringToken)advanceReturnPrevious());
                case NumberLiteral -> new NumberLiteral((NumberToken)advanceReturnPrevious());
                case Identifier -> new IdentifierLiteral(advanceReturnPrevious());
                case SpecialChar -> switch (ctok.value) {
                    case "{" -> parseBlockExpr();
                    case "@" -> {
                        advance();
                        yield parseType();
                    }
                    case "[" -> parseArrayLiteral();
                    case "~" -> new OpExpr(OperationType.BwNot, advanceParseExpression());
                    case "!" -> new OpExpr(OperationType.Not, advanceParseExpression());
                    case "++" -> new OpExpr(OperationType.PreInc, advanceParseExpression());
                    case "--" -> new OpExpr(OperationType.PreDec, advanceParseExpression());
                    case "(" -> {
                        advance(); // ( //
                        Expression op = parseExpression();
                        advance(); // ) // 
                        yield op;
                    }
                    default -> null;
                };
                case Keyword -> switch (ctok.value) {
                    case "if"     -> parseIfExpr();
                    case "switch" -> parseSwitchExpr();
                    case "loop"   -> parseLoopExpr();
                    case "null",
                         "false" -> {
                        advance(); // null / false
                        yield new NumberLiteral(new NumberToken(new Token(null, "0")));
                    }
                    case "true" -> {
                        advance(); // true
                        yield new NumberLiteral(new NumberToken(new Token(null, "1")));
                    }
                    case "this",
                         "This" -> new IdentifierLiteral(advanceReturnPrevious());
                    case "void",
                         "byte",
                         "short",
                         "int",
                         "long",
                         "ubyte",
                         "ushort",
                         "uint",
                         "ulong",
                         "float",
                         "double",
                         "final" -> parseType();
                    default -> null;
                };
                case Comment -> {
                    System.err.println("switch statement caught a comment token, this should not happen.");
                    advance();
                    yield null;
                }
            };

        if (node == null || ctok.value.equals(";"))
            return node;
        boolean run = true;
        while (run) {
            if (ctok.type == TokenType.SpecialChar)
                node = switch (ctok.value) {
                    case "(" -> new BinOpExpr(OperationType.Call, node, parseMethodExpr());
                    case "[" -> {
                        advance(); // [
                        var expr = new BinOpExpr(OperationType.ArrayGet, node, parseExpression());
                        expectedSpecial("]");
                        advance(); // ]
                        yield expr;
                    }
                    case "." -> {
                        advance(); // . //
                        if (ctok.type == TokenType.Keyword || ctok.type == TokenType.Identifier)
                            yield new BinOpExpr(OperationType.MemberOf, node,
                                new IdentifierLiteral(advanceReturnPrevious()));
                        throw new UnexpectedException("expected keyword or identifier, got " + ctok.type + ". [" + ctok + "]");
                    }
                    // arith
                    case "+" -> new BinOpExpr(OperationType.Add, node, advanceParseExpression());
                    case "-" -> new BinOpExpr(OperationType.Sub, node, advanceParseExpression());
                    case "*" -> new BinOpExpr(OperationType.Mul, node, advanceParseExpression());
                    case "**" -> new BinOpExpr(OperationType.Pow, node, advanceParseExpression());
                    case "/" -> new BinOpExpr(OperationType.Div, node, advanceParseExpression());
                    // compare
                    case "==" -> new BinOpExpr(OperationType.Eq, node, advanceParseExpression());
                    case "!=" -> new BinOpExpr(OperationType.NEq, node, advanceParseExpression());
                    case "<" -> new BinOpExpr(OperationType.Lt, node, advanceParseExpression());
                    case ">" -> new BinOpExpr(OperationType.Gt, node, advanceParseExpression());
                    case "<=" -> new BinOpExpr(OperationType.LtEq, node, advanceParseExpression());
                    case ">=" -> new BinOpExpr(OperationType.GtEq, node, advanceParseExpression());
                    // bitwise
                    case "&" -> new BinOpExpr(OperationType.BwAnd, node, advanceParseExpression());
                    case "|" -> new BinOpExpr(OperationType.BwOr, node, advanceParseExpression());
                    case "^" -> new BinOpExpr(OperationType.BwXor, node, advanceParseExpression());
                    case "~&" -> new BinOpExpr(OperationType.BwNand, node, advanceParseExpression());
                    case "~|" -> new BinOpExpr(OperationType.BwNor, node, advanceParseExpression());
                    case "~^" -> new BinOpExpr(OperationType.BwXnor, node, advanceParseExpression());
                    case "<<" -> new BinOpExpr(OperationType.LShift, node, advanceParseExpression());
                    case ">>" -> new BinOpExpr(OperationType.RShift, node, advanceParseExpression());
                    case "|<" -> new BinOpExpr(OperationType.LRoll, node, advanceParseExpression());
                    case "|>" -> new BinOpExpr(OperationType.RRoll, node, advanceParseExpression());
                    case "<<<" -> new BinOpExpr(OperationType.LShiftSignless, node, advanceParseExpression());
                    case ">>>" -> new BinOpExpr(OperationType.RShiftSignless, node, advanceParseExpression());
                    case "|<<" -> new BinOpExpr(OperationType.LRollSignless, node, advanceParseExpression());
                    case "|>>" -> new BinOpExpr(OperationType.RRollSignless, node, advanceParseExpression());
                    // "bool"
                    case "&&" -> new BinOpExpr(OperationType.And, node, advanceParseExpression());
                    case "||" -> new BinOpExpr(OperationType.Or, node, advanceParseExpression());
                    case "^^" -> new BinOpExpr(OperationType.Xor, node, advanceParseExpression());
                    case "!&" -> new BinOpExpr(OperationType.Nand, node, advanceParseExpression());
                    case "!|" -> new BinOpExpr(OperationType.Nor, node, advanceParseExpression());
                    case "!^" -> new BinOpExpr(OperationType.Xnor, node, advanceParseExpression());
                    // inc/dec
                    case "++", "--" -> throw new UnsupportedOperationException("Post increment and decrement are not supported.");
                    // assign
                    case ":" -> {
                        advance(); // :
                        if (ctok.type != TokenType.Keyword && ctok.type != TokenType.Identifier) {
                            var expr = new OpExpr(OperationType.VarDec, node);
                            yield parseExpressionWith(expr);
                        }
                        yield new BinOpExpr(OperationType.VarDec, node, parseType());
                    }
                    case "=" -> new BinOpExpr(OperationType.VarDef, node, advanceParseExpression());
                    // assign/arith
                    case "+=" -> new BinOpExpr(OperationType.VarDefAdd, node, advanceParseExpression());
                    case "-=" -> new BinOpExpr(OperationType.VarDefSub, node, advanceParseExpression());
                    case "*=" -> new BinOpExpr(OperationType.VarDefMul, node, advanceParseExpression());
                    case "**=" -> new BinOpExpr(OperationType.VarDefPow, node, advanceParseExpression());
                    case "/=" -> new BinOpExpr(OperationType.VarDefDiv, node, advanceParseExpression());
                    // assign/bitwise
                    case "&=" -> new BinOpExpr(OperationType.VarDefBwAnd, node, advanceParseExpression());
                    case "|=" -> new BinOpExpr(OperationType.VarDefBwOr, node, advanceParseExpression());
                    case "^=" -> new BinOpExpr(OperationType.VarDefBwXor, node, advanceParseExpression());
                    case "~&=" -> new BinOpExpr(OperationType.VarDefBwNand, node, advanceParseExpression());
                    case "~|=" -> new BinOpExpr(OperationType.VarDefBwNor, node, advanceParseExpression());
                    case "~^=" -> new BinOpExpr(OperationType.VarDefBwXnor, node, advanceParseExpression());
                    case "<<=" -> new BinOpExpr(OperationType.VarDefLShift, node, advanceParseExpression());
                    case ">>=" -> new BinOpExpr(OperationType.VarDefRShift, node, advanceParseExpression());
                    case "|<=" -> new BinOpExpr(OperationType.VarDefLRoll, node, advanceParseExpression());
                    case "|>=" -> new BinOpExpr(OperationType.VarDefRRoll, node, advanceParseExpression());
                    case "<<<=" -> new BinOpExpr(OperationType.VarDefLShiftSignless, node, advanceParseExpression());
                    case ">>>=" -> new BinOpExpr(OperationType.VarDefRShiftSignless, node, advanceParseExpression());
                    case "|<<=" -> new BinOpExpr(OperationType.VarDefLRollSignless, node, advanceParseExpression());
                    case "|>>=" -> new BinOpExpr(OperationType.VarDefRRollSignless, node, advanceParseExpression());
                    // assign/"bool"
                    case "&&=" -> new BinOpExpr(OperationType.VarDefAnd, node, advanceParseExpression());
                    case "||=" -> new BinOpExpr(OperationType.VarDefOr, node, advanceParseExpression());
                    case "^^=" -> new BinOpExpr(OperationType.VarDefXor, node, advanceParseExpression());
                    case "!&=" -> new BinOpExpr(OperationType.VarDefNand, node, advanceParseExpression());
                    case "!|=" -> new BinOpExpr(OperationType.VarDefNor, node, advanceParseExpression());
                    case "!^=" -> new BinOpExpr(OperationType.VarDefXnor, node, advanceParseExpression());
                    default -> {
                        run = false;
                        yield node;
                    }
                };
            else if (ctok.type == TokenType.Keyword)
                node = switch (ctok.value) {
                    case "is" -> new BinOpExpr(OperationType.Is, node, advanceParseExpression());
                    default -> {
                        run = false;
                        yield node;
                    }
                };
            else
                run = false;
        }
        return node;
    }
}

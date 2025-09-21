package com.zergatul.scripting.tests.codegen;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.MultiLineTextRange;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.lexer.Trivia;
import com.zergatul.scripting.parser.ParserOutput;
import com.zergatul.scripting.parser.nodes.*;

import java.util.List;

public class ParserDumper {

    private final StringBuilder sb = new StringBuilder();

    public String dump(ParserOutput output) {
        sb.setLength(0);
        dump(output.unit());
        return sb.toString();
    }

    private void dump(ParserNode node) {
        if (node == null) {
            sb.append("null");
            return;
        }

        switch (node.getNodeType()) {
            case COMPILATION_UNIT -> dump((CompilationUnitNode) node);
            case FUNCTION -> dump((FunctionNode) node);
            case MODIFIERS -> dump((ModifiersNode) node);
            case PARAMETER_LIST -> dump((ParameterListNode) node);
            case VARIABLE_DECLARATION -> dump((VariableDeclarationNode) node);
            case BLOCK_STATEMENT -> dump((BlockStatementNode) node);
            case FOR_LOOP_STATEMENT -> dump((ForLoopStatementNode) node);
            case FOREACH_LOOP_STATEMENT -> dump((ForEachLoopStatementNode) node);
            case WHILE_LOOP_STATEMENT -> dump((WhileLoopStatementNode) node);
            case IF_STATEMENT -> dump((IfStatementNode) node);
            case RETURN_STATEMENT -> dump((ReturnStatementNode) node);
            case EXPRESSION_STATEMENT -> dump((ExpressionStatementNode) node);
            case INVALID_STATEMENT -> dump((InvalidStatementNode) node);
            case INVOCATION_EXPRESSION -> dump((InvocationExpressionNode) node);
            case ARGUMENTS_LIST -> dump((ArgumentsListNode) node);
            case BINARY_EXPRESSION -> dump((BinaryExpressionNode) node);
            case MEMBER_ACCESS_EXPRESSION -> dump((MemberAccessExpressionNode) node);
            case ARRAY_CREATION_EXPRESSION -> dump((ArrayCreationExpressionNode) node);
            case ARRAY_INITIALIZER_EXPRESSION -> dump((ArrayInitializerExpressionNode) node);
            case OBJECT_CREATION_EXPRESSION -> dump((ObjectCreationExpressionNode) node);
            case INTEGER_LITERAL -> dump((IntegerLiteralExpressionNode) node);
            case BOOLEAN_LITERAL -> dump((BooleanLiteralExpressionNode) node);
            case NAME_EXPRESSION -> dump((NameExpressionNode) node);
            case INVALID_EXPRESSION -> dump((InvalidExpressionNode) node);
            case LET_TYPE -> dump((LetTypeNode) node);
            case ARRAY_TYPE -> dump((ArrayTypeNode) node);
            case CUSTOM_TYPE -> dump((CustomTypeNode) node);
            case PREDEFINED_TYPE -> dump((PredefinedTypeNode) node);
            case VOID_TYPE -> dump((VoidTypeNode) node);
            case INVALID_TYPE -> dump((InvalidTypeNode) node);
            case BINARY_OPERATOR -> dump((BinaryOperatorNode) node);
            default -> throw new InternalException(node.getClass().getName());
        }
    }

    private void dump(CompilationUnitNode node) {
        sb.append("new CompilationUnitNode(");
        dump(node.members);
        sb.append(", ");
        dump(node.statements);
        sb.append(", ");
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(CompilationUnitMembersListNode node) {
        sb.append("new CompilationUnitMembersListNode(");
        dumpList(node.members);
        sb.append(", ");
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(StatementsListNode node) {
        sb.append("new StatementsListNode(");
        dumpList(node.statements);
        sb.append(", ");
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(FunctionNode node) {
        sb.append("new FunctionNode(");
        dump(node.modifiers);
        sb.append(", ");
        dump(node.returnType);
        sb.append(", ");
        dump(node.name);
        sb.append(", ");
        dump(node.parameters);
        sb.append(", ");
        dump(node.body);
        sb.append(", ");
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(ModifiersNode node) {
        sb.append("new ModifiersNode(");
        dumpTokens(node.tokens);
        sb.append(", ");
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(ParameterListNode node) {
        sb.append("new ParameterListNode(");
        dump(node.openParen);
        sb.append(", ");
        dumpList(node.parameters);
        sb.append(", ");
        dump(node.closeParen);
        sb.append(", ");
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(BlockStatementNode node) {
        sb.append("new BlockStatementNode(");
        dumpList(node.statements);
        sb.append(", ");
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(ForLoopStatementNode node) {
        sb.append("new ForLoopStatementNode(");
        dump(node.openParenthesis);
        sb.append(", ");
        dump(node.init);
        sb.append(", ");
        dump(node.condition);
        sb.append(", ");
        dump(node.update);
        sb.append(", ");
        dump(node.closeParenthesis);
        sb.append(", ");
        dump(node.body);
        sb.append(", ");
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(ForEachLoopStatementNode node) {
        sb.append("new ForEachLoopStatementNode(");
        dump(node.openParen);
        sb.append(", ");
        dump(node.typeNode);
        sb.append(", ");
        dump(node.name);
        sb.append(", ");
        dump(node.iterable);
        sb.append(", ");
        dump(node.closeParen);
        sb.append(", ");
        dump(node.body);
        sb.append(", ");
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(WhileLoopStatementNode node) {
        sb.append("new WhileLoopStatementNode(");
        dump(node.keyword);
        sb.append(", ");
        dump(node.openParen);
        sb.append(", ");
        dump(node.condition);
        sb.append(", ");
        dump(node.closeParen);
        sb.append(", ");
        dump(node.body);
        sb.append(", ");
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(IfStatementNode node) {
        sb.append("new IfStatementNode(");
        dump(node.ifToken);
        sb.append(", ");
        dump(node.openParen);
        sb.append(", ");
        dump(node.closeParen);
        sb.append(", ");
        dump(node.condition);
        sb.append(", ");
        dump(node.thenStatement);
        sb.append(", ");
        dump(node.elseToken);
        sb.append(", ");
        dump(node.elseStatement);
        sb.append(", ");
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(ReturnStatementNode node) {
        sb.append("new ReturnStatementNode(");
        dump(node.keyword);
        sb.append(", ");
        dump(node.expression);
        sb.append(", ");
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(ExpressionStatementNode node) {
        sb.append("new ExpressionStatementNode(");
        dump(node.expression);
        sb.append(",");
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(VariableDeclarationNode node) {
        sb.append("new VariableDeclarationNode(");
        dump(node.type);
        sb.append(", ");
        dump(node.name);
        sb.append(", ");

        if (node.expression != null) {
            dump(node.expression);
            sb.append(", ");
        } else {
            sb.append("null, ");
        }

        if (node.semicolon != null) {
            dump(node.semicolon);
            sb.append(", ");
        } else {
            sb.append("null, ");
        }

        dump(node.getRange());
        sb.append(")");
    }

    private void dump(InvalidStatementNode node) {
        sb.append("new InvalidStatementNode(");
        dump(node.getRange());
        sb.append(")");
    }

    public void dump(InvocationExpressionNode node) {
        sb.append("new InvocationExpressionNode(");
        dump(node.callee);
        sb.append(", ");
        dump(node.arguments);
        sb.append(", ");
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(BinaryExpressionNode node) {
        sb.append("new BinaryExpressionNode(");
        dump(node.left);
        sb.append(", ");
        dump(node.operator);
        sb.append(", ");
        dump(node.right);
        sb.append(", ");
        dump(node.getRange());
        sb.append(")");
    }

    public void dump(MemberAccessExpressionNode node) {
        sb.append("new MemberAccessExpressionNode(");
        dump(node.callee);
        sb.append(", ");
        dump(node.dot);
        sb.append(", ");
        dump(node.name);
        sb.append(", ");
        dump(node.getRange());
        sb.append(")");
    }

    public void dump(ArrayCreationExpressionNode node) {
        sb.append("new ArrayCreationExpressionNode(");
        dump(node.typeNode);
        sb.append(", ");
        dump(node.lengthExpression);
        sb.append(", ");
        dump(node.getRange());
        sb.append(")");
    }

    public void dump(ArrayInitializerExpressionNode node) {
        sb.append("new ArrayInitializerExpressionNode(");
        dump(node.typeNode);
        sb.append(", ");
        dumpList(node.items);
        sb.append(", ");
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(ObjectCreationExpressionNode node) {
        sb.append("new ObjectCreationExpressionNode(");
        dump(node.typeNode);
        sb.append(", ");
        dump(node.arguments);
        sb.append(", ");
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(IntegerLiteralExpressionNode node) {
        sb.append("new IntegerLiteralExpressionNode(\"");
        sb.append(node.value);
        sb.append("\", ");
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(BooleanLiteralExpressionNode node) {
        sb.append("new BooleanLiteralExpressionNode(");
        sb.append(node.value);
        sb.append(", ");
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(NameExpressionNode node) {
        sb.append("new NameExpressionNode(\"");
        sb.append(node.value);
        sb.append("\", ");
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(InvalidExpressionNode node) {
        sb.append("new InvalidExpressionNode(");
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(ArgumentsListNode node) {
        sb.append("new ArgumentsListNode(");
        dumpList(node.arguments);
        sb.append(", ");
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(LetTypeNode node) {
        sb.append("new LetTypeNode(");
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(ArrayTypeNode node) {
        sb.append("new ArrayTypeNode(");
        dump(node.underlying);
        sb.append(", ");
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(CustomTypeNode node) {
        sb.append("new CustomTypeNode(\"");
        sb.append(node.value);
        sb.append("\", ");
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(PredefinedTypeNode node) {
        sb.append("new PredefinedTypeNode(PredefinedType.");
        sb.append(node.type.toString());
        sb.append(", ");
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(VoidTypeNode node) {
        sb.append("new VoidTypeNode(");
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(InvalidTypeNode node) {
        sb.append("new InvalidTypeNode(");
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(BinaryOperatorNode node) {
        sb.append("new BinaryOperatorNode(");
        sb.append(node.operator.name());
        sb.append(", ");
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(Token token) {
        if (token == null) {
            sb.append("null");
        } else if (token.getClass() == Token.class) {
            sb.append("new Token(TokenType.");
            sb.append(token.getTokenType().toString());
            sb.append(", ");
            dump(token.getRange());
            sb.append(")");

            for (var trivia : token.getLeadingTrivia()) {
                sb.append(".withLeadingTrivia(");
                dump(trivia);
                sb.append(")");
            }

            for (var trivia : token.getTrailingTrivia()) {
                sb.append(".withTrailingTrivia(");
                dump(trivia);
                sb.append(")");
            }
        } else {
            throw new InternalException();
        }
    }

    private void dump(Trivia trivia) {
        sb.append("new Trivia(TokenType.");
        sb.append(trivia.getTokenType().toString());
        sb.append(", ");
        dump(trivia.getRange());
        sb.append(")");
    }

    private void dump(TextRange range) {
        if (range instanceof SingleLineTextRange single) {
            sb.append("new SingleLineTextRange(");
            sb.append(single.getLine1());
            sb.append(", ");
            sb.append(single.getColumn1());
            sb.append(", ");
            sb.append(single.getPosition());
            sb.append(", ");
            sb.append(single.getLength());
            sb.append(")");
        } else if (range instanceof MultiLineTextRange multi) {
            sb.append("new MultiLineTextRange(");
            sb.append(multi.getLine1());
            sb.append(", ");
            sb.append(multi.getColumn1());
            sb.append(", ");
            sb.append(multi.getLine2());
            sb.append(", ");
            sb.append(multi.getColumn2());
            sb.append(", ");
            sb.append(multi.getPosition());
            sb.append(", ");
            sb.append(multi.getLength());
            sb.append(")");
        } else {
            throw new InternalException();
        }
    }

    private void dumpTokens(List<Token> tokens) {
        sb.append("List.of(");
        for (int i = 0; i < tokens.size(); i++) {
            dump(tokens.get(i));
            if (i < tokens.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
    }

    private <T extends ParserNode> void dumpList(List<T> nodes) {
        sb.append("List.of(");
        for (int i = 0; i < nodes.size(); i++) {
            dump(nodes.get(i));
            if (i < nodes.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
    }
}
package com.zergatul.scripting.tests.utility;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.MultiLineTextRange;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.lexer.Trivia;
import com.zergatul.scripting.parser.ParserOutput;
import com.zergatul.scripting.parser.nodes.*;

import java.util.List;

public class ParserDumper extends Dumper {

    public String dump(ParserOutput output) {
        reset();
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
            case PARAMETER -> dump((ParameterNode) node);
            case ASSIGNMENT_OPERATOR -> dump((AssignmentOperatorNode) node);
            case ASSIGNMENT_STATEMENT -> dump((AssignmentStatementNode) node);
            default -> throw new InternalException(node.getClass().getName());
        }
    }

    private void dump(ArrayInitializerExpressionNode node) {
        fullLine("new ArrayInitializerExpressionNode(");
        incIndent();
        dump(node.keyword);
        commaBreak();
        dump(node.typeNode);
        commaBreak();
        dump(node.openBrace);
        commaBreak();
        dumpList(node.list);
        commaBreak();
        dump(node.closeBrace);
        commaBreak();
        dump(node.getRange());
        sb.append(")");
        decIndent();
    }

    private void dump(ArrayTypeNode node) {
        fullLine("new ArrayTypeNode(");
        incIndent();
        dump(node.underlying);
        commaBreak();
        dump(node.openBracket);
        commaBreak();
        dump(node.closeBracket);
        commaBreak();
        dump(node.getRange());
        sb.append(")");
        decIndent();
    }

    private void dump(AssignmentOperatorNode node) {
        fullLine("new AssignmentOperatorNode(");
        incIndent();
        dump(node.token);
        commaBreak();
        beginLine("AssignmentOperator." + node.operator.name());
        commaBreak();
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(AssignmentStatementNode node) {
        fullLine("new AssignmentStatementNode(");
        incIndent();
        dump(node.left);
        commaBreak();
        dump(node.operator);
        commaBreak();
        dump(node.right);
        commaBreak();
        dump(node.semicolon);
        commaBreak();
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(BinaryExpressionNode node) {
        fullLine("new BinaryExpressionNode(");
        incIndent();
        dump(node.left);
        commaBreak();
        dump(node.operator);
        commaBreak();
        dump(node.right);
        commaBreak();
        dump(node.getRange());
        sb.append(")");
        decIndent();
    }

    private void dump(BinaryOperatorNode node) {
        beginLine("new BinaryOperatorNode(BinaryOperator.");
        sb.append(node.operator.name());
        sb.append(", ");
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(BlockStatementNode node) {
        if (node.statements.isEmpty()) {
            beginLine("new BlockStatementNode(List.of(), ");
            dump(node.getRange());
            sb.append(")");
        } else {
            fullLine("new BlockStatementNode(");
            dumpList(node.statements);
            commaBreak();
            dump(node.getRange());
            sb.append(")");
        }
    }

    private void dump(CompilationUnitNode node) {
        fullLine("new CompilationUnitNode(");
        incIndent();
        dump(node.members);
        commaBreak();
        dump(node.statements);
        commaBreak();
        dump(node.end);
        sb.append(")");
    }

    private void dump(CompilationUnitMembersListNode node) {
        if (node.members.isEmpty()) {
            beginLine("new CompilationUnitMembersListNode(List.of(), ");
            dump(node.getRange());
            sb.append(")");
        } else {
            beginLine("new CompilationUnitMembersListNode(");
            incIndent();
            dumpList(node.members);
            commaBreak();
            dump(node.getRange());
            sb.append(")");
            decIndent();
        }
    }

    private void dump(FunctionNode node) {
        fullLine("new FunctionNode(");
        incIndent();
        dump(node.modifiers);
        commaBreak();
        dump(node.returnType);
        commaBreak();
        dump(node.name);
        commaBreak();
        dump(node.parameters);
        commaBreak();
        dump(node.body);
        commaBreak();
        dump(node.getRange());
        sb.append(")");
        decIndent();
    }

    private void dump(ModifiersNode node) {
        if (node.tokens.isEmpty()) {
            beginLine("new ModifiersNode(List.of(), ");
            dump(node.getRange());
            sb.append(")");
        } else {
            fullLine("new ModifiersNode(");
            incIndent();
            dumpTokens(node.tokens);
            commaBreak();
            dump(node.getRange());
            sb.append(")");
            decIndent();
        }
    }

    private void dump(ParameterListNode node) {
        fullLine("new ParameterListNode(");
        incIndent();
        dump(node.openParen);
        commaBreak();
        dumpList(node.parameters);
        commaBreak();
        dump(node.closeParen);
        commaBreak();
        beginLine();
        dump(node.getRange());
        sb.append(")");
        decIndent();
    }

    private void dump(ParameterNode node) {
        fullLine("new ParameterNode(");
        incIndent();
        dump(node.getType());
        commaBreak();
        dump(node.getName());
        commaBreak();
        dump(node.getRange());
        sb.append(')');
        decIndent();
    }

    private void dump(StatementsListNode node) {
        if (node.statements.isEmpty()) {
            beginLine("new StatementsListNode(List.of(), ");
            dump(node.getRange());
            sb.append(")");
        } else {
            beginLine("new StatementsListNode(");
            incIndent();
            dumpList(node.statements);
            commaBreak();
            dump(node.getRange());
            sb.append(")");
            decIndent();
        }
    }



    private void dump(ForLoopStatementNode node) {
        sb.append("new ForLoopStatementNode(");
        dump(node.openParen);
        sb.append(", ");
        dump(node.init);
        sb.append(", ");
        dump(node.condition);
        sb.append(", ");
        dump(node.update);
        sb.append(", ");
        dump(node.closeParen);
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
        fullLine("new ExpressionStatementNode(");
        incIndent();
        dump(node.expression);
        commaBreak();
        dump(node.semicolon);
        sb.append(")");
        decIndent();
    }

    private void dump(VariableDeclarationNode node) {
        fullLine("new VariableDeclarationNode(");
        incIndent();
        dump(node.type);
        commaBreak();
        dump(node.name);
        commaBreak();
        dump(node.expression);
        commaBreak();
        dump(node.semicolon);
        commaBreak();
        dump(node.getRange());
        sb.append(")");
        decIndent();
    }

    private void dump(InvalidStatementNode node) {
        sb.append("new InvalidStatementNode(");
        dump(node.getRange());
        sb.append(")");
    }

    public void dump(InvocationExpressionNode node) {
        fullLine("new InvocationExpressionNode(");
        incIndent();
        dump(node.callee);
        commaBreak();
        dump(node.arguments);
        commaBreak();
        dump(node.getRange());
        sb.append(")");
        decIndent();
    }

    public void dump(MemberAccessExpressionNode node) {
        fullLine("new MemberAccessExpressionNode(");
        incIndent();
        dump(node.callee);
        commaBreak();
        dump(node.dot);
        commaBreak();
        dump(node.name);
        sb.append(")");
        decIndent();
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
        beginLine("new IntegerLiteralExpressionNode(\"");
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
        fullLine("new NameExpressionNode(");
        incIndent();
        dump(node.token);
        sb.append(")");
        decIndent();
    }

    private void dump(InvalidExpressionNode node) {
        sb.append("new InvalidExpressionNode(");
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(ArgumentsListNode node) {
        fullLine("new ArgumentsListNode(");
        incIndent();
        dump(node.openParen);
        commaBreak();
        dumpList(node.arguments);
        commaBreak();
        dump(node.closeParen);
        sb.append(")");
    }

    public void dump(ArrayCreationExpressionNode node) {
        sb.append("new ArrayCreationExpressionNode(");
        dump(node.keyword);
        sb.append(", ");
        dump(node.typeNode);
        sb.append(", ");
        dump(node.openBracket);
        sb.append(", ");
        dump(node.lengthExpression);
        sb.append(", ");
        dump(node.closeBracket);
        sb.append(", ");
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(LetTypeNode node) {
        sb.append("new LetTypeNode(");
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
        beginLine("new PredefinedTypeNode(PredefinedType.");
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

    private void dumpTokens(List<Token> tokens) {
        boolean newLine = beginNewLineIfRequired();

        endLine("List.of(");
        if (newLine) incIndent();
        for (int i = 0; i < tokens.size(); i++) {
            dump(tokens.get(i));
            if (i < tokens.size() - 1) {
                commaBreak();
            }
        }
        sb.append(")");
        if (newLine) decIndent();
    }

    private <T extends ParserNode> void dumpList(List<T> nodes) {
        boolean newLine = beginNewLineIfRequired();

        endLine("List.of(");
        if (newLine) incIndent();
        for (int i = 0; i < nodes.size(); i++) {
            dump(nodes.get(i));
            if (i < nodes.size() - 1) {
                commaBreak();
            }
        }
        sb.append(")");
        if (newLine) decIndent();
    }

    private <T extends ParserNode> void dumpList(SeparatedList<T> nodes) {
        if (nodes.size() == 0) {
            beginLine("SeparatedList.of()");
        } else {
            endLine("SeparatedList.of(");
            incIndent();
            for (var item : nodes.getChildNodes()) {
                if (item instanceof ParserNode node) {
                    dump(node);
                    continue;
                }
                if (item instanceof Token token) {
                    dump(token);
                    continue;
                }
                throw new InternalException();
            }
            sb.append(")");
            decIndent();
        }
    }
}
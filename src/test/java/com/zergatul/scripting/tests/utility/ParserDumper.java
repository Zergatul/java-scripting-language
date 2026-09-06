package com.zergatul.scripting.tests.utility;

import com.zergatul.scripting.utility.Lists;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.lexer.Token;
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
            beginNewLineIfRequired();
            sb.append("null");
            return;
        }

        switch (node.getNodeType()) {
            case CLASS_DECLARATION:
                dump((ClassNode) node);
                break;
            case CLASS_METHOD:
                dump((ClassMethodNode) node);
                break;
            case COMPILATION_UNIT:
                dump((CompilationUnitNode) node);
                break;
            case FUNCTION:
                dump((FunctionNode) node);
                break;
            case MODIFIERS:
                dump((ModifiersNode) node);
                break;
            case PARAMETER_LIST:
                dump((ParameterListNode) node);
                break;
            case VARIABLE_DECLARATION:
                dump((VariableDeclarationNode) node);
                break;
            case BLOCK_STATEMENT:
                dump((BlockStatementNode) node);
                break;
            case FOR_LOOP_STATEMENT:
                dump((ForLoopStatementNode) node);
                break;
            case FOREACH_LOOP_STATEMENT:
                dump((ForEachLoopStatementNode) node);
                break;
            case WHILE_LOOP_STATEMENT:
                dump((WhileLoopStatementNode) node);
                break;
            case IF_STATEMENT:
                dump((IfStatementNode) node);
                break;
            case RETURN_STATEMENT:
                dump((ReturnStatementNode) node);
                break;
            case EXPRESSION_STATEMENT:
                dump((ExpressionStatementNode) node);
                break;
            case INVALID_STATEMENT:
                dump((InvalidStatementNode) node);
                break;
            case INVOCATION_EXPRESSION:
                dump((InvocationExpressionNode) node);
                break;
            case ARGUMENTS_LIST:
                dump((ArgumentsListNode) node);
                break;
            case BINARY_EXPRESSION:
                dump((BinaryExpressionNode) node);
                break;
            case MEMBER_ACCESS_EXPRESSION:
                dump((MemberAccessExpressionNode) node);
                break;
            case ARRAY_CREATION_EXPRESSION:
                dump((ArrayCreationExpressionNode) node);
                break;
            case ARRAY_INITIALIZER_EXPRESSION:
                dump((ArrayInitializerExpressionNode) node);
                break;
            case OBJECT_CREATION_EXPRESSION:
                dump((ObjectCreationExpressionNode) node);
                break;
            case INTEGER_LITERAL:
                dump((IntegerLiteralExpressionNode) node);
                break;
            case BOOLEAN_LITERAL:
                dump((BooleanLiteralExpressionNode) node);
                break;
            case NAME_EXPRESSION:
                dump((NameExpressionNode) node);
                break;
            case INVALID_EXPRESSION:
                dump((InvalidExpressionNode) node);
                break;
            case LET_TYPE:
                dump((LetTypeNode) node);
                break;
            case ARRAY_TYPE:
                dump((ArrayTypeNode) node);
                break;
            case CUSTOM_TYPE:
                dump((CustomTypeNode) node);
                break;
            case PREDEFINED_TYPE:
                dump((PredefinedTypeNode) node);
                break;
            case VOID_TYPE:
                dump((VoidTypeNode) node);
                break;
            case INVALID_TYPE:
                dump((InvalidTypeNode) node);
                break;
            case BINARY_OPERATOR:
                dump((BinaryOperatorNode) node);
                break;
            case PARAMETER:
                dump((ParameterNode) node);
                break;
            case ASSIGNMENT_OPERATOR:
                dump((AssignmentOperatorNode) node);
                break;
            case ASSIGNMENT_STATEMENT:
                dump((AssignmentStatementNode) node);
                break;
            case UNARY_EXPRESSION:
                dump((UnaryExpressionNode) node);
                break;
            case META_TYPE_EXPRESSION:
                dump((MetaTypeExpressionNode) node);
                break;
            case META_TYPE_OF_EXPRESSION:
                dump((MetaTypeOfExpressionNode) node);
                break;
            case JAVA_TYPE:
                dump((JavaTypeNode) node);
                break;
            case LAMBDA_EXPRESSION:
                dump((LambdaExpressionNode) node);
                break;
            case IS_EXPRESSION:
                dump((IsExpressionNode) node);
                break;
            case STATIC_REFERENCE:
                dump((StaticReferenceNode) node);
                break;
            case FUNCTION_TYPE:
                dump((FunctionTypeNode) node);
                break;
            default:
                throw new InternalException(node.getClass().getName());
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
        dumpList(ExpressionNode.class, node.list);
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
        decIndent();
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
        decIndent();
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
        fullLine("new BinaryOperatorNode(");
        incIndent();
        dump(node.token);
        commaBreak();
        beginLine("BinaryOperator.");
        sb.append(node.operator.name());
        sb.append(")");
        decIndent();
    }

    private void dump(BlockStatementNode node) {
        fullLine("new BlockStatementNode(");
        incIndent();
        dump(node.openBrace);
        commaBreak();
        dumpList(node.statements);
        commaBreak();
        dump(node.closeBrace);
        sb.append(")");
        decIndent();
    }

    private void dump(ClassMethodNode node) {
        fullLine("new ClassMethodNode(");
        incIndent();
        dump(node.modifiers);
        commaBreak();
        dump(node.type);
        commaBreak();
        dump(node.name);
        commaBreak();
        dump(node.parameters);
        commaBreak();
        dump(node.arrow);
        commaBreak();
        dump(node.body);
        sb.append(")");
    }

    private void dump(ClassNode node) {
        fullLine("new ClassNode(");
        incIndent();
        dump(node.keyword);
        commaBreak();
        dump(node.name.token);
        commaBreak();
        dump(node.colon);
        commaBreak();
        dumpList(TypeNode.class, node.baseTypeNodes);
        commaBreak();
        dump(node.openBrace);
        commaBreak();
        dumpList(node.members);
        commaBreak();
        dump(node.closeBrace);
        sb.append(")");
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
            beginLine("new CompilationUnitMembersListNode(Lists.of(), ");
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
        dump(node.arrow);
        commaBreak();
        dump(node.body);
        sb.append(")");
        decIndent();
    }

    private void dump(ModifiersNode node) {
        if (node.tokens.isEmpty()) {
            beginLine("new ModifiersNode(Lists.of(), ");
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
        dumpList(ParameterNode.class, node.parameters);
        commaBreak();
        dump(node.closeParen);
        sb.append(")");
        decIndent();
    }

    private void dump(ParameterNode node) {
        fullLine("new ParameterNode(");
        incIndent();
        dump(node.getType());
        commaBreak();
        dump(node.getName());
        sb.append(')');
        decIndent();
    }

    private void dump(StatementsListNode node) {
        if (node.statements.isEmpty()) {
            beginLine("new StatementsListNode(Lists.of(), ");
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
        fullLine("new ForLoopStatementNode(");
        incIndent();
        dump(node.keyword);
        commaBreak();
        dump(node.openParen);
        commaBreak();
        dump(node.init);
        commaBreak();
        dump(node.semicolon1);
        commaBreak();
        dump(node.condition);
        commaBreak();
        dump(node.semicolon2);
        commaBreak();
        dump(node.update);
        commaBreak();
        dump(node.closeParen);
        commaBreak();
        dump(node.body);
        sb.append(")");
        decIndent();
    }

    private void dump(ForEachLoopStatementNode node) {
        fullLine("new ForEachLoopStatementNode(");
        incIndent();
        dump(node.keyword);
        commaBreak();
        dump(node.openParen);
        commaBreak();
        dump(node.typeNode);
        commaBreak();
        dump(node.name);
        commaBreak();
        dump(node.in);
        commaBreak();
        dump(node.iterable);
        commaBreak();
        dump(node.closeParen);
        commaBreak();
        dump(node.body);
        sb.append(")");
    }

    private void dump(WhileLoopStatementNode node) {
        fullLine("new WhileLoopStatementNode(");
        incIndent();
        dump(node.keyword);
        commaBreak();
        dump(node.openParen);
        commaBreak();
        dump(node.condition);
        commaBreak();
        dump(node.closeParen);
        commaBreak();
        dump(node.body);
        sb.append(")");
        decIndent();
    }

    private void dump(IfStatementNode node) {
        fullLine("new IfStatementNode(");
        incIndent();
        dump(node.ifToken);
        commaBreak();
        dump(node.openParen);
        commaBreak();
        dump(node.condition);
        commaBreak();
        dump(node.closeParen);
        commaBreak();
        dump(node.thenStatement);
        commaBreak();
        dump(node.elseToken);
        commaBreak();
        dump(node.elseStatement);
        commaBreak();
        dump(node.getRange());
        sb.append(")");
        decIndent();
    }

    private void dump(ReturnStatementNode node) {
        fullLine("new ReturnStatementNode(");
        incIndent();
        dump(node.keyword);
        commaBreak();
        dump(node.expression);
        commaBreak();
        dump(node.semicolon);
        sb.append(")");
        decIndent();
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
        dump(node.equal);
        commaBreak();
        dump(node.expression);
        commaBreak();
        dump(node.semicolon);
        sb.append(")");
        decIndent();
    }

    private void dump(InvalidStatementNode node) {
        beginLine("new InvalidStatementNode(");
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
        dump(node.operator);
        commaBreak();
        dump(node.name);
        sb.append(")");
        decIndent();
    }

    private void dump(ObjectCreationExpressionNode node) {
        fullLine("new ObjectCreationExpressionNode(");
        incIndent();
        dump(node.keyword);
        commaBreak();
        dump(node.typeNode);
        commaBreak();
        dump(node.arguments);
        sb.append(")");
        decIndent();
    }

    private void dump(IntegerLiteralExpressionNode node) {
        fullLine("new IntegerLiteralExpressionNode(");
        incIndent();
        dump(node.sign);
        commaBreak();
        dump(node.token);
        sb.append(")");
        decIndent();
    }

    private void dump(BooleanLiteralExpressionNode node) {
        fullLine("new BooleanLiteralExpressionNode(");
        incIndent();
        dump(node.token);
        commaBreak();
        beginLine(String.valueOf(node.value));
        sb.append(")");
        decIndent();
    }

    private void dump(NameExpressionNode node) {
        fullLine("new NameExpressionNode(");
        incIndent();
        dump(node.token);
        sb.append(")");
        decIndent();
    }

    private void dump(InvalidExpressionNode node) {
        beginLine("new InvalidExpressionNode(");
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(ArgumentsListNode node) {
        fullLine("new ArgumentsListNode(");
        incIndent();
        dump(node.openParen);
        commaBreak();
        dumpList(ExpressionNode.class, node.arguments);
        commaBreak();
        dump(node.closeParen);
        sb.append(")");
    }

    public void dump(ArrayCreationExpressionNode node) {
        fullLine("new ArrayCreationExpressionNode(");
        incIndent();
        dump(node.keyword);
        commaBreak();
        dump(node.typeNode);
        commaBreak();
        dump(node.openBracket);
        commaBreak();
        dump(node.lengthExpression);
        commaBreak();
        dump(node.closeBracket);
        sb.append(")");
        decIndent();
    }

    private void dump(LetTypeNode node) {
        fullLine("new LetTypeNode(");
        dump(node.token);
        sb.append(")");
    }

    private void dump(CustomTypeNode node) {
        fullLine("new CustomTypeNode(");
        incIndent();
        dump(node.token);
        sb.append(")");
        decIndent();
    }

    private void dump(PredefinedTypeNode node) {
        fullLine("new PredefinedTypeNode(");
        incIndent();
        dump(node.token);
        commaBreak();
        beginLine("PredefinedType.");
        sb.append(node.type);
        sb.append(")");
        decIndent();
    }

    private void dump(VoidTypeNode node) {
        fullLine("new VoidTypeNode(");
        incIndent();
        dump(node.token);
        sb.append(")");
        decIndent();
    }

    private void dump(InvalidTypeNode node) {
        fullLine("new InvalidTypeNode(");
        incIndent();
        dump(node.token);
        sb.append(")");
        decIndent();
    }

    private void dump(UnaryExpressionNode node) {
        fullLine("new UnaryExpressionNode(");
        incIndent();
        dump(node.operator);
        commaBreak();
        dump(node.operand);
        sb.append(")");
        decIndent();
    }

    private void dump(UnaryOperatorNode node) {
        fullLine("new UnaryOperatorNode(");
        incIndent();
        dump(node.token);
        commaBreak();
        beginLine("UnaryOperator." + node.operator.name());
        sb.append(")");
        decIndent();
    }

    private void dump(MetaTypeExpressionNode node) {
        fullLine("new MetaTypeExpressionNode(");
        incIndent();
        dump(node.keyword);
        commaBreak();
        dump(node.openParen);
        commaBreak();
        dump(node.type);
        commaBreak();
        dump(node.closeParen);
        sb.append(")");
        decIndent();
    }

    private void dump(MetaTypeOfExpressionNode node) {
        fullLine("new MetaTypeOfExpressionNode(");
        incIndent();
        dump(node.keyword);
        commaBreak();
        dump(node.openParen);
        commaBreak();
        dump(node.expression);
        commaBreak();
        dump(node.closeParen);
        sb.append(")");
        decIndent();
    }

    private void dump(JavaTypeNode node) {
        fullLine("new JavaTypeNode(");
        incIndent();
        dump(node.java);
        commaBreak();
        dump(node.openBracket);
        commaBreak();
        dump(node.name);
        commaBreak();
        dump(node.closeBracket);
        sb.append(")");
        decIndent();
    }

    private void dump(JavaQualifiedTypeNameNode node) {
        beginLine("new JavaQualifiedTypeNameNode(");
        incIndent();
        dumpTokens(node.tokens);
        commaBreak();
        beginLine("\"" + node.value + "\"");
        sb.append(")");
        decIndent();
    }

    private void dump(LambdaExpressionNode node) {
        fullLine("new LambdaExpressionNode(");
        incIndent();
        dump(node.openParen);
        commaBreak();
        dumpList(NameExpressionNode.class, node.parameters);
        commaBreak();
        dump(node.closeParen);
        commaBreak();
        dump(node.arrow);
        commaBreak();
        dump(node.body);
        sb.append(")");
        decIndent();
    }

    private void dump(IsExpressionNode node) {
        fullLine("new TypeTestExpressionNode(");
        incIndent();
        dump(node.expression);
        commaBreak();
        dump(node.keyword);
        commaBreak();
        dump(node.pattern);
        sb.append(")");
        decIndent();
    }

    private void dump(StaticReferenceNode node) {
        fullLine("new StaticReferenceNode(");
        incIndent();
        dump(node.typeNode);
        commaBreak();
        dump(node.getRange());
        sb.append(")");
        decIndent();
    }

    private void dump(FunctionTypeNode node) {
        fullLine("new FunctionTypeNode(");
        incIndent();
        dump(node.fn);
        commaBreak();
        dump(node.openBracket);
        commaBreak();
        dump(node.openParen);
        commaBreak();
        dumpList(TypeNode.class, node.parameterTypes);
        commaBreak();
        dump(node.closeParen);
        commaBreak();
        dump(node.arrow);
        commaBreak();
        dump(node.returnTypeNode);
        commaBreak();
        dump(node.closeBracket);
        sb.append(")");
        decIndent();
    }

    private void dumpTokens(List<Token> tokens) {
        boolean newLine = beginNewLineIfRequired();

        endLine("Lists.of(");
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
        if (nodes.isEmpty()) {
            sb.append("Lists.of()");
            return;
        }

        endLine("Lists.of(");
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

    private <T extends ParserNode> void dumpList(Class<T> clazz, SeparatedList<T> nodes) {
        if (nodes.size() == 0) {
            beginLine("SeparatedList.of()");
        } else {
            fullLine("SeparatedList.of(");
            incIndent();
            beginLine(clazz.getSimpleName() + ".class");
            for (Locatable item : nodes.getChildNodes()) {
                commaBreak();
                if (item instanceof ParserNode) {
                    ParserNode node = (ParserNode) item;
                    dump(node);
                    continue;
                }
                if (item instanceof Token) {
                    Token token = (Token) item;
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
package com.zergatul.scripting.tests.codegen;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.ParserOutput;
import com.zergatul.scripting.parser.PredefinedType;
import com.zergatul.scripting.parser.nodes.*;

import java.util.List;

public class ParserDumper {

    private final StringBuilder sb = new StringBuilder();

    public String dump(ParserOutput output) {
        sb.setLength(0);
        dump(output.unit());
        return sb.toString();
    }

    private void dump(Node node) {
        switch (node.getNodeType()) {
            case COMPILATION_UNIT -> dump((CompilationUnitNode) node);
            case VARIABLE_DECLARATION -> dump((VariableDeclarationNode) node);
            case ARRAY_CREATION_EXPRESSION -> dump((ArrayCreationExpressionNode) node);
            case ARRAY_INITIALIZER_EXPRESSION -> dump((ArrayInitializerExpressionNode) node);
            case OBJECT_CREATION_EXPRESSION -> dump((ObjectCreationExpressionNode) node);
            case INTEGER_LITERAL -> dump((IntegerLiteralExpressionNode) node);
            case NAME_EXPRESSION -> dump((NameExpressionNode) node);
            case ARGUMENTS_LIST -> dump((ArgumentsListNode) node);
            case LET_TYPE -> dump((LetTypeNode) node);
            case ARRAY_TYPE -> dump((ArrayTypeNode) node);
            case CUSTOM_TYPE -> dump((CustomTypeNode) node);
            case PREDEFINED_TYPE -> dump((PredefinedTypeNode) node);
            default -> throw new InternalException(node.getClass().getName());
        }
    }

    private void dump(CompilationUnitNode node) {
        sb.append("new CompilationUnitNode(");
        sb.append("new CompilationUnitMembersListNode(List.of(), new SingleLineTextRange(1, 1, 0, 0)),");
        dump(node.statements);
        sb.append(",");
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(StatementsListNode node) {
        sb.append("new StatementsListNode(");
        sb.append("List.of(");
        for (int i = 0; i < node.statements.size(); i++) {
            dump(node.statements.get(i));
            if (i < node.statements.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
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
        }
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
        sb.append("List.of(");
        List<ExpressionNode> items = node.items;
        for (int i = 0; i < items.size(); i++) {
            dump(items.get(i));
            if (i < items.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
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

    private void dump(NameExpressionNode node) {
        sb.append("new NameExpressionNode(\"");
        sb.append(node.value);
        sb.append("\", ");
        dump(node.getRange());
        sb.append(")");
    }

    private void dump(ArgumentsListNode node) {
        sb.append("new ArgumentsListNode(");
        sb.append("List.of(");
        for (int i = 0; i < node.arguments.size(); i++) {
            dump(node.arguments.get(i));
            if (i < node.arguments.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
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
        } else {
            throw new InternalException();
        }
    }
}
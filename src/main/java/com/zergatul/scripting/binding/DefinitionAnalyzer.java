package com.zergatul.scripting.binding;

import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.UnaryOperator;

import java.util.ArrayList;
import java.util.List;

public class DefinitionAnalyzer {

    public AssignedVariables analyze(BoundExpressionNode expression) {
        return switch (expression.getNodeType()) {
            case UNARY_EXPRESSION -> analyze((BoundUnaryExpressionNode) expression);
            case BINARY_EXPRESSION -> analyze((BoundBinaryExpressionNode) expression);
            case TYPE_TEST_EXPRESSION -> analyze((BoundTypeTestExpressionNode) expression);
            default -> AssignedVariables.EMPTY;
        };
    }

    private AssignedVariables analyze(BoundUnaryExpressionNode node) {
        if (node.operator.operation.operator == UnaryOperator.NOT) {
            AssignedVariables inner = analyze(node.operand);
            return new AssignedVariables(inner.definitelyUnassigned, inner.definitelyAssigned);
        } else {
            return AssignedVariables.EMPTY;
        }
    }

    private AssignedVariables analyze(BoundBinaryExpressionNode node) {
        return switch (node.operator.operation.operator) {
            case BOOLEAN_AND -> {
                AssignedVariables left = analyze(node.left);
                AssignedVariables right = analyze(node.right);
                yield new AssignedVariables(join(left.definitelyAssigned, right.definitelyAssigned), List.of());
            }
            case BOOLEAN_OR -> {
                AssignedVariables left = analyze(node.left);
                AssignedVariables right = analyze(node.right);
                yield new AssignedVariables(List.of(), join(left.definitelyUnassigned, right.definitelyUnassigned));
            }
            default -> AssignedVariables.EMPTY;
        };
    }

    private AssignedVariables analyze(BoundTypeTestExpressionNode node) {
        if (node.pattern.getNodeType() == NodeType.DECLARATION_PATTERN) {
            BoundDeclarationPatternNode pattern = (BoundDeclarationPatternNode) node.pattern;
            return new AssignedVariables(List.of(pattern.declaration), List.of());
        } else {
            return AssignedVariables.EMPTY;
        }
    }

    private <T> List<T> join(List<T> list1, List<T> list2) {
        ArrayList<T> list = new ArrayList<T>(list1.size() + list2.size());
        list.addAll(list1);
        list.addAll(list2);
        return list;
    }

    public record AssignedVariables(
            List<BoundVariableDeclarationNode> definitelyAssigned,
            List<BoundVariableDeclarationNode> definitelyUnassigned
    ) {
        public static final AssignedVariables EMPTY = new AssignedVariables(List.of(), List.of());
    }
}
package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.SyntaxFactory;
import com.zergatul.scripting.parser.nodes.UnaryOperatorNode;
import com.zergatul.scripting.type.operation.UnaryOperation;

import java.util.List;

public class BoundUnaryOperatorNode extends BoundNode {

    public final UnaryOperatorNode syntaxNode;
    public final UnaryOperation operation;

    public BoundUnaryOperatorNode(UnaryOperation operation) {
        this(SyntaxFactory.missingUnaryOperator(), operation, TextRange.MISSING);
    }

    public BoundUnaryOperatorNode(UnaryOperatorNode node, UnaryOperation operation) {
        this(node, operation, node.getRange());
    }

    public BoundUnaryOperatorNode(UnaryOperatorNode node, UnaryOperation operation, TextRange range) {
        super(BoundNodeType.UNARY_OPERATOR, range);
        this.syntaxNode = node;
        this.operation = operation;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {}

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}
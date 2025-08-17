package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeRewriter;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.operation.PostfixOperation;

import java.util.List;

public class BoundPostfixStatementNode extends BoundStatementNode {

    public final BoundExpressionNode expression;
    public final PostfixOperation operation;

    public BoundPostfixStatementNode(NodeType nodeType, BoundExpressionNode expression, PostfixOperation operation) {
        this(nodeType, expression, operation, null);
    }

    public BoundPostfixStatementNode(NodeType nodeType, BoundExpressionNode expression, PostfixOperation operation, TextRange range) {
        super(nodeType, range);
        this.expression = expression;
        this.operation = operation;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public BoundNode accept(BinderTreeRewriter rewriter) {
        return rewriter.visit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        expression.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(expression);
    }
}
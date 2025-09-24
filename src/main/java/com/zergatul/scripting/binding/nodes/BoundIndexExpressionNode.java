package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.nodes.IndexExpressionNode;
import com.zergatul.scripting.type.operation.IndexOperation;

import java.util.List;

public class BoundIndexExpressionNode extends BoundExpressionNode {

    public final BoundExpressionNode callee;
    public final Token openBracket;
    public final BoundExpressionNode index;
    public final Token closeBracket;
    public final IndexOperation operation;

    public BoundIndexExpressionNode(BoundExpressionNode callee, BoundExpressionNode index, IndexOperation operation) {
        this(callee, null, index, null, operation, null);
    }

    public BoundIndexExpressionNode(IndexExpressionNode node, BoundExpressionNode callee, BoundExpressionNode index, IndexOperation operation) {
        this(callee, node.openBracket, index, node.closeBracket, operation, node.getRange());
    }

    public BoundIndexExpressionNode(BoundExpressionNode callee, Token openBracket, BoundExpressionNode index, Token closeBracket, IndexOperation operation, TextRange range) {
        super(BoundNodeType.INDEX_EXPRESSION, operation.returnType, range);
        this.callee = callee;
        this.openBracket = openBracket;
        this.index = index;
        this.closeBracket = closeBracket;
        this.operation = operation;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        callee.accept(visitor);
        index.accept(visitor);
    }

    @Override
    public boolean canSet() {
        return operation.canSet();
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(callee, index);
    }
}
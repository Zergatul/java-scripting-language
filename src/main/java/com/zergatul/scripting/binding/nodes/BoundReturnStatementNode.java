package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;

import java.util.List;
import java.util.Objects;

public class BoundReturnStatementNode extends BoundStatementNode {

    public final Token keyword;
    public final BoundExpressionNode expression;

    public BoundReturnStatementNode(BoundExpressionNode expression) {
        this(null, expression, null);
    }

    public BoundReturnStatementNode(Token keyword, BoundExpressionNode expression, TextRange range) {
        super(BoundNodeType.RETURN_STATEMENT, range);
        this.keyword = keyword;
        this.expression = expression;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        if (expression != null) {
            expression.accept(visitor);
        }
    }

    @Override
    public List<BoundNode> getChildren() {
        return expression == null ? List.of() : List.of(expression);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BoundReturnStatementNode other) {
            return Objects.equals(other.expression, expression) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}
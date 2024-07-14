package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserVisitor;

import java.util.Objects;

public class ReturnStatementNode extends StatementNode {

    public final ExpressionNode expression;

    public ReturnStatementNode(ExpressionNode expression, TextRange range) {
        super(NodeType.RETURN_STATEMENT, range);
        this.expression = expression;
    }

    @Override
    public void accept(ParserVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserVisitor visitor) {
        if (expression != null) {
            expression.accept(visitor);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ReturnStatementNode other) {
            return Objects.equals(other.expression, expression) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}
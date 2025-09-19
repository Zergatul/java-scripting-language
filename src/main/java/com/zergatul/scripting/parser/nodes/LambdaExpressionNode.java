package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;
import java.util.Objects;

public class LambdaExpressionNode extends ExpressionNode {

    public final List<NameExpressionNode> parameters;
    public final Token arrow;
    public final StatementNode body;

    public LambdaExpressionNode(List<NameExpressionNode> parameters, Token arrow, StatementNode body, TextRange range) {
        super(NodeType.LAMBDA_EXPRESSION, range);
        this.parameters = parameters;
        this.arrow = arrow;
        this.body = body;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        for (NameExpressionNode name : parameters) {
            name.accept(visitor);
        }
        body.accept(visitor);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LambdaExpressionNode other) {
            return  Objects.equals(other.parameters, parameters) &&
                    other.arrow.equals(arrow) &&
                    other.body.equals(body) &&
                    other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}
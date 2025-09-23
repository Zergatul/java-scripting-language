package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class LambdaExpressionNode extends ExpressionNode {

    public final Token openParen;
    public final SeparatedList<NameExpressionNode> parameters;
    public final Token closeParen;
    public final Token arrow;
    public final StatementNode body;

    public LambdaExpressionNode(
            Token openParen,
            SeparatedList<NameExpressionNode> parameters,
            Token closeParen,
            Token arrow,
            StatementNode body
    ) {
        super(ParserNodeType.LAMBDA_EXPRESSION, TextRange.combine(openParen != null ? openParen : parameters.getNodeAt(0), body));
        this.openParen = openParen;
        this.parameters = parameters;
        this.closeParen = closeParen;
        this.arrow = arrow;
        this.body = body;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        for (NameExpressionNode name : parameters.getNodes()) {
            name.accept(visitor);
        }
        body.accept(visitor);
    }
}
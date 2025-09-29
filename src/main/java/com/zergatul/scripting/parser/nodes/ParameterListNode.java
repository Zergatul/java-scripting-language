package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.ArrayList;
import java.util.List;

public class ParameterListNode extends ParserNode {

    public final Token openParen;
    public final SeparatedList<ParameterNode> parameters;
    public final Token closeParen;

    public ParameterListNode(Token openParen, SeparatedList<ParameterNode> parameters, Token closeParen) {
        super(ParserNodeType.PARAMETER_LIST, TextRange.combine(openParen, closeParen));
        this.openParen = openParen;
        this.parameters = parameters;
        this.closeParen = closeParen;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        for (ParameterNode parameter : parameters.getNodes()) {
            parameter.accept(visitor);
        }
    }

    @Override
    public List<Locatable> getChildNodes() {
        List<Locatable> nodes = new ArrayList<>();
        nodes.add(openParen);
        nodes.addAll(parameters.getChildNodes());
        nodes.add(closeParen);
        return nodes;
    }

    public boolean hasParentheses() {
        return !openParen.isMissing() && !closeParen.isMissing();
    }
}
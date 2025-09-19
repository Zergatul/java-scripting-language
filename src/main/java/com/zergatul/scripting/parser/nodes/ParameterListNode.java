package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;
import java.util.Objects;

public class ParameterListNode extends ParserNode {

    public final Token openParen;
    public final List<ParameterNode> parameters;
    public final Token closeParen;

    public ParameterListNode(Token openParen, List<ParameterNode> parameters, Token closeParen, TextRange range) {
        super(NodeType.PARAMETER_LIST, range);
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
        for (ParameterNode parameter : parameters) {
            parameter.accept(visitor);
        }
    }

    public boolean hasParentheses() {
        return !openParen.isMissing() && !closeParen.isMissing();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ParameterListNode other) {
            return  other.openParen.equals(openParen) &&
                    Objects.equals(other.parameters, parameters) &&
                    other.closeParen.equals(closeParen) &&
                    other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}
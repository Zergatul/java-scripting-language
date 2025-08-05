package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;
import java.util.Objects;

public class ParameterListNode extends Node {

    public final Token openParenthesis;
    public final List<ParameterNode> parameters;
    public final Token closeParenthesis;

    public ParameterListNode(Token openParenthesis, List<ParameterNode> parameters, Token closeParenthesis, TextRange range) {
        super(NodeType.PARAMETER_LIST, range);
        this.openParenthesis = openParenthesis;
        this.parameters = parameters;
        this.closeParenthesis = closeParenthesis;
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
        return openParenthesis.getRange().getLength() > 0 && closeParenthesis.getRange().getLength() > 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ParameterListNode other) {
            return  other.openParenthesis.equals(openParenthesis) &&
                    Objects.equals(other.parameters, parameters) &&
                    other.closeParenthesis.equals(closeParenthesis) &&
                    other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}
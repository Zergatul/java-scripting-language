package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.nodes.ParameterListNode;

import java.util.List;
import java.util.Objects;

public class BoundParameterListNode extends BoundNode {

    public final Token openParen;
    public final List<BoundParameterNode> parameters;
    public final Token closeParen;

    public BoundParameterListNode(List<BoundParameterNode> parameters, ParameterListNode node) {
        this(node.openParen, parameters, node.closeParen, node.getRange());
    }

    public BoundParameterListNode(Token openParen, List<BoundParameterNode> parameters, Token closeParen, TextRange range) {
        super(BoundNodeType.PARAMETER_LIST, range);
        this.openParen = openParen;
        this.parameters = parameters;
        this.closeParen = closeParen;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        for (BoundParameterNode parameter : parameters) {
            parameter.accept(visitor);
        }
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.copyOf(parameters);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BoundParameterListNode other) {
            return  other.openParen.equals(openParen) &&
                    Objects.equals(other.parameters, parameters) &&
                    other.closeParen.equals(closeParen) &&
                    other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}
package com.zergatul.scripting.parser.nodes;

public class MemberAccessExpressionNode extends ExpressionNode {

    public final ExpressionNode callee;
    public final String identifier;

    public MemberAccessExpressionNode(ExpressionNode callee, String identifier) {
        this.callee = callee;
        this.identifier = identifier;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MemberAccessExpressionNode other) {
            return other.callee.equals(callee) && other.identifier.equals(identifier);
        } else {
            return false;
        }
    }
}

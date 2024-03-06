package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;

public class MemberAccessExpressionNode extends ExpressionNode {

    public final ExpressionNode callee;
    public final String identifier;

    public MemberAccessExpressionNode(ExpressionNode callee, String identifier, TextRange range) {
        super(range);
        this.callee = callee;
        this.identifier = identifier;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MemberAccessExpressionNode other) {
            return  other.callee.equals(callee) &&
                    other.identifier.equals(identifier) &&
                    other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}
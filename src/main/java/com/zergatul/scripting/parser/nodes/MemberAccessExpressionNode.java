package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

public class MemberAccessExpressionNode extends ExpressionNode {

    public final ExpressionNode callee;
    public final NameExpressionNode name;

    public MemberAccessExpressionNode(ExpressionNode callee, NameExpressionNode name, TextRange range) {
        super(NodeType.MEMBER_ACCESS_EXPRESSION, range);
        this.callee = callee;
        this.name = name;
    }

    @Override
    public boolean isAsync() {
        return callee.isAsync();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MemberAccessExpressionNode other) {
            return  other.callee.equals(callee) &&
                    other.name.equals(name) &&
                    other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}
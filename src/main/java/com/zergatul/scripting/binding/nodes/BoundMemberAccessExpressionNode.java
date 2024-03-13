package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SMethodReferences;

public class BoundMemberAccessExpressionNode extends BoundExpressionNode {

    public final BoundExpressionNode callee;
    public final String name;

    public BoundMemberAccessExpressionNode(BoundExpressionNode callee, String name, SMethodReferences references, TextRange range) {
        super(NodeType.MEMBER_ACCESS_EXPRESSION, references, range);
        this.callee = callee;
        this.name = name;
    }
}
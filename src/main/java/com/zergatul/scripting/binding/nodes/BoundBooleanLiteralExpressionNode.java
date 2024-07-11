package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SBoolean;

import java.util.List;

public class BoundBooleanLiteralExpressionNode extends BoundExpressionNode {

    public final boolean value;

    public BoundBooleanLiteralExpressionNode(boolean value, TextRange range) {
        super(NodeType.BOOLEAN_LITERAL, SBoolean.instance, range);
        this.value = value;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}
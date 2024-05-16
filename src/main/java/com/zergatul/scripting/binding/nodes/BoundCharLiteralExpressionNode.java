package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SChar;

import java.util.List;

public class BoundCharLiteralExpressionNode extends BoundExpressionNode {

    public final char value;

    public BoundCharLiteralExpressionNode(char value, TextRange range) {
        super(NodeType.CHAR_LITERAL, SChar.instance, range);
        this.value = value;
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}
package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundArgumentsListNode extends BoundNode {

    public final List<BoundExpressionNode> arguments;

    public BoundArgumentsListNode(List<BoundExpressionNode> arguments, TextRange range) {
        super(NodeType.ARGUMENTS_LIST, range);
        this.arguments = arguments;
    }
}
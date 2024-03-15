package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SMethodsHolder;

public class BoundMethodAccessExpressionNode extends BoundExpressionNode {

    public final BoundExpressionNode callee;
    public final String name;

    public BoundMethodAccessExpressionNode(BoundExpressionNode callee, String name, SMethodsHolder holder, TextRange range) {
        super(NodeType.METHOD_ACCESS_EXPRESSION, holder, range);
        this.callee = callee;
        this.name = name;
    }
}
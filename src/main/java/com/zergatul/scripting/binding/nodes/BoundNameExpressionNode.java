package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.compiler.Symbol;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.nodes.NameExpressionNode;
import com.zergatul.scripting.type.SType;

public class BoundNameExpressionNode extends BoundExpressionNode {

    public final String value;
    public final Symbol symbol;

    public BoundNameExpressionNode(Symbol symbol, SType type, String value, TextRange range) {
        super(NodeType.NAME_EXPRESSION, type, range);
        this.symbol = symbol;
        this.value = value;
    }
}
package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.compiler.Symbol;
import com.zergatul.scripting.compiler.Variable;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.nodes.NameExpressionNode;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundNameExpressionNode extends BoundExpressionNode {

    public final String value;
    public final Symbol symbol;

    public BoundNameExpressionNode(Symbol symbol, TextRange range) {
        this(symbol, symbol.getType(), symbol.getName(), range);
    }

    public BoundNameExpressionNode(Symbol symbol, SType type, String value, TextRange range) {
        super(NodeType.NAME_EXPRESSION, type, range);
        this.symbol = symbol;
        this.value = value;
    }

    @Override
    public boolean canSet() {
        return symbol != null && symbol.canSet();
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}
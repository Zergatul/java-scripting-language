package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.NameExpressionNode;
import com.zergatul.scripting.symbols.Function;

import java.util.List;

public class BoundFunctionNode extends BoundNode {

    public final NameExpressionNode syntaxNode;
    public final Function function;

    public BoundFunctionNode(NameExpressionNode node, Function function) {
        this(node, function, node.getRange());
    }

    public BoundFunctionNode(NameExpressionNode node, Function function, TextRange range) {
        super(BoundNodeType.FUNCTION, range);
        this.syntaxNode = node;
        this.function = function;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {}

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}
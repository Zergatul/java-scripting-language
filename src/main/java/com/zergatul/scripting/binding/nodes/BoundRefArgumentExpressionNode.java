package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.symbols.LocalVariable;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundRefArgumentExpressionNode extends BoundExpressionNode {

    public final BoundNameExpressionNode name;
    public final LocalVariable holder;

    public BoundRefArgumentExpressionNode(BoundNameExpressionNode name, LocalVariable holder, SType type, TextRange range) {
        super(NodeType.REF_ARGUMENT_EXPRESSION, type, range);
        this.name = name;
        this.holder = holder;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        name.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(name);
    }
}
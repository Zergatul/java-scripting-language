package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundTypePatternNode extends BoundPatternNode {

    public final BoundTypeNode type;

    public BoundTypePatternNode(BoundTypeNode type, TextRange range) {
        super(NodeType.TYPE_PATTERN, range);
        this.type = type;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        type.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(type);
    }
}
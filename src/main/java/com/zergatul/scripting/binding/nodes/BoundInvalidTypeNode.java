package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.CustomTypeNode;
import com.zergatul.scripting.parser.nodes.InvalidTypeNode;
import com.zergatul.scripting.parser.nodes.LetTypeNode;
import com.zergatul.scripting.parser.nodes.ParserNode;
import com.zergatul.scripting.type.SUnknown;

import java.util.List;

public class BoundInvalidTypeNode extends BoundTypeNode {

    public final ParserNode syntaxNode;

    public BoundInvalidTypeNode(LetTypeNode node) {
        this(node, node.getRange());
    }

    public BoundInvalidTypeNode(CustomTypeNode node) {
        this(node, node.getRange());
    }

    public BoundInvalidTypeNode(InvalidTypeNode node) {
        this(node, node.getRange());
    }

    public BoundInvalidTypeNode(ParserNode node, TextRange range) {
        super(BoundNodeType.INVALID_TYPE, SUnknown.instance, range);
        this.syntaxNode = node;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {

    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}
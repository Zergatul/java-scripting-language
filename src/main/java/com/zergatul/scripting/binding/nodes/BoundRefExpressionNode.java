package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.compiler.LocalVariable;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SType;

public class BoundRefExpressionNode extends BoundExpressionNode {

    public final BoundNameExpressionNode name;
    public final LocalVariable holder;

    public BoundRefExpressionNode(BoundNameExpressionNode name, LocalVariable holder, SType type, TextRange range) {
        super(NodeType.REF_EXPRESSION, type, range);
        this.name = name;
        this.holder = holder;
    }
}
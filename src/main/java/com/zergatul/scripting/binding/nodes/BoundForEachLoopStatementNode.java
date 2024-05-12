package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.compiler.LocalVariable;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundForEachLoopStatementNode extends BoundStatementNode {

    public final BoundTypeNode typeNode;
    public final BoundNameExpressionNode name;
    public final BoundExpressionNode iterable;
    public final BoundStatementNode body;
    public final LocalVariable index;
    public final LocalVariable length;

    public BoundForEachLoopStatementNode(
            BoundTypeNode typeNode,
            BoundNameExpressionNode name,
            BoundExpressionNode iterable,
            BoundStatementNode body,
            LocalVariable index,
            LocalVariable length,
            TextRange range
    ) {
        super(NodeType.FOREACH_LOOP_STATEMENT, range);
        this.typeNode = typeNode;
        this.name = name;
        this.iterable = iterable;
        this.body = body;
        this.index = index;
        this.length = length;
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(typeNode, name, iterable, body);
    }
}
package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

public class ForEachLoopStatementNode extends StatementNode {

    public final TypeNode typeNode;
    public final NameExpressionNode name;
    public final ExpressionNode iterable;
    public final StatementNode body;

    public ForEachLoopStatementNode(TypeNode typeNode, NameExpressionNode name, ExpressionNode iterable, StatementNode body, TextRange range) {
        super(NodeType.FOREACH_LOOP_STATEMENT, range);
        this.typeNode = typeNode;
        this.name = name;
        this.iterable = iterable;
        this.body = body;
    }
}
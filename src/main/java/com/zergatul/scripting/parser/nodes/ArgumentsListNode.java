package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserVisitor;

import java.util.List;
import java.util.Objects;

public class ArgumentsListNode extends Node {

    public final List<ExpressionNode> arguments;

    public ArgumentsListNode(List<ExpressionNode> arguments, TextRange range) {
        super(NodeType.ARGUMENTS_LIST, range);
        this.arguments = arguments;
    }

    @Override
    public void accept(ParserVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserVisitor visitor) {
        for (ExpressionNode expression : arguments) {
            expression.accept(visitor);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ArgumentsListNode other) {
            return Objects.equals(other.arguments, arguments) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}
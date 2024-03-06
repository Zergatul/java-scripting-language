package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Node;
import com.zergatul.scripting.TextRange;

import java.util.List;
import java.util.Objects;

public class ArgumentsListNode extends Node {

    public final List<ExpressionNode> arguments;

    public ArgumentsListNode(List<ExpressionNode> arguments, TextRange range) {
        super(range);
        this.arguments = arguments;
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
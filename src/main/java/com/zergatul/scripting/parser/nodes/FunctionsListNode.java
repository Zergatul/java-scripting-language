package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;
import java.util.Objects;

public class FunctionsListNode extends Node {

    public final List<FunctionNode> functions;

    public FunctionsListNode(List<FunctionNode> functions, TextRange range) {
        super(NodeType.FUNCTIONS_LIST, range);
        this.functions = functions;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        for (FunctionNode function : functions) {
            function.accept(visitor);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FunctionsListNode other) {
            return Objects.equals(other.functions, functions) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}
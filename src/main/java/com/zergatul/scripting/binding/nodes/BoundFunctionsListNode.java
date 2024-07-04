package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundFunctionsListNode extends BoundNode {

    public final List<BoundFunctionNode> functions;

    public BoundFunctionsListNode(List<BoundFunctionNode> functions, TextRange range) {
        super(NodeType.FUNCTIONS_LIST, range);
        this.functions = functions;
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.copyOf(functions);
    }
}
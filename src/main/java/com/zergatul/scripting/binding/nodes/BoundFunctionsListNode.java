package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.nodes.FunctionNode;

import java.util.List;
import java.util.Objects;

public class BoundFunctionsListNode extends BoundNode {

    public final List<BoundFunctionNode> functions;

    public BoundFunctionsListNode(List<BoundFunctionNode> functions, TextRange range) {
        super(NodeType.FUNCTIONS_LIST, range);
        this.functions = functions;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        for (BoundFunctionNode function : functions) {
            function.accept(visitor);
        }
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.copyOf(functions);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BoundFunctionsListNode other) {
            return Objects.equals(other.functions, functions) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}
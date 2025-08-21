package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundFunctionNode extends BoundCompilationUnitMemberNode {

    public final boolean isAsync;
    public final BoundTypeNode returnType;
    public final BoundNameExpressionNode name;
    public final BoundParameterListNode parameters;
    public final BoundStatementNode body;

    public BoundFunctionNode(boolean isAsync, BoundTypeNode returnType, BoundNameExpressionNode name, BoundParameterListNode parameters, BoundStatementNode body, TextRange range) {
        super(NodeType.FUNCTION, range);
        this.isAsync = isAsync;
        this.returnType = returnType;
        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        returnType.accept(visitor);
        name.accept(visitor);
        parameters.accept(visitor);
        body.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(returnType, name, parameters, body);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BoundFunctionNode other) {
            return  other.isAsync == isAsync &&
                    other.returnType.equals(returnType) &&
                    other.name.equals(name) &&
                    other.parameters.equals(parameters) &&
                    other.body.equals(body) &&
                    other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}
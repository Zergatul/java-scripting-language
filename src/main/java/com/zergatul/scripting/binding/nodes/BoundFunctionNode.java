package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.FunctionNode;
import com.zergatul.scripting.parser.nodes.ModifiersNode;
import com.zergatul.scripting.symbols.LiftedVariable;

import java.util.List;

public class BoundFunctionNode extends BoundCompilationUnitMemberNode {

    public final boolean isAsync;
    public final ModifiersNode modifiers;
    public final BoundTypeNode returnType;
    public final BoundNameExpressionNode name;
    public final BoundParameterListNode parameters;
    public final BoundStatementNode body;
    public final List<LiftedVariable> lifted;

    public BoundFunctionNode(boolean isAsync, BoundTypeNode returnType, BoundNameExpressionNode name, BoundParameterListNode parameters, BoundStatementNode body, List<LiftedVariable> lifted, FunctionNode node) {
        this(node.modifiers, isAsync, returnType, name, parameters, body, lifted, node.getRange());
    }

    public BoundFunctionNode(ModifiersNode modifiers, boolean isAsync, BoundTypeNode returnType, BoundNameExpressionNode name, BoundParameterListNode parameters, BoundStatementNode body, List<LiftedVariable> lifted, TextRange range) {
        super(BoundNodeType.FUNCTION, range);
        this.modifiers = modifiers;
        this.isAsync = isAsync;
        this.returnType = returnType;
        this.name = name;
        this.parameters = parameters;
        this.body = body;
        this.lifted = lifted;
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
    public boolean isOpen() {
        return body.isOpen();
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
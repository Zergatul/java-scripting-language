package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SMethodFunction;

import java.util.List;

public class BoundClassMethodNode extends BoundClassMemberNode {

    public final boolean isAsync;
    public final SMethodFunction functionType;
    public final BoundTypeNode typeNode;
    public final BoundNameExpressionNode name;
    public final BoundParameterListNode parameters;
    public final BoundStatementNode body;

    public BoundClassMethodNode(boolean isAsync, SMethodFunction functionType, BoundTypeNode typeNode, BoundNameExpressionNode name, BoundParameterListNode parameters, BoundStatementNode body, TextRange range) {
        super(NodeType.CLASS_METHOD, range);
        this.isAsync = isAsync;
        this.functionType = functionType;
        this.typeNode = typeNode;
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
        typeNode.accept(visitor);
        name.accept(visitor);
        parameters.accept(visitor);
        body.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(typeNode, name, parameters, body);
    }

    public BoundClassMethodNode update(BoundBlockStatementNode body) {
        return new BoundClassMethodNode(isAsync, functionType, typeNode, name, parameters, body, getRange());
    }
}
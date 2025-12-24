package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.ClassOperatorOverloadNode;
import com.zergatul.scripting.symbols.LiftedVariable;
import com.zergatul.scripting.type.MethodReference;
import com.zergatul.scripting.type.SMethodFunction;

import java.util.List;

public abstract class BoundClassOperatorOverloadNode extends BoundClassMemberNode {

    public final ClassOperatorOverloadNode syntaxNode;
    public final SMethodFunction functionType;
    public final MethodReference method;
    public final BoundTypeNode returnTypeNode;
    public final BoundParameterListNode parameters;
    public final BoundStatementNode body;
    public final List<LiftedVariable> lifted;

    protected BoundClassOperatorOverloadNode(
            BoundNodeType nodeType,
            ClassOperatorOverloadNode node,
            SMethodFunction functionType,
            MethodReference method,
            BoundTypeNode returnTypeNode,
            BoundParameterListNode parameters,
            BoundStatementNode body,
            List<LiftedVariable> lifted
    ) {
        super(nodeType, node.getRange());
        this.syntaxNode = node;
        this.functionType = functionType;
        this.method = method;
        this.returnTypeNode = returnTypeNode;
        this.parameters = parameters;
        this.body = body;
        this.lifted = lifted;
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        returnTypeNode.accept(visitor);
        parameters.accept(visitor);
        body.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(returnTypeNode, parameters, body);
    }
}
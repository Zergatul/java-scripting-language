package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.ClassOperatorOverloadNode;
import com.zergatul.scripting.symbols.LiftedVariable;
import com.zergatul.scripting.type.MethodReference;
import com.zergatul.scripting.type.SMethodFunction;

import java.util.List;

public abstract class BoundExtensionOperatorOverloadNode extends BoundExtensionMemberNode {

    public final ClassOperatorOverloadNode syntaxNode;
    public final BoundTypeNode returnTypeNode;
    public final BoundParameterListNode parameters;
    public final BoundStatementNode body;
    public final List<LiftedVariable> lifted;

    protected BoundExtensionOperatorOverloadNode(
            BoundNodeType nodeType,
            ClassOperatorOverloadNode node,
            BoundTypeNode returnTypeNode,
            BoundParameterListNode parameters,
            BoundStatementNode body,
            List<LiftedVariable> lifted
    ) {
        super(nodeType, node.getRange());
        this.syntaxNode = node;
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
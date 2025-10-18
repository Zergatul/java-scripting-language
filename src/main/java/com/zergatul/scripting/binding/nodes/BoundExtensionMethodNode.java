package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.ClassMethodNode;
import com.zergatul.scripting.symbols.LiftedVariable;
import com.zergatul.scripting.type.MethodReference;
import com.zergatul.scripting.type.SMethodFunction;

import java.util.List;

public class BoundExtensionMethodNode extends BoundNode {

    public final ClassMethodNode syntaxNode;
    public final SMethodFunction functionType;
    public final MethodReference method;
    public final BoundTypeNode typeNode;
    public final BoundNameExpressionNode name;
    public final BoundParameterListNode parameters;
    public final BoundStatementNode body;
    public final List<LiftedVariable> lifted;

    public BoundExtensionMethodNode(
            ClassMethodNode node,
            SMethodFunction functionType,
            MethodReference method,
            BoundTypeNode typeNode,
            BoundNameExpressionNode name,
            BoundParameterListNode parameters,
            BoundStatementNode body,
            List<LiftedVariable> lifted
    ) {
        this(node, functionType, method, typeNode, name, parameters, body, lifted, node.getRange());
    }

    public BoundExtensionMethodNode(
            ClassMethodNode node,
            SMethodFunction functionType,
            MethodReference method,
            BoundTypeNode typeNode,
            BoundNameExpressionNode name,
            BoundParameterListNode parameters,
            BoundStatementNode body,
            List<LiftedVariable> lifted,
            TextRange range
    ) {
        super(BoundNodeType.EXTENSION_METHOD, range);
        this.syntaxNode = node;
        this.functionType = functionType;
        this.method = method;
        this.typeNode = typeNode;
        this.name = name;
        this.parameters = parameters;
        this.body = body;
        this.lifted = lifted;
    }

    public boolean isAsync() {
        return syntaxNode.modifiers.isAsync();
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
}

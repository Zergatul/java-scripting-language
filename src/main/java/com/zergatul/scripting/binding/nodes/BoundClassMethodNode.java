package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.nodes.ClassMethodNode;
import com.zergatul.scripting.type.SMethodFunction;

import java.util.List;

public class BoundClassMethodNode extends BoundClassMemberNode {

    public final boolean isAsync;
    public final SMethodFunction functionType;
    public final BoundTypeNode typeNode;
    public final BoundNameExpressionNode name;
    public final BoundParameterListNode parameters;
    public final Token arrow;
    public final BoundStatementNode body;

    public BoundClassMethodNode(
            ClassMethodNode node,
            boolean isAsync,
            SMethodFunction functionType,
            BoundTypeNode typeNode,
            BoundNameExpressionNode name,
            BoundParameterListNode parameters,
            BoundStatementNode body
    ) {
        this(isAsync, functionType, typeNode, name, parameters, node.arrow, body, node.getRange());
    }

    public BoundClassMethodNode(
            boolean isAsync,
            SMethodFunction functionType,
            BoundTypeNode typeNode,
            BoundNameExpressionNode name,
            BoundParameterListNode parameters,
            Token arrow,
            BoundStatementNode body,
            TextRange range
    ) {
        super(BoundNodeType.CLASS_METHOD, range);
        this.isAsync = isAsync;
        this.functionType = functionType;
        this.typeNode = typeNode;
        this.name = name;
        this.parameters = parameters;
        this.arrow = arrow;
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
}
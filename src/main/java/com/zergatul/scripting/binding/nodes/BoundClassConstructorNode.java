package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.ClassConstructorNode;
import com.zergatul.scripting.symbols.LiftedVariable;
import com.zergatul.scripting.type.SMethodFunction;

import java.util.List;

public class BoundClassConstructorNode extends BoundClassMemberNode {

    public final ClassConstructorNode syntaxNode;
    public final SMethodFunction functionType;
    public final BoundParameterListNode parameters;
    public final BoundStatementNode body;
    public final List<LiftedVariable> lifted;

    public BoundClassConstructorNode(
            ClassConstructorNode node,
            SMethodFunction functionType,
            BoundParameterListNode parameters,
            BoundStatementNode body,
            List<LiftedVariable> lifted
    ) {
        this(node, functionType, parameters, body, lifted, node.getRange());
    }

    public BoundClassConstructorNode(
            ClassConstructorNode node,
            SMethodFunction functionType,
            BoundParameterListNode parameters,
            BoundStatementNode body,
            List<LiftedVariable> lifted,
            TextRange range
    ) {
        super(BoundNodeType.CLASS_CONSTRUCTOR, range);
        this.syntaxNode = node;
        this.functionType = functionType;
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
        parameters.accept(visitor);
        body.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(parameters, body);
    }
}

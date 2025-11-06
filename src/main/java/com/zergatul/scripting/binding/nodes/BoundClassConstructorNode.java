package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.ClassConstructorNode;
import com.zergatul.scripting.symbols.LiftedVariable;
import com.zergatul.scripting.type.ConstructorReference;
import com.zergatul.scripting.type.SMethodFunction;

import java.util.List;

public class BoundClassConstructorNode extends BoundClassMemberNode {

    public final ClassConstructorNode syntaxNode;
    public final SMethodFunction functionType;
    public final ConstructorReference constructor;
    public final BoundParameterListNode parameters;
    public final BoundConstructorInitializerNode initializer;
    public final BoundStatementNode body;
    public final List<LiftedVariable> lifted;

    public BoundClassConstructorNode(
            ClassConstructorNode node,
            SMethodFunction functionType,
            ConstructorReference constructor,
            BoundParameterListNode parameters,
            BoundConstructorInitializerNode initializer,
            BoundStatementNode body,
            List<LiftedVariable> lifted
    ) {
        this(node, functionType, constructor, parameters, initializer, body, lifted, node.getRange());
    }

    public BoundClassConstructorNode(
            ClassConstructorNode node,
            SMethodFunction functionType,
            ConstructorReference constructor,
            BoundParameterListNode parameters,
            BoundConstructorInitializerNode initializer,
            BoundStatementNode body,
            List<LiftedVariable> lifted,
            TextRange range
    ) {
        super(BoundNodeType.CLASS_CONSTRUCTOR, range);
        this.syntaxNode = node;
        this.functionType = functionType;
        this.constructor = constructor;
        this.parameters = parameters;
        this.initializer = initializer;
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
        initializer.accept(visitor);
        body.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        if (initializer.getRange() != TextRange.MISSING) {
            return List.of(parameters, initializer, body);
        } else {
            return List.of(parameters, body);
        }
    }
}

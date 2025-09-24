package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.nodes.FunctionNode;
import com.zergatul.scripting.parser.nodes.ModifiersNode;
import com.zergatul.scripting.symbols.LiftedVariable;

import java.util.List;

public class BoundFunctionNode extends BoundCompilationUnitMemberNode {

    public final ModifiersNode modifiers;
    public final BoundTypeNode returnType;
    public final BoundNameExpressionNode name;
    public final BoundParameterListNode parameters;
    public final Token arrow;
    public final BoundStatementNode body;
    public final List<LiftedVariable> lifted;

    public BoundFunctionNode(
            FunctionNode node,
            BoundTypeNode returnType,
            BoundNameExpressionNode name,
            BoundParameterListNode parameters,
            BoundStatementNode body,
            List<LiftedVariable> lifted
    ) {
        this(node.modifiers, returnType, name, parameters, node.arrow, body, lifted, node.getRange());
    }

    public BoundFunctionNode(
            ModifiersNode modifiers,
            BoundTypeNode returnType,
            BoundNameExpressionNode name,
            BoundParameterListNode parameters,
            Token arrow,
            BoundStatementNode body,
            List<LiftedVariable> lifted,
            TextRange range
    ) {
        super(BoundNodeType.FUNCTION, range);
        this.modifiers = modifiers;
        this.returnType = returnType;
        this.name = name;
        this.parameters = parameters;
        this.arrow = arrow;
        this.body = body;
        this.lifted = lifted;
    }

    public boolean isAsync() {
        return modifiers.isAsync();
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
}
package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeRewriter;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.SFunction;

import java.util.List;

public class BoundClassConstructorNode extends BoundClassMemberNode {

    public final SFunction functionType;
    public final BoundParameterListNode parameters;
    public final BoundBlockStatementNode body;

    public BoundClassConstructorNode(SFunction functionType, BoundParameterListNode parameters, BoundBlockStatementNode body, TextRange range) {
        super(NodeType.CLASS_CONSTRUCTOR, range);
        this.functionType = functionType;
        this.parameters = parameters;
        this.body = body;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public BoundNode accept(BinderTreeRewriter rewriter) {
        return rewriter.visit(this);
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

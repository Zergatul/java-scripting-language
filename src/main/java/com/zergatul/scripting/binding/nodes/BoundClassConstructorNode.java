package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.nodes.ClassConstructorNode;
import com.zergatul.scripting.type.SMethodFunction;

import java.util.List;

public class BoundClassConstructorNode extends BoundClassMemberNode {

    public final Token keyword;
    public final SMethodFunction functionType;
    public final BoundParameterListNode parameters;
    public final Token arrow;
    public final BoundStatementNode body;

    public BoundClassConstructorNode(
            ClassConstructorNode node,
            SMethodFunction functionType,
            BoundParameterListNode parameters,
            BoundStatementNode body
    ) {
        this(node.keyword, functionType, parameters, node.arrow, body, node.getRange());
    }

    public BoundClassConstructorNode(
            Token keyword,
            SMethodFunction functionType,
            BoundParameterListNode parameters,
            Token arrow,
            BoundStatementNode body,
            TextRange range
    ) {
        super(BoundNodeType.CLASS_CONSTRUCTOR, range);
        this.keyword = keyword;
        this.functionType = functionType;
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
        parameters.accept(visitor);
        body.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(parameters, body);
    }
}

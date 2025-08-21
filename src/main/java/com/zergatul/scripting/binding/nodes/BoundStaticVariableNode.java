package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.nodes.StaticVariableNode;

import java.util.List;

public class BoundStaticVariableNode extends BoundCompilationUnitMemberNode {

    public final Token keyword;
    public final BoundTypeNode type;
    public final BoundNameExpressionNode name;
    public final Token equal;
    public final BoundExpressionNode expression;

    public BoundStaticVariableNode(BoundTypeNode type, BoundNameExpressionNode name, BoundExpressionNode expression, StaticVariableNode node) {
        this(node.keyword, type, name, node.equal, expression, node.getRange());
    }

    public BoundStaticVariableNode(Token keyword, BoundTypeNode type, BoundNameExpressionNode name, Token equal, BoundExpressionNode expression, TextRange range) {
        super(NodeType.STATIC_VARIABLE, range);
        this.keyword = keyword;
        this.type = type;
        this.name = name;
        this.equal = equal;
        this.expression = expression;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        type.accept(visitor);
        name.accept(visitor);
        if (expression != null) {
            expression.accept(visitor);
        }
    }

    @Override
    public boolean isOpen() {
        return (expression != null && expression.isMissing()) || name.isMissing();
    }

    @Override
    public List<BoundNode> getChildren() {
        if (expression != null) {
            return List.of(type, name, expression);
        } else {
            return List.of(type, name);
        }
    }
}
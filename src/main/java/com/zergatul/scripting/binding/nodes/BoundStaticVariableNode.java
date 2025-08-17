package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeRewriter;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundStaticVariableNode extends BoundCompilationUnitMemberNode {

    public final Token keyword;
    public final BoundTypeNode type;
    public final BoundNameExpressionNode name;
    public final BoundExpressionNode expression;

    public BoundStaticVariableNode(Token keyword, BoundTypeNode type, BoundNameExpressionNode name, BoundExpressionNode expression, TextRange range) {
        super(NodeType.STATIC_VARIABLE, range);
        this.keyword = keyword;
        this.type = type;
        this.name = name;
        this.expression = expression;
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
        type.accept(visitor);
        name.accept(visitor);
        if (expression != null) {
            expression.accept(visitor);
        }
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(type, name, expression);
    }
}
package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.ValueToken;
import com.zergatul.scripting.parser.nodes.Integer64LiteralExpressionNode;
import com.zergatul.scripting.type.SInt64;

import java.util.List;

public class BoundInteger64LiteralExpressionNode extends BoundExpressionNode {

    public final ValueToken token;
    public final long value;

    public BoundInteger64LiteralExpressionNode(Integer64LiteralExpressionNode literal, long value) {
        this(literal.token, value, literal.getRange());
    }

    public BoundInteger64LiteralExpressionNode(ValueToken token, long value, TextRange range) {
        super(BoundNodeType.INTEGER64_LITERAL, SInt64.instance, range);
        this.token = token;
        this.value = value;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {}

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}
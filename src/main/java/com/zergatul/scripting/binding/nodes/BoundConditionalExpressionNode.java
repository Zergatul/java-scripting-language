package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.nodes.ConditionalExpressionNode;

import java.util.List;

public class BoundConditionalExpressionNode extends BoundExpressionNode {

    public final BoundExpressionNode condition;
    public final Token questionMark;
    public final BoundExpressionNode whenTrue;
    public final Token colon;
    public final BoundExpressionNode whenFalse;

    public BoundConditionalExpressionNode(ConditionalExpressionNode node, BoundExpressionNode condition, BoundExpressionNode whenTrue, BoundExpressionNode whenFalse) {
        this(condition, node.questionMark, whenTrue, node.colon, whenFalse, node.getRange());
    }

    public BoundConditionalExpressionNode(
            BoundExpressionNode condition,
            Token questionMark,
            BoundExpressionNode whenTrue,
            Token colon,
            BoundExpressionNode whenFalse,
            TextRange range
    ) {
        super(BoundNodeType.CONDITIONAL_EXPRESSION, whenTrue.type, range);
        this.condition = condition;
        this.questionMark = questionMark;
        this.whenTrue = whenTrue;
        this.colon = colon;
        this.whenFalse = whenFalse;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        condition.accept(visitor);
        whenTrue.accept(visitor);
        whenFalse.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(condition, whenTrue, whenFalse);
    }
}
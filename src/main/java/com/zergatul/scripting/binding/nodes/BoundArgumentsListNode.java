package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.SyntaxFactory;
import com.zergatul.scripting.parser.nodes.ArgumentsListNode;

import java.util.List;

public class BoundArgumentsListNode extends BoundNode {

    public final ArgumentsListNode syntaxNode;
    public final List<BoundExpressionNode> arguments;

    public BoundArgumentsListNode(List<BoundExpressionNode> arguments) {
        this(SyntaxFactory.missingArgumentList(), arguments, TextRange.MISSING);
    }

    public BoundArgumentsListNode(ArgumentsListNode node, List<BoundExpressionNode> arguments) {
        this(node, arguments, node.getRange());
    }

    public BoundArgumentsListNode(
            ArgumentsListNode node,
            List<BoundExpressionNode> arguments,
            TextRange range
    ) {
        super(BoundNodeType.ARGUMENTS_LIST, range);
        this.syntaxNode = node;
        this.arguments = arguments;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        for (BoundExpressionNode argument : arguments) {
            argument.accept(visitor);
        }
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.copyOf(arguments);
    }
}
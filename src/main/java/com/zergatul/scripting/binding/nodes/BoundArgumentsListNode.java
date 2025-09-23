package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.nodes.ArgumentsListNode;

import java.util.List;
import java.util.Objects;

public class BoundArgumentsListNode extends BoundNode {

    public final Token openParen;
    public final BoundSeparatedList<BoundExpressionNode> arguments;
    public final Token closeParen;

    public BoundArgumentsListNode(BoundSeparatedList<BoundExpressionNode> arguments) {
        this(null, arguments, null, null);
    }

    public BoundArgumentsListNode(ArgumentsListNode node, BoundSeparatedList<BoundExpressionNode> arguments) {
        this(node.openParen, arguments, node.closeParen, node.getRange());
    }

    public BoundArgumentsListNode(Token openParen, BoundSeparatedList<BoundExpressionNode> arguments, Token closeParen, TextRange range) {
        super(BoundNodeType.ARGUMENTS_LIST, range);
        this.openParen = openParen;
        this.arguments = arguments;
        this.closeParen = closeParen;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        for (BoundExpressionNode argument : arguments.getNodes()) {
            argument.accept(visitor);
        }
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.copyOf(arguments.getNodes());
    }
}
package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.MemberAccessExpressionNode;
import com.zergatul.scripting.parser.nodes.NameExpressionNode;
import com.zergatul.scripting.parser.nodes.ParserNode;
import com.zergatul.scripting.parser.nodes.ParserNodeType;
import com.zergatul.scripting.type.MethodReference;
import com.zergatul.scripting.type.SMethodGroup;

import java.util.List;

public class BoundMethodGroupExpressionNode extends BoundExpressionNode {

    public final ParserNode syntaxNode;
    public final BoundExpressionNode callee;
    public final List<MethodReference> candidates;
    public final BoundUnresolvedMethodNode method;

    public BoundMethodGroupExpressionNode(ParserNode node, BoundExpressionNode callee, List<MethodReference> candidates, BoundUnresolvedMethodNode method) {
        this(node, callee, candidates, method, node.getRange());
    }

    public BoundMethodGroupExpressionNode(ParserNode node, BoundExpressionNode callee, List<MethodReference> candidates, BoundUnresolvedMethodNode method, TextRange range) {
        super(BoundNodeType.METHOD_GROUP, new SMethodGroup(), range);

        if (node.isNot(ParserNodeType.NAME_EXPRESSION) && node.isNot(ParserNodeType.MEMBER_ACCESS_EXPRESSION)) {
            throw new InternalException();
        }

        this.syntaxNode = node;
        this.callee = callee;
        this.candidates = candidates;
        this.method = method;
    }

    public NameExpressionNode getNameSyntaxNode() {
        if (syntaxNode.is(ParserNodeType.NAME_EXPRESSION)) {
            return (NameExpressionNode) syntaxNode;
        }
        return ((MemberAccessExpressionNode) syntaxNode).name;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {

    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {

    }

    @Override
    public List<BoundNode> getChildren() {
        if (callee == null) {
            return List.of(method);
        } else {
            return List.of(callee, method);
        }
    }
}
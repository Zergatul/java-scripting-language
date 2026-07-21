package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.MemberAccessExpressionNode;
import com.zergatul.scripting.parser.nodes.ParserNodeType;
import com.zergatul.scripting.type.MethodReference;
import com.zergatul.scripting.type.SMethodGroup;

import java.util.List;

public class BoundMethodGroupExpressionNode extends BoundExpressionNode {

    public final MemberAccessExpressionNode syntaxNode;
    public final BoundExpressionNode callee;
    public final List<MethodReference> candidates;
    public final BoundUnresolvedMethodNode method;
    public final BoundCallTarget.AccessStrategy access;

    public BoundMethodGroupExpressionNode(
            MemberAccessExpressionNode node,
            BoundExpressionNode callee,
            List<MethodReference> candidates,
            BoundUnresolvedMethodNode method,
            BoundCallTarget.AccessStrategy access
    ) {
        this(node, callee, candidates, method, access, node.getRange());
    }

    public BoundMethodGroupExpressionNode(
            MemberAccessExpressionNode node,
            BoundExpressionNode callee,
            List<MethodReference> candidates,
            BoundUnresolvedMethodNode method,
            BoundCallTarget.AccessStrategy access,
            TextRange range
    ) {
        super(BoundNodeType.METHOD_GROUP, new SMethodGroup(), range);

        if (node.isNot(ParserNodeType.NAME_EXPRESSION) && node.isNot(ParserNodeType.MEMBER_ACCESS_EXPRESSION)) {
            throw new InternalException();
        }

        this.syntaxNode = node;
        this.callee = callee;
        this.candidates = candidates;
        this.method = method;
        this.access = access;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {

    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {

    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(callee, method);
    }
}
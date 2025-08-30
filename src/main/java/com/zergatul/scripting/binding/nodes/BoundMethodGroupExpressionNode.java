package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.type.MethodReference;
import com.zergatul.scripting.type.SMethodGroup;
import com.zergatul.scripting.type.SUnknown;

import java.util.List;

public class BoundMethodGroupExpressionNode extends BoundExpressionNode {

    public final BoundExpressionNode callee;
    public final Token dot;
    public final List<MethodReference> candidates;
    public final BoundUnresolvedMethodNode method;

    public BoundMethodGroupExpressionNode(BoundExpressionNode callee, Token dot, List<MethodReference> candidates, BoundUnresolvedMethodNode method, TextRange range) {
        super(NodeType.METHOD_GROUP, new SMethodGroup(), range);
        this.callee = callee;
        this.dot = dot;
        this.candidates = candidates;
        this.method = method;
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
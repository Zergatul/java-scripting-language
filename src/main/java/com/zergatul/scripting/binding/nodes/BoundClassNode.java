package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.nodes.ClassNode;

import java.util.ArrayList;
import java.util.List;

public class BoundClassNode extends BoundCompilationUnitMemberNode {

    public final Token keyword;
    public final BoundNameExpressionNode name;
    public final Token openBrace;
    public final List<BoundClassMemberNode> members;
    public final Token closeBrace;

    public BoundClassNode(ClassNode node, BoundNameExpressionNode name, List<BoundClassMemberNode> members) {
        this(node.keyword, name, node.openBrace, members, node.closeBrace, node.getRange());
    }

    public BoundClassNode(
            Token keyword,
            BoundNameExpressionNode name,
            Token openBrace,
            List<BoundClassMemberNode> members,
            Token closeBrace,
            TextRange range
    ) {
        super(BoundNodeType.CLASS_DECLARATION, range);
        this.keyword = keyword;
        this.name = name;
        this.openBrace = openBrace;
        this.members = members;
        this.closeBrace = closeBrace;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        name.accept(visitor);
        for (BoundClassMemberNode member : members) {
            member.accept(visitor);
        }
    }

    @Override
    public List<BoundNode> getChildren() {
        List<BoundNode> list = new ArrayList<>(1 + members.size());
        list.add(name);
        list.addAll(members);
        return list;
    }
}
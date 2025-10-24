package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.ClassNode;
import com.zergatul.scripting.type.ConstructorReference;
import com.zergatul.scripting.type.SDeclaredType;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BoundClassNode extends BoundCompilationUnitMemberNode {

    public final ClassNode syntaxNode;
    public final BoundNameExpressionNode name;
    @Nullable public final BoundTypeNode baseTypeNode;
    public final List<BoundClassMemberNode> members;
    @Nullable public ConstructorReference defaultBaseConstructor;

    public BoundClassNode(
            ClassNode node,
            BoundNameExpressionNode name,
            @Nullable BoundTypeNode baseTypeNode,
            List<BoundClassMemberNode> members,
            @Nullable ConstructorReference defaultBaseConstructor
    ) {
        this(node, name, baseTypeNode, members, defaultBaseConstructor, node.getRange());
    }

    public BoundClassNode(
            ClassNode node,
            BoundNameExpressionNode name,
            @Nullable BoundTypeNode baseTypeNode,
            List<BoundClassMemberNode> members,
            @Nullable ConstructorReference defaultBaseConstructor,
            TextRange range
    ) {
        super(BoundNodeType.CLASS_DECLARATION, range);
        this.syntaxNode = node;
        this.name = name;
        this.baseTypeNode = baseTypeNode;
        this.members = members;
        this.defaultBaseConstructor = defaultBaseConstructor;
    }

    public SDeclaredType getDeclaredType() {
        return (SDeclaredType) this.name.type;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        name.accept(visitor);
        if (baseTypeNode != null) {
            baseTypeNode.accept(visitor);
        }
        for (BoundClassMemberNode member : members) {
            member.accept(visitor);
        }
    }

    @Override
    public List<BoundNode> getChildren() {
        List<BoundNode> list = new ArrayList<>(1 + members.size());
        list.add(name);
        if (baseTypeNode != null) {
            list.add(baseTypeNode);
        }
        list.addAll(members);
        return list;
    }

    @Override
    public boolean isOpen() {
        return syntaxNode.closeBrace.isMissing();
    }
}
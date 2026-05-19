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
    public final List<BoundTypeNode> baseTypeNodes;
    public final List<BoundClassMemberNode> members;
    public @Nullable ConstructorReference defaultBaseConstructor;

    public BoundClassNode(
            ClassNode node,
            BoundNameExpressionNode name,
            List<BoundTypeNode> baseTypeNodes,
            List<BoundClassMemberNode> members,
            @Nullable ConstructorReference defaultBaseConstructor
    ) {
        this(node, name, baseTypeNodes, members, defaultBaseConstructor, node.getRange());
    }

    public BoundClassNode(
            ClassNode node,
            BoundNameExpressionNode name,
            List<BoundTypeNode> baseTypeNodes,
            List<BoundClassMemberNode> members,
            @Nullable ConstructorReference defaultBaseConstructor,
            TextRange range
    ) {
        super(BoundNodeType.CLASS_DECLARATION, range);
        this.syntaxNode = node;
        this.name = name;
        this.baseTypeNodes = baseTypeNodes;
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
        for (BoundTypeNode baseTypeNode : baseTypeNodes) {
            baseTypeNode.accept(visitor);
        }
        for (BoundClassMemberNode member : members) {
            member.accept(visitor);
        }
    }

    @Override
    public List<BoundNode> getChildren() {
        List<BoundNode> list = new ArrayList<>(1 + baseTypeNodes.size() + members.size());
        list.add(name);
        list.addAll(baseTypeNodes);
        list.addAll(members);
        return list;
    }

    @Override
    public boolean isOpen() {
        return syntaxNode.closeBrace.isMissing();
    }
}
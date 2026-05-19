package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.lexer.ValueToken;
import com.zergatul.scripting.parser.ParserTreeVisitor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ClassNode extends CompilationUnitMemberNode {

    public final Token keyword;
    public final NameExpressionNode name;
    public final @Nullable Token colon;
    public final SeparatedList<TypeNode> baseTypeNodes;
    public final Token openBrace;
    public final List<ClassMemberNode> members;
    public final Token closeBrace;

    public ClassNode(
            Token keyword,
            ValueToken identifier,
            Token openBrace,
            List<ClassMemberNode> members,
            Token closeBrace
    ) {
        this(keyword, identifier, null, SeparatedList.of(), openBrace, members, closeBrace);
    }

    public ClassNode(
            Token keyword,
            ValueToken identifier,
            @Nullable Token colon,
            SeparatedList<TypeNode> baseTypeNodes,
            Token openBrace,
            List<ClassMemberNode> members,
            Token closeBrace
    ) {
        super(ParserNodeType.CLASS_DECLARATION, TextRange.combine(keyword, closeBrace));
        this.keyword = keyword;
        this.name = new NameExpressionNode(identifier);
        this.colon = colon;
        this.baseTypeNodes = baseTypeNodes;
        this.openBrace = openBrace;
        this.members = members;
        this.closeBrace = closeBrace;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        name.accept(visitor);
        for (TypeNode baseTypeNode : baseTypeNodes.getNodes()) {
            baseTypeNode.accept(visitor);
        }
        for (ClassMemberNode member : members) {
            member.accept(visitor);
        }
    }

    @Override
    public List<Locatable> getChildNodes() {
        List<Locatable> nodes = new ArrayList<>();
        nodes.add(keyword);
        nodes.add(name);
        if (colon != null) {
            nodes.add(colon);
        }
        nodes.addAll(baseTypeNodes.getChildNodes());
        nodes.add(openBrace);
        nodes.addAll(members);
        nodes.add(closeBrace);
        return nodes;
    }
}
package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.IdentifierToken;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class ClassNode extends CompilationUnitMemberNode {

    public final NameExpressionNode name;
    public final List<ClassMemberNode> members;

    public ClassNode(IdentifierToken identifier, TextRange range) {
        this(identifier, List.of(), range);
    }

    public ClassNode(IdentifierToken identifier, List<ClassMemberNode> members, TextRange range) {
        super(NodeType.CLASS, range);
        this.name = new NameExpressionNode(identifier);
        this.members = members;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        name.accept(visitor);
        for (ClassMemberNode member : members) {
            member.accept(visitor);
        }
    }
}
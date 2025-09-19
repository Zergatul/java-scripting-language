package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.NodeType;
import com.zergatul.scripting.lexer.ValueToken;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class ClassNode extends CompilationUnitMemberNode {

    public final NameExpressionNode name;
    public final List<ClassMemberNode> members;

    public ClassNode(ValueToken identifier, TextRange range) {
        this(identifier, List.of(), range);
    }

    public ClassNode(ValueToken identifier, List<ClassMemberNode> members, TextRange range) {
        super(NodeType.CLASS_DECLARATION, range);
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
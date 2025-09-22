package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class ClassFieldNode extends ClassMemberNode {

    public final TypeNode type;
    public final NameExpressionNode name;
    public final Token semicolon;

    public ClassFieldNode(TypeNode type, NameExpressionNode name, Token semicolon, TextRange range) {
        super(ParserNodeType.CLASS_FIELD, range);
        this.type = type;
        this.name = name;
        this.semicolon = semicolon;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        type.accept(visitor);
        name.accept(visitor);
    }
}
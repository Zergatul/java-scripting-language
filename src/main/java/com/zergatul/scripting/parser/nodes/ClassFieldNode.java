package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class ClassFieldNode extends ClassMemberNode {

    public final TypeNode type;
    public final NameExpressionNode name;

    public ClassFieldNode(TypeNode type, NameExpressionNode name, TextRange range) {
        super(ParserNodeType.CLASS_FIELD, range);
        this.type = type;
        this.name = name;
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
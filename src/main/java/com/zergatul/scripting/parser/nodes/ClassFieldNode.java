package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class ClassFieldNode extends ClassMemberNode {

    public final ModifiersNode modifiers;
    public final TypeNode type;
    public final NameExpressionNode name;
    public final Token semicolon;

    public ClassFieldNode(ModifiersNode modifiers, TypeNode type, NameExpressionNode name, Token semicolon) {
        super(ParserNodeType.CLASS_FIELD, TextRange.combine(modifiers, semicolon));
        this.modifiers = modifiers;
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
        modifiers.accept(visitor);
        type.accept(visitor);
        name.accept(visitor);
    }

    @Override
    public List<Locatable> getChildNodes() {
        return List.of(modifiers, type, name, semicolon);
    }
}
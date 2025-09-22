package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class ClassMethodNode extends ClassMemberNode {

    public final ModifiersNode modifiers;
    public final TypeNode type;
    public final NameExpressionNode name;
    public final ParameterListNode parameters;
    public final Token arrow;
    public final StatementNode body;

    public ClassMethodNode(
            ModifiersNode modifiers,
            TypeNode type,
            NameExpressionNode name,
            ParameterListNode parameters,
            Token arrow,
            StatementNode body,
            TextRange range
    ) {
        super(ParserNodeType.CLASS_METHOD, range);
        this.modifiers = modifiers;
        this.type = type;
        this.name = name;
        this.parameters = parameters;
        this.arrow = arrow;
        this.body = body;
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
        parameters.accept(visitor);
        body.accept(visitor);
    }
}
package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ClassMethodNode extends ClassMemberNode {

    public final ModifiersNode modifiers;
    public final TypeNode type;
    public final NameExpressionNode name;
    public final ParameterListNode parameters;
    @Nullable public final Token arrow;
    public final StatementNode body;

    public ClassMethodNode(
            ModifiersNode modifiers,
            TypeNode type,
            NameExpressionNode name,
            ParameterListNode parameters,
            @Nullable Token arrow,
            StatementNode body
    ) {
        super(ParserNodeType.CLASS_METHOD, TextRange.combine(modifiers, body));
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

    @Override
    public List<Locatable> getChildNodes() {
        List<Locatable> nodes = new ArrayList<>();
        nodes.add(modifiers);
        nodes.add(type);
        nodes.add(parameters);
        if (arrow != null) {
            nodes.add(arrow);
        }
        nodes.add(body);
        return nodes;
    }
}
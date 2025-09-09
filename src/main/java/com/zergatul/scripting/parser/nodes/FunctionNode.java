package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class FunctionNode extends CompilationUnitMemberNode {

    public final ModifiersNode modifiers;
    public final TypeNode returnType;
    public final NameExpressionNode name;
    public final ParameterListNode parameters;
    @Nullable
    public final Token arrow;
    public final StatementNode body;

    public FunctionNode(
            ModifiersNode modifiers,
            TypeNode returnType,
            NameExpressionNode name,
            ParameterListNode parameters,
            @Nullable Token arrow,
            StatementNode body
    ) {
        super(ParserNodeType.FUNCTION, TextRange.combine(modifiers, body));
        this.modifiers = modifiers;
        this.returnType = returnType;
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
        returnType.accept(visitor);
        name.accept(visitor);
        parameters.accept(visitor);
        body.accept(visitor);
    }

    @Override
    public List<Locatable> getChildNodes() {
        List<Locatable> nodes = new ArrayList<>();
        nodes.add(modifiers);
        nodes.add(returnType);
        nodes.add(name);
        nodes.add(parameters);
        if (arrow != null) {
            nodes.add(arrow);
        }
        nodes.add(body);
        return nodes;
    }
}
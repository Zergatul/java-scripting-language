package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class FunctionNode extends CompilationUnitMemberNode {

    public final ModifiersNode modifiers;
    public final TypeNode returnType;
    public final NameExpressionNode name;
    public final ParameterListNode parameters;
    public final Token arrow;
    public final StatementNode body;

    public FunctionNode(
            ModifiersNode modifiers,
            TypeNode returnType,
            NameExpressionNode name,
            ParameterListNode parameters,
            Token arrow,
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
}
package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class FunctionNode extends CompilationUnitMemberNode {

    public final ModifiersNode modifiers;
    public final TypeNode returnType;
    public final NameExpressionNode name;
    public final ParameterListNode parameters;
    public final StatementNode body;

    public FunctionNode(ModifiersNode modifiers, TypeNode returnType, NameExpressionNode name, ParameterListNode parameters, StatementNode body, TextRange range) {
        super(ParserNodeType.FUNCTION, range);
        this.modifiers = modifiers;
        this.returnType = returnType;
        this.name = name;
        this.parameters = parameters;
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
    public boolean equals(Object obj) {
        if (obj instanceof FunctionNode other) {
            return  other.modifiers.equals(modifiers) &&
                    other.returnType.equals(returnType) &&
                    other.name.equals(name) &&
                    other.parameters.equals(parameters) &&
                    other.body.equals(body) &&
                    other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}
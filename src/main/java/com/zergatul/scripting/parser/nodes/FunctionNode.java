package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.Objects;

public class FunctionNode extends CompilationUnitMemberNode {

    public final Token asyncToken;
    public final TypeNode returnType;
    public final NameExpressionNode name;
    public final ParameterListNode parameters;
    public final BlockStatementNode body;

    public FunctionNode(Token asyncToken, TypeNode returnType, NameExpressionNode name, ParameterListNode parameters, BlockStatementNode body, TextRange range) {
        super(NodeType.FUNCTION, range);
        this.asyncToken = asyncToken;
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
        returnType.accept(visitor);
        name.accept(visitor);
        parameters.accept(visitor);
        body.accept(visitor);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FunctionNode other) {
            return  Objects.equals(other.asyncToken, asyncToken) &&
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
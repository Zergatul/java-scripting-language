package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

import java.util.Objects;

public class VariableDeclarationNode extends StatementNode {

    public final TypeNode type;
    public final NameExpressionNode name;
    public final ExpressionNode expression;

    public VariableDeclarationNode(TypeNode type, NameExpressionNode name, TextRange range) {
        this(type, name, null, range);
    }

    public VariableDeclarationNode(TypeNode type, NameExpressionNode name, ExpressionNode expression, TextRange range) {
        super(NodeType.VARIABLE_DECLARATION, range);
        this.type = type;
        this.name = name;
        this.expression = expression;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof VariableDeclarationNode other) {
            return  other.type.equals(type) &&
                    other.name.equals(name) &&
                    Objects.equals(other.expression, expression) &&
                    other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}
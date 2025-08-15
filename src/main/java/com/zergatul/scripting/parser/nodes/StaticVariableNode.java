package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class StaticVariableNode extends CompilationUnitMemberNode {

    public final Token keyword;
    public final TypeNode type;
    public final NameExpressionNode name;
    public final ExpressionNode expression;

    public StaticVariableNode(Token keyword, TypeNode type, NameExpressionNode name, ExpressionNode expression, TextRange range) {
        super(NodeType.STATIC_VARIABLE, range);
        this.keyword = keyword;
        this.type = type;
        this.name = name;
        this.expression = expression;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        type.accept(visitor);
        name.accept(visitor);
        if (expression != null) {
            expression.accept(visitor);
        }
    }
}
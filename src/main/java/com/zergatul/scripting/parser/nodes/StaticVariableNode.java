package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class StaticVariableNode extends CompilationUnitMemberNode {

    public final Token keyword;
    public final TypeNode type;
    public final NameExpressionNode name;
    @Nullable
    public final Token equal;
    @Nullable
    public final ExpressionNode expression;
    public final Token semicolon;

    public StaticVariableNode(
            Token keyword,
            TypeNode type,
            NameExpressionNode name,
            @Nullable Token equal,
            @Nullable ExpressionNode expression,
            Token semicolon
    ) {
        super(ParserNodeType.STATIC_VARIABLE, TextRange.combine(keyword, semicolon));

        assert (equal != null) == (expression != null);

        this.keyword = keyword;
        this.type = type;
        this.name = name;
        this.equal = equal;
        this.expression = expression;
        this.semicolon = semicolon;
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

    @Override
    public List<Locatable> getChildNodes() {
        List<Locatable> nodes = new ArrayList<>();
        nodes.add(keyword);
        nodes.add(type);
        nodes.add(name);
        if (equal != null) {
            nodes.add(equal);
        }
        if (expression != null) {
            nodes.add(expression);
        }
        nodes.add(semicolon);
        return nodes;
    }
}
package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.lexer.ValueToken;
import com.zergatul.scripting.parser.ParserTreeVisitor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ClassOperatorOverloadNode extends ClassMemberNode {

    public final ValueToken keyword;
    public final Token openBracket;
    public final Token operator;
    public final Token closeBracket;
    public final TypeNode returnType;
    public final ParameterListNode parameters;
    public final @Nullable Token arrow;
    public final StatementNode body;

    public ClassOperatorOverloadNode(
            ValueToken keyword,
            Token openBracket,
            Token operator,
            Token closeBracket,
            TypeNode returnType,
            ParameterListNode parameters,
            @Nullable Token arrow,
            StatementNode body
    ) {
        super(ParserNodeType.CLASS_OPERATOR_OVERLOAD, TextRange.combine(keyword, body));
        this.keyword = keyword;
        this.openBracket = openBracket;
        this.operator = operator;
        this.closeBracket = closeBracket;
        this.returnType = returnType;
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
        returnType.accept(visitor);
        parameters.accept(visitor);
        body.accept(visitor);
    }

    @Override
    public List<Locatable> getChildNodes() {
        List<Locatable> nodes = new ArrayList<>();
        nodes.add(returnType);
        nodes.add(keyword);
        nodes.add(operator);
        nodes.add(parameters);
        if (arrow != null) {
            nodes.add(arrow);
        }
        nodes.add(body);
        return nodes;
    }
}

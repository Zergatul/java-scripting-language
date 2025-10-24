package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ClassConstructorNode extends ClassMemberNode {

    public final Token keyword;
    public final ParameterListNode parameters;
    @Nullable public final Token colon;
    @Nullable public final ConstructorInitializerNode initializer;
    @Nullable public final Token arrow;
    public final StatementNode body;

    public ClassConstructorNode(
            Token keyword,
            ParameterListNode parameters,
            @Nullable Token colon,
            @Nullable ConstructorInitializerNode initializer,
            @Nullable Token arrow,
            StatementNode body,
            TextRange range
    ) {
        super(ParserNodeType.CLASS_CONSTRUCTOR, range);
        this.keyword = keyword;
        this.parameters = parameters;
        this.colon = colon;
        this.initializer = initializer;
        this.arrow = arrow;
        this.body = body;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        parameters.accept(visitor);
        if (initializer != null) {
            initializer.accept(visitor);
        }
        body.accept(visitor);
    }

    @Override
    public List<Locatable> getChildNodes() {
        List<Locatable> nodes = new ArrayList<>();
        nodes.add(keyword);
        nodes.add(parameters);
        if (colon != null) {
            nodes.add(colon);
        }
        if (initializer != null) {
            nodes.add(initializer);
        }
        if (arrow != null) {
            nodes.add(arrow);
        }
        nodes.add(body);
        return nodes;
    }
}
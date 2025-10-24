package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class ConstructorInitializerNode extends ParserNode {

    public final Token keyword;
    public final ArgumentsListNode arguments;

    public ConstructorInitializerNode(Token keyword, ArgumentsListNode arguments) {
        this(keyword, arguments, TextRange.combine(keyword, arguments));
    }

    public ConstructorInitializerNode(Token keyword, ArgumentsListNode arguments, TextRange range) {
        super(ParserNodeType.CONSTRUCTOR_INITIALIZER, range);
        this.keyword = keyword;
        this.arguments = arguments;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        arguments.accept(visitor);
    }

    @Override
    public List<Locatable> getChildNodes() {
        return List.of(keyword, arguments);
    }
}
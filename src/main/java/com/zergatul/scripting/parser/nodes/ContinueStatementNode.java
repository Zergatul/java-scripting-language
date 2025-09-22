package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class ContinueStatementNode extends StatementNode {

    public final Token keyword;
    public final Token semicolon;

    public ContinueStatementNode(Token keyword, Token semicolon) {
        super(ParserNodeType.CONTINUE_STATEMENT,  TextRange.combine(keyword, semicolon));
        this.keyword = keyword;
        this.semicolon = semicolon;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {}
}
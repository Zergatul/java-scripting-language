package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class BreakStatementNode extends StatementNode {

    public final Token keyword;
    public final Token semicolon;

    public BreakStatementNode(Token keyword, Token semicolon, TextRange range) {
        super(ParserNodeType.BREAK_STATEMENT, range);
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
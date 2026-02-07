package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class FinallyClauseNode extends ParserNode {

    public final Token keyword;
    public final BlockStatementNode block;

    public FinallyClauseNode(Token keyword, BlockStatementNode block) {
        super(ParserNodeType.FINALLY_CLAUSE, TextRange.combine(keyword, block));
        this.keyword = keyword;
        this.block = block;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        block.accept(visitor);
    }

    @Override
    public List<Locatable> getChildNodes() {
        return List.of(keyword, block);
    }
}
package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TryStatementNode extends StatementNode {

    public final Token keyword;
    public final BlockStatementNode block;
    public final @Nullable CatchClauseNode catchClause;
    public final @Nullable FinallyClauseNode finallyClause;

    public TryStatementNode(
            Token keyword,
            BlockStatementNode block,
            @Nullable CatchClauseNode catchClause,
            @Nullable FinallyClauseNode finallyClause
    ) {
        super(ParserNodeType.TRY_STATEMENT, calculateRange(keyword, catchClause, finallyClause));
        this.keyword = keyword;
        this.block = block;
        this.catchClause = catchClause;
        this.finallyClause = finallyClause;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        block.accept(visitor);
        if (catchClause != null) {
            catchClause.accept(visitor);
        }
        if (finallyClause != null) {
            finallyClause.accept(visitor);
        }
    }

    @Override
    public List<Locatable> getChildNodes() {
        List<Locatable> children = new ArrayList<>();
        children.add(block);
        if (catchClause != null) {
            children.add(catchClause);
        }
        if (finallyClause != null) {
            children.add(finallyClause);
        }
        return children;
    }

    private static TextRange calculateRange(Token keyword, @Nullable CatchClauseNode catchClause, @Nullable FinallyClauseNode finallyClause) {
        assert catchClause != null || finallyClause != null;

        return TextRange.combine(keyword, Objects.requireNonNullElse(finallyClause, catchClause));
    }
}
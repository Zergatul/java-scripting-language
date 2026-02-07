package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CatchClauseNode extends ParserNode {

    public final Token keyword;
    public final @Nullable CatchDeclarationNode declaration;
    public final BlockStatementNode block;

    public CatchClauseNode(Token keyword, @Nullable CatchDeclarationNode declaration, BlockStatementNode block) {
        super(ParserNodeType.CATCH_CLAUSE, TextRange.combine(keyword, block));
        this.keyword = keyword;
        this.declaration = declaration;
        this.block = block;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        if (declaration != null) {
            declaration.accept(visitor);
        }
        block.accept(visitor);
    }

    @Override
    public List<Locatable> getChildNodes() {
        List<Locatable> children = new ArrayList<>();
        children.add(keyword);
        if (declaration != null) {
            children.add(declaration);
        }
        children.add(block);
        return children;
    }

    @Override
    public boolean isOpen() {
        return block.isOpen();
    }
}
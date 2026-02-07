package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.nodes.TryStatementNode;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BoundTryStatementNode extends BoundStatementNode {

    public final TryStatementNode syntaxNode;
    public final BoundBlockStatementNode block;
    public final @Nullable BoundSymbolNode exceptionSymbol;
    public final @Nullable BoundBlockStatementNode catchBlock;
    public final @Nullable BoundBlockStatementNode finallyBlock;

    public BoundTryStatementNode(
            TryStatementNode node,
            BoundBlockStatementNode block,
            @Nullable BoundSymbolNode exceptionSymbol,
            @Nullable BoundBlockStatementNode catchBlock,
            @Nullable BoundBlockStatementNode finallyBlock
    ) {
        super(BoundNodeType.TRY_STATEMENT, node.getRange());

        assert catchBlock != null || finallyBlock != null;
        assert catchBlock != null || exceptionSymbol == null;

        this.syntaxNode = node;
        this.block = block;
        this.exceptionSymbol = exceptionSymbol;
        this.catchBlock = catchBlock;
        this.finallyBlock = finallyBlock;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        block.accept(visitor);
        if (exceptionSymbol != null) {
            exceptionSymbol.accept(visitor);
        }
        if (catchBlock != null) {
            catchBlock.accept(visitor);
        }
        if (finallyBlock != null) {
            finallyBlock.accept(visitor);
        }
    }

    @Override
    public List<BoundNode> getChildren() {
        List<BoundNode> children = new ArrayList<>();
        children.add(block);
        if (exceptionSymbol != null) {
            children.add(exceptionSymbol);
        }
        if (catchBlock != null) {
            children.add(catchBlock);
        }
        if (finallyBlock != null) {
            children.add(finallyBlock);
        }
        return children;
    }
}
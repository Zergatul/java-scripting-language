package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.generator.StateBoundary;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class BoundGeneratorFinallyEpilogueNode extends BoundStatementNode {

    public final StateBoundary nextState;
    public final @Nullable StateBoundary exceptionState;

    public BoundGeneratorFinallyEpilogueNode(StateBoundary nextState) {
        this(nextState, null);
    }

    public BoundGeneratorFinallyEpilogueNode(StateBoundary nextState, @Nullable StateBoundary exceptionState) {
        super(BoundNodeType.GENERATOR_FINALLY_EPILOGUE, TextRange.MISSING);
        this.nextState = nextState;
        this.exceptionState = exceptionState;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {}

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {}

    @Override
    public List<BoundNode> getChildren() {
        throw new InternalException();
    }
}
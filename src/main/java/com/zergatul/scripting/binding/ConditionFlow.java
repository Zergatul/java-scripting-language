package com.zergatul.scripting.binding;

import com.zergatul.scripting.binding.nodes.BoundExpressionNode;
import com.zergatul.scripting.symbols.SymbolRef;
import com.zergatul.scripting.utility.Lists;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public final class ConditionFlow {

    private final BoundExpressionNode expression;
    private final List<SymbolRef> whenTrueLocals;
    private final List<SymbolRef> whenFalseLocals;
    private final List<SymbolRef> allLocals;

    public ConditionFlow(
            BoundExpressionNode expression,
            List<SymbolRef> whenTrueLocals,
            List<SymbolRef> whenFalseLocals,
            List<SymbolRef> allLocals
    ) {
        this.expression = expression;
        this.whenTrueLocals = whenTrueLocals;
        this.whenFalseLocals = whenFalseLocals;
        this.allLocals = allLocals;
    }

    public ConditionFlow(BoundExpressionNode expression) {
        this(expression, Lists.of(), Lists.of(), Lists.of());
    }

    public BoundExpressionNode expression() {
        return expression;
    }

    public List<SymbolRef> whenTrueLocals() {
        return whenTrueLocals;
    }

    public List<SymbolRef> whenFalseLocals() {
        return whenFalseLocals;
    }

    public List<SymbolRef> allLocals() {
        return allLocals;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        ConditionFlow that = (ConditionFlow) obj;
        return  Objects.equals(this.expression, that.expression) &&
                Objects.equals(this.whenTrueLocals, that.whenTrueLocals) &&
                Objects.equals(this.whenFalseLocals, that.whenFalseLocals) &&
                Objects.equals(this.allLocals, that.allLocals);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression, whenTrueLocals, whenFalseLocals, allLocals);
    }

    @Override
    public String toString() {
        return  "ConditionFlow[" +
                "expression=" + expression + ", " +
                "whenTrueLocals=" + whenTrueLocals + ", " +
                "whenFalseLocals=" + whenFalseLocals + ", " +
                "allLocals=" + allLocals + ']';
    }
}
package com.zergatul.scripting.binding;

import com.zergatul.scripting.binding.nodes.BoundExpressionNode;
import com.zergatul.scripting.symbols.SymbolRef;

import java.util.List;

public record ConditionFlow(
        BoundExpressionNode expression,
        List<SymbolRef> whenTrueLocals,
        List<SymbolRef> whenFalseLocals,
        List<SymbolRef> allLocals
) {
    public ConditionFlow(BoundExpressionNode expression) {
        this(expression, List.of(), List.of(), List.of());
    }
}
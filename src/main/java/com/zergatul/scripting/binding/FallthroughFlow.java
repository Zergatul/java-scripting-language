package com.zergatul.scripting.binding;

import com.zergatul.scripting.symbols.SymbolRef;

import java.util.List;

public record FallthroughFlow(
        List<SymbolRef> whenTrueLocals,
        List<SymbolRef> whenFalseLocals,
        List<SymbolRef> fallthroughLocals,
        List<SymbolRef> allLocals
) {
    public static final FallthroughFlow EMPTY = new FallthroughFlow(List.of(), List.of(), List.of(), List.of());

    public FallthroughFlow(ConditionFlow flow, List<SymbolRef> fallthroughLocals) {
        this(flow.whenTrueLocals(), flow.whenFalseLocals(), fallthroughLocals, flow.allLocals());
    }
}
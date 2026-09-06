package com.zergatul.scripting.binding;

import com.zergatul.scripting.symbols.SymbolRef;
import com.zergatul.scripting.utility.Lists;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public final class FallthroughFlow {

    public static final FallthroughFlow EMPTY = new FallthroughFlow(Lists.of(), Lists.of(), Lists.of(), Lists.of());

    private final List<SymbolRef> whenTrueLocals;
    private final List<SymbolRef> whenFalseLocals;
    private final List<SymbolRef> fallthroughLocals;
    private final List<SymbolRef> allLocals;

    public FallthroughFlow(
            List<SymbolRef> whenTrueLocals,
            List<SymbolRef> whenFalseLocals,
            List<SymbolRef> fallthroughLocals,
            List<SymbolRef> allLocals
    ) {
        this.whenTrueLocals = whenTrueLocals;
        this.whenFalseLocals = whenFalseLocals;
        this.fallthroughLocals = fallthroughLocals;
        this.allLocals = allLocals;
    }

    public FallthroughFlow(ConditionFlow flow, List<SymbolRef> fallthroughLocals) {
        this(flow.whenTrueLocals(), flow.whenFalseLocals(), fallthroughLocals, flow.allLocals());
    }

    public List<SymbolRef> whenTrueLocals() {
        return whenTrueLocals;
    }

    public List<SymbolRef> whenFalseLocals() {
        return whenFalseLocals;
    }

    public List<SymbolRef> fallthroughLocals() {
        return fallthroughLocals;
    }

    public List<SymbolRef> allLocals() {
        return allLocals;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        FallthroughFlow that = (FallthroughFlow) obj;
        return  Objects.equals(this.whenTrueLocals, that.whenTrueLocals) &&
                Objects.equals(this.whenFalseLocals, that.whenFalseLocals) &&
                Objects.equals(this.fallthroughLocals, that.fallthroughLocals) &&
                Objects.equals(this.allLocals, that.allLocals);
    }

    @Override
    public int hashCode() {
        return Objects.hash(whenTrueLocals, whenFalseLocals, fallthroughLocals, allLocals);
    }

    @Override
    public String toString() {
        return  "FallthroughFlow[" +
                "whenTrueLocals=" + whenTrueLocals + ", " +
                "whenFalseLocals=" + whenFalseLocals + ", " +
                "fallthroughLocals=" + fallthroughLocals + ", " +
                "allLocals=" + allLocals + ']';
    }
}
package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.symbols.LiftedVariable;

import java.util.List;

public interface FunctionRootNode {
    List<LiftedVariable> getLifted();
}
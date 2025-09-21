package com.zergatul.scripting.binding;

import com.zergatul.scripting.binding.nodes.BoundTypeNode;

public record StaticVariableDeclaration(String name, BoundTypeNode typeNode, boolean hasError) {}
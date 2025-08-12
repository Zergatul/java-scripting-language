package com.zergatul.scripting.binding;

import com.zergatul.scripting.binding.nodes.BoundTypeNode;
import com.zergatul.scripting.parser.nodes.ClassFieldNode;

public record ClassFieldDeclaration(String name, ClassFieldNode classFieldNode, BoundTypeNode typeNode) {
}

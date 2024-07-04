package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;
import java.util.Objects;

public class StaticVariablesListNode extends Node {

    public final List<VariableDeclarationNode> variables;

    public StaticVariablesListNode(List<VariableDeclarationNode> variables, TextRange range) {
        super(NodeType.STATIC_VARIABLES_LIST, range);
        this.variables = variables;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StaticVariablesListNode other) {
            return Objects.equals(other.variables, variables) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}
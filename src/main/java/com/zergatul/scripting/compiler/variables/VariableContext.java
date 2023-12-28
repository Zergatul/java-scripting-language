package com.zergatul.scripting.compiler.variables;

import java.util.HashMap;
import java.util.Map;

public class VariableContext {

    private final int startIndex;
    private final Map<String, LocalVariableEntry> variables = new HashMap<>();

    public VariableContext(int startIndex) {
        this.startIndex = startIndex;
    }

    public void add(String identifier, LocalVariableEntry entry) {
        if (identifier != null) {
            variables.put(identifier, entry);
        }
    }

    public boolean contains(String identifier) {
        return variables.containsKey(identifier);
    }

    public LocalVariableEntry get(String identifier) {
        return variables.get(identifier);
    }

    public int getStartIndex() {
        return startIndex;
    }
}
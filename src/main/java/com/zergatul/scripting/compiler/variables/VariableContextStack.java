package com.zergatul.scripting.compiler.variables;

import com.zergatul.scripting.compiler.ScriptCompileException;
import com.zergatul.scripting.compiler.types.SFloatType;
import com.zergatul.scripting.compiler.types.SType;

import java.util.*;

public class VariableContextStack {

    private final Map<String, StaticVariableEntry> staticVariables = new HashMap<>();
    private final Map<String, FunctionEntry> functions = new HashMap<>();
    private final Stack<VariableContext> stack = new Stack<>();
    private int index = 2; // index=1 reserved for StringBuilder

    public VariableContextStack() {
        stack.add(new VariableContext(index));
    }

    public LocalVariableEntry addLocal(String identifier, SType type) throws ScriptCompileException {
        if (identifier != null) {
            checkIdentifier(identifier);
        }

        LocalVariableEntry entry = new LocalVariableEntry(type, index);
        stack.peek().add(identifier, entry);

        if (type == SFloatType.instance) {
            index += 2;
        } else {
            index += 1;
        }

        return entry;
    }

    public StaticVariableEntry addStatic(String identifier, SType type, String className) throws ScriptCompileException {
        if (identifier == null) {
            throw new ScriptCompileException("Identifier is required.");
        }

        if (staticVariables.containsKey(identifier)) {
            throw new ScriptCompileException(String.format("Static variable %s is already declared.", identifier));
        }

        StaticVariableEntry entry = new StaticVariableEntry(type, className, identifier);
        staticVariables.put(identifier, entry);
        return entry;
    }

    public FunctionEntry addFunction(String identifier, SType returnType, SType[] arguments) throws ScriptCompileException {
        if (identifier == null) {
            throw new ScriptCompileException("Identifier is required.");
        }

        if (staticVariables.containsKey(identifier)) {
            throw new ScriptCompileException(String.format("Cannot declare function with the same name as static variable %s.", identifier));
        }

        if (functions.containsKey(identifier)) {
            throw new ScriptCompileException(String.format("Function %s is already declared.", identifier));
        }

        throw new ScriptCompileException("Not implemented.");
    }

    public void begin() {
        stack.add(new VariableContext(index));
    }

    public void end() {
        index = stack.pop().getStartIndex();
    }

    public VariableEntry get(String identifier) {
        for (int i = stack.size() - 1; i >= 0; i--) {
            VariableEntry entry = stack.get(i).get(identifier);
            if (entry != null) {
                return entry;
            }
        }

        if (staticVariables.containsKey(identifier)) {
            return staticVariables.get(identifier);
        }

        return null;
    }

    public Collection<StaticVariableEntry> getStaticVariables() {
        return staticVariables.values();
    }

    public VariableContextStack newForLambda() {
        VariableContextStack context = new VariableContextStack();
        for (StaticVariableEntry entry : staticVariables.values()) {
            context.staticVariables.put(entry.getIdentifier(), entry);
        }
        return context;
    }

    private void checkIdentifier(String identifier) throws ScriptCompileException {
        for (int i = stack.size() - 1; i >= 0; i--) {
            if (stack.get(i).contains(identifier)) {
                throw new ScriptCompileException(String.format("Identifier %s is already declared.", identifier));
            }
        }
    }
}
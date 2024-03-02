package com.zergatul.scripting.old.compiler.variables;

import com.zergatul.scripting.old.compiler.ScriptCompileException;
import com.zergatul.scripting.old.compiler.types.SFloatType;
import com.zergatul.scripting.old.compiler.types.SType;

import java.util.*;

public class VariableContextStack {

    private final Map<String, StaticVariableEntry> staticVariables = new HashMap<>();
    private final Map<String, FunctionEntry> functions = new HashMap<>();
    private final Stack<VariableContext> stack = new Stack<>();
    private int index;

    public VariableContextStack(int initialLocalVarIndex) {
        index = initialLocalVarIndex;
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

    public void addFunction(String identifier, SType returnType, SType[] arguments, String className) throws ScriptCompileException {
        if (identifier == null) {
            throw new ScriptCompileException("Identifier is required.");
        }

        if (staticVariables.containsKey(identifier)) {
            throw new ScriptCompileException(String.format("Cannot declare function with the same name as static variable %s.", identifier));
        }

        if (functions.containsKey(identifier)) {
            throw new ScriptCompileException(String.format("Function %s is already declared.", identifier));
        }

        FunctionEntry entry = new FunctionEntry(className, identifier, arguments, returnType);
        functions.put(identifier, entry);
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

    public FunctionEntry getFunction(String identifier) {
        return functions.get(identifier);
    }

    public Collection<StaticVariableEntry> getStaticVariables() {
        return staticVariables.values();
    }

    public VariableContextStack newWithStaticVariables(int initialLocalVarIndex) {
        VariableContextStack context = new VariableContextStack(initialLocalVarIndex);
        for (StaticVariableEntry entry : staticVariables.values()) {
            context.staticVariables.put(entry.getIdentifier(), entry);
        }
        for (FunctionEntry entry : functions.values()) {
            context.functions.put(entry.getIdentifier(), entry);
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
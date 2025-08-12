package com.zergatul.scripting.binding;

import com.zergatul.scripting.parser.nodes.*;
import com.zergatul.scripting.symbols.SymbolRef;

import java.util.HashMap;
import java.util.Map;

public class DeclarationTable {

    private final Map<String, StaticVariableDeclaration> staticVariables = new HashMap<>();
    private final Map<String, FunctionDeclaration> functions = new HashMap<>();
    private final Map<String, ClassDeclaration> classes = new HashMap<>();

    private final Map<StaticFieldNode, StaticVariableDeclaration> staticVariableNodeMap = new HashMap<>();
    private final Map<FunctionNode, FunctionDeclaration> functionNodeMap = new HashMap<>();
    private final Map<ClassNode, ClassDeclaration> classNodeMap = new HashMap<>();

    public void addStaticVariable(StaticFieldNode fieldNode, StaticVariableDeclaration declaration) {
        String name = declaration.name();
        if (!name.isEmpty() && !staticVariables.containsKey(name)) {
            staticVariables.put(name, declaration);
        }
        staticVariableNodeMap.put(fieldNode, declaration);
    }

    public void addFunction(FunctionNode functionNode, FunctionDeclaration declaration) {
        String name = declaration.getName();
        if (!name.isEmpty() && !functions.containsKey(name)) {
            functions.put(name, declaration);
        }
        functionNodeMap.put(functionNode, declaration);
    }

    public void addClass(String name, ClassNode classNode, ClassDeclaration declaration) {
        if (!name.isEmpty() && !classes.containsKey(name)) {
            classes.put(name, declaration);
        }
        classNodeMap.put(classNode, declaration);
    }

    public StaticVariableDeclaration getStaticVariableDeclaration(StaticFieldNode fieldNode) {
        return staticVariableNodeMap.get(fieldNode);
    }

    public FunctionDeclaration getFunctionDeclaration(FunctionNode functionNode) {
        return functionNodeMap.get(functionNode);
    }

    public ClassDeclaration getClassDeclaration(String name) {
        return classes.get(name);
    }

    public ClassDeclaration getClassDeclaration(ClassNode classNode) {
        return classNodeMap.get(classNode);
    }

    public SymbolRef getSymbol(String name) {
        if (functions.containsKey(name)) {
            return functions.get(name).getSymbolRef();
        }
        return null;
    }

    public boolean hasSymbol(String name) {
        return staticVariables.containsKey(name) || functions.containsKey(name) || classes.containsKey(name);
    }
}
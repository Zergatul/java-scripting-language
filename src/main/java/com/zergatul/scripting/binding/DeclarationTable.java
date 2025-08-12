package com.zergatul.scripting.binding;

import com.zergatul.scripting.binding.nodes.BoundParameterListNode;
import com.zergatul.scripting.binding.nodes.BoundTypeNode;
import com.zergatul.scripting.parser.nodes.*;
import com.zergatul.scripting.type.SDeclaredType;
import com.zergatul.scripting.type.SFunction;
import com.zergatul.scripting.type.SType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeclarationTable {

    private final Map<String, StaticVariableDeclaration> staticVariables = new HashMap<>();
    private final Map<String, FunctionDeclaration> functions = new HashMap<>();
    private final Map<String, ClassDeclaration> classes = new HashMap<>();

    private final Map<StaticFieldNode, StaticVariableDeclaration> staticVariableNodeMap = new HashMap<>();
    private final Map<FunctionNode, FunctionDeclaration> functionNodeMap = new HashMap<>();
    private final Map<ClassNode, ClassDeclaration> classNodeMap = new HashMap<>();

    public void addStaticVariable(StaticFieldNode fieldNode, StaticVariableDeclaration declaration) {
        String name = declaration.name;
        if (!name.isEmpty() && !staticVariables.containsKey(name)) {
            staticVariables.put(name, declaration);
        }
        staticVariableNodeMap.put(fieldNode, declaration);
    }

    public void addFunction(FunctionNode functionNode, FunctionDeclaration declaration) {
        String name = declaration.name;
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

    public boolean hasSymbol(String name) {
        return staticVariables.containsKey(name) || functions.containsKey(name) || classes.containsKey(name);
    }

    public record StaticVariableDeclaration(String name, BoundTypeNode typeNode, boolean hasError) {}

    public record FunctionDeclaration(
            String name,
            boolean isAsync,
            BoundTypeNode returnTypeNode,
            BoundParameterListNode parameters,
            SFunction functionType,
            boolean hasError
    ) {
        public SType getReturnType() {
            return returnTypeNode.type;
        }
    }

    public static class ClassDeclaration {

        private final SDeclaredType classType;
        private final List<ClassFieldDeclaration> fields = new ArrayList<>();
        private final List<ClassConstructorDeclaration> constructors = new ArrayList<>();
        private final List<ClassMethodDeclaration> methods = new ArrayList<>();

        public ClassDeclaration(SDeclaredType classType) {
            this.classType = classType;
        }

        public void addField(ClassFieldDeclaration declaration) {
            fields.add(declaration);
        }

        public SDeclaredType classType() {
            return this.classType;
        }

        public boolean hasMember(String name) {
            return fields.stream().anyMatch(f -> f.name.equals(name)) || methods.stream().anyMatch(m -> m.name.equals(name));
        }
    }

    public record ClassFieldDeclaration(String name, ClassFieldNode classFieldNode, BoundTypeNode typeNode) {}

    public record ClassConstructorDeclaration() {}
    public record ClassMethodDeclaration(String name) {}
}
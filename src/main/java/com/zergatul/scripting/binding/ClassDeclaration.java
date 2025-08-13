package com.zergatul.scripting.binding;

import com.zergatul.scripting.binding.nodes.BoundParameterNode;
import com.zergatul.scripting.binding.nodes.BoundTypeNode;
import com.zergatul.scripting.parser.nodes.ClassConstructorNode;
import com.zergatul.scripting.parser.nodes.ClassFieldNode;
import com.zergatul.scripting.parser.nodes.ClassMethodNode;
import com.zergatul.scripting.symbols.ClassSymbol;
import com.zergatul.scripting.symbols.SymbolRef;
import com.zergatul.scripting.type.SDeclaredType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassDeclaration extends Declaration {

    private final SDeclaredType classType;
    private final Map<ClassFieldNode, ClassFieldDeclaration> fieldNodeMap = new HashMap<>();
    private final Map<ClassConstructorNode, ClassConstructorDeclaration> constructorNodeMap = new HashMap<>();
    private final Map<ClassMethodNode, ClassMethodDeclaration> methodNodeMap = new HashMap<>();

    public ClassDeclaration(String name, SymbolRef symbolRef) {
        super(name, symbolRef);
        this.classType = ((ClassSymbol) symbolRef.get()).getDeclaredType();
    }

    public void addField(ClassFieldNode node, ClassFieldDeclaration declaration) {
        fieldNodeMap.put(node, declaration);
    }

    public void addConstructor(ClassConstructorNode node, ClassConstructorDeclaration declaration) {
        constructorNodeMap.put(node, declaration);
    }

    public void addMethod(ClassMethodNode node, ClassMethodDeclaration declaration) {
        methodNodeMap.put(node, declaration);
    }

    public SDeclaredType getDeclaredType() {
        return this.classType;
    }

    public ClassFieldDeclaration getFieldDeclaration(ClassFieldNode node) {
        return fieldNodeMap.get(node);
    }

    public ClassConstructorDeclaration getConstructorDeclaration(ClassConstructorNode node) {
        return constructorNodeMap.get(node);
    }

    public ClassMethodDeclaration getMethodDeclaration(ClassMethodNode node) {
        return methodNodeMap.get(node);
    }

    public boolean hasMember(String name) {
        return hasField(name) || methodNodeMap.values().stream().anyMatch(m -> m.getName().equals(name));
    }

    public boolean hasField(String name) {
        return fieldNodeMap.values().stream().anyMatch(f -> f.getName().equals(name));
    }

    public boolean hasConstructor(List<BoundParameterNode> parameters) {
        for (ClassConstructorDeclaration declaration : constructorNodeMap.values()) {
            if (declaration.getParameters().parameters.size() != parameters.size()) {
                continue;
            }

            boolean sameParameters = true;
            for (int i = 0; i < parameters.size(); i++) {
                if (!parameters.get(i).getType().equals(declaration.getParameters().parameters.get(i).getType())) {
                    sameParameters = false;
                    break;
                }
            }

            if (sameParameters) {
                return true;
            }
        }

        return false;
    }

    public boolean hasMethod(BoundTypeNode typeNode, String name, List<BoundParameterNode> parameters) {
        for (ClassMethodDeclaration declaration : methodNodeMap.values()) {
            if (!name.equals(declaration.getName())) {
                continue;
            }

            if (declaration.getParameters().parameters.size() != parameters.size()) {
                continue;
            }

            if (!typeNode.type.equals(declaration.getTypeNode().type)) {
                continue;
            }

            boolean sameParameters = true;
            for (int i = 0; i < parameters.size(); i++) {
                if (!parameters.get(i).getType().equals(declaration.getParameters().parameters.get(i).getType())) {
                    sameParameters = false;
                    break;
                }
            }

            if (sameParameters) {
                return true;
            }
        }

        return false;
    }
}
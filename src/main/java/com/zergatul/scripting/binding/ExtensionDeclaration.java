package com.zergatul.scripting.binding;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.binding.nodes.BoundParameterNode;
import com.zergatul.scripting.binding.nodes.BoundTypeNode;
import com.zergatul.scripting.parser.nodes.ClassMethodNode;
import com.zergatul.scripting.type.SType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtensionDeclaration {

    private final BoundTypeNode typeNode;
    private final Map<ClassMethodNode, ClassMethodDeclaration> methodNodeMap = new HashMap<>();

    public ExtensionDeclaration(BoundTypeNode typeNode) {
        this.typeNode = typeNode;
    }

    public void addMethod(ClassMethodNode node, ClassMethodDeclaration declaration) {
        methodNodeMap.put(node, declaration);
    }

    public BoundTypeNode getTypeNode() {
        return typeNode;
    }

    public SType getBaseType() {
        return typeNode.type;
    }

    public ClassMethodDeclaration getMethodDeclaration(ClassMethodNode node) {
        ClassMethodDeclaration declaration = methodNodeMap.get(node);
        if (declaration == null) {
            throw new InternalException();
        }
        return declaration;
    }

    public boolean hasMethod(String name, List<BoundParameterNode> parameters) {
        for (ClassMethodDeclaration declaration : methodNodeMap.values()) {
            if (!name.equals(declaration.getName())) {
                continue;
            }

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
}
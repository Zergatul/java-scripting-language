package com.zergatul.scripting.binding;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.binding.nodes.BoundParameterNode;
import com.zergatul.scripting.binding.nodes.BoundTypeNode;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.parser.UnaryOperator;
import com.zergatul.scripting.parser.nodes.ClassMethodNode;
import com.zergatul.scripting.parser.nodes.ClassOperatorOverloadNode;
import com.zergatul.scripting.type.SType;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExtensionDeclaration {

    private final BoundTypeNode typeNode;
    private final Map<ClassMethodNode, ClassMethodDeclaration> methodNodeMap = new HashMap<>();
    private final Map<ClassOperatorOverloadNode, ExtensionUnaryOperationDeclaration> unaryOperationNodeMap = new HashMap<>();
    private final Map<ClassOperatorOverloadNode, ExtensionBinaryOperationDeclaration> binaryOperationNodeMap = new HashMap<>();

    public ExtensionDeclaration(BoundTypeNode typeNode) {
        this.typeNode = typeNode;
    }

    public void addMethod(ClassMethodNode node, ClassMethodDeclaration declaration) {
        methodNodeMap.put(node, declaration);
    }

    public void addUnaryOperation(ClassOperatorOverloadNode node, ExtensionUnaryOperationDeclaration declaration) {
        unaryOperationNodeMap.put(node, declaration);
    }

    public void addBinaryOperation(ClassOperatorOverloadNode node, ExtensionBinaryOperationDeclaration declaration) {
        binaryOperationNodeMap.put(node, declaration);
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

    public @Nullable ExtensionUnaryOperationDeclaration getUnaryOperationDeclaration(ClassOperatorOverloadNode node) {
        return unaryOperationNodeMap.get(node);
    }

    public @Nullable ExtensionBinaryOperationDeclaration getBinaryOperationDeclaration(ClassOperatorOverloadNode node) {
        return binaryOperationNodeMap.get(node);
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

    public boolean hasUnaryOperation(UnaryOperator operator) {
        for (ExtensionUnaryOperationDeclaration declaration : unaryOperationNodeMap.values()) {
            if (declaration.getOperation().getOperator() != operator) {
                continue;
            }

            if (declaration.hasError()) {
                continue;
            }

            return true;
        }

        return false;
    }

    public boolean hasBinaryOperation(BinaryOperator operator, SType left, SType right) {
        for (ExtensionBinaryOperationDeclaration declaration : binaryOperationNodeMap.values()) {
            if (declaration.getOperation().getOperator() != operator) {
                continue;
            }

            if (declaration.hasError()) {
                continue;
            }

            if (!declaration.getOperation().getLeft().equals(left)) {
                continue;
            }

            if (!declaration.getOperation().getRight().equals(right)) {
                continue;
            }

            return true;
        }

        return false;
    }
}
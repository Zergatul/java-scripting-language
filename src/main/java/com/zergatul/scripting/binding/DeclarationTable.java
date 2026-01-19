package com.zergatul.scripting.binding;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.binding.nodes.BoundParameterNode;
import com.zergatul.scripting.parser.BinaryOperator;
import com.zergatul.scripting.parser.UnaryOperator;
import com.zergatul.scripting.parser.nodes.*;
import com.zergatul.scripting.symbols.SymbolRef;
import com.zergatul.scripting.type.*;
import com.zergatul.scripting.type.operation.BinaryOperation;
import com.zergatul.scripting.type.operation.UnaryOperation;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;

public class DeclarationTable {

    private final Map<String, StaticVariableDeclaration> staticVariables = new HashMap<>();
    private final Map<String, FunctionDeclaration> functions = new HashMap<>();
    private final Map<String, ClassDeclaration> classes = new HashMap<>();
    private final Map<String, TypeAliasDeclaration> typeAliases = new HashMap<>();
    private final List<ExtensionDeclaration> extensions = new ArrayList<>();

    private final Map<StaticVariableNode, StaticVariableDeclaration> staticVariableNodeMap = new HashMap<>();
    private final Map<FunctionNode, FunctionDeclaration> functionNodeMap = new HashMap<>();
    private final Map<ClassNode, ClassDeclaration> classNodeMap = new HashMap<>();
    private final Map<TypeAliasNode, TypeAliasDeclaration> typeAliasNodeMap = new HashMap<>();
    private final Map<ExtensionNode, ExtensionDeclaration> extensionNodeMap = new HashMap<>();

    private final List<ExtensionMethodReference> extensionMethods = new ArrayList<>();
    private final List<ExtensionUnaryOperation> extensionUnaryOperations = new ArrayList<>();
    private final List<ExtensionBinaryOperation> extensionBinaryOperations = new ArrayList<>();

    public void addStaticVariable(StaticVariableNode fieldNode, StaticVariableDeclaration declaration) {
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

    public void addTypeAlias(String name, TypeAliasNode typeAliasNode, TypeAliasDeclaration declaration) {
        if (!name.isEmpty() && !typeAliases.containsKey(name)) {
            typeAliases.put(name, declaration);
        }
        typeAliasNodeMap.put(typeAliasNode, declaration);
    }

    public void addExtension(ExtensionNode extensionNode, ExtensionDeclaration declaration) {
        extensions.add(declaration);
        extensionNodeMap.put(extensionNode, declaration);
    }

    public void addExtensionMethod(ExtensionMethodReference method) {
        extensionMethods.add(method);
    }

    public void addExtensionUnaryOperation(ExtensionUnaryOperation operation) {
        extensionUnaryOperations.add(operation);
    }

    public void addExtensionBinaryOperation(ExtensionBinaryOperation operation) {
        extensionBinaryOperations.add(operation);
    }

    public StaticVariableDeclaration getStaticVariableDeclaration(StaticVariableNode fieldNode) {
        StaticVariableDeclaration declaration = staticVariableNodeMap.get(fieldNode);
        if (declaration == null) {
            throw new InternalException();
        }
        return declaration;
    }

    public FunctionDeclaration getFunctionDeclaration(FunctionNode functionNode) {
        FunctionDeclaration declaration = functionNodeMap.get(functionNode);
        if (declaration == null) {
            throw new InternalException();
        }
        return declaration;
    }

    public ClassDeclaration getClassDeclaration(ClassNode classNode) {
        ClassDeclaration declaration = classNodeMap.get(classNode);
        if (declaration == null) {
            throw new InternalException();
        }
        return declaration;
    }

    public TypeAliasDeclaration getTypeAliasDeclaration(TypeAliasNode typeAliasNode) {
        TypeAliasDeclaration declaration = typeAliasNodeMap.get(typeAliasNode);
        if (declaration == null) {
            throw new InternalException();
        }
        return declaration;
    }

    public ExtensionDeclaration getExtensionDeclaration(ExtensionNode extensionNode) {
        ExtensionDeclaration declaration = extensionNodeMap.get(extensionNode);
        if (declaration == null) {
            throw new InternalException();
        }
        return declaration;
    }

    public void forEachClassDeclaration(BiConsumer<ClassNode, ClassDeclaration> consumer) {
        classNodeMap.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> entry.getKey().getRange().getPosition()))
                .forEachOrdered(entry -> consumer.accept(entry.getKey(), entry.getValue()));
    }

    public void forEachExtension(BiConsumer<ExtensionNode, ExtensionDeclaration> consumer) {
        extensionNodeMap.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> entry.getKey().getRange().getPosition()))
                .forEachOrdered(entry -> consumer.accept(entry.getKey(), entry.getValue()));
    }

    public String generateExtensionMethodInternalName(SType type, String methodName) {
        return String.format("$_extension_$_%s_$_%s", type.asMethodPart(), methodName);
    }

    public String generateExtensionOperationOverloadInternalName(SType type, String operation) {
        return String.format("$_extension_$_%s_$_op_$_%s", type.asMethodPart(), operation);
    }

    public void appendExtensionMethods(SType type, List<MethodReference> methods) {
        for (ExtensionMethodReference method : extensionMethods) {
            if (type.isInstanceOf(method.getOwner())) {
                methods.add(method);
            }
        }
    }

    public List<UnaryOperation> appendExtensionUnaryOperations(SType type, List<UnaryOperation> operations) {
        List<UnaryOperation> combined = new ArrayList<>(operations);
        for (ExtensionUnaryOperation operation : extensionUnaryOperations) {
            if (operation.getOperandType().equals(type)) {
                combined.add(operation);
            }
        }
        return combined;
    }

    public List<BinaryOperation> getExtensionBinaryOperations() {
        return Collections.unmodifiableList(extensionBinaryOperations);
    }

    @Nullable
    public SymbolRef getSymbol(String name) {
        if (classes.containsKey(name)) {
            return classes.get(name).getSymbolRef();
        }
        if (typeAliases.containsKey(name)) {
            return typeAliases.get(name).getSymbolRef();
        }
        if (functions.containsKey(name)) {
            return functions.get(name).getSymbolRef();
        }
        return null;
    }

    public boolean hasSymbol(String name) {
        return staticVariables.containsKey(name) || functions.containsKey(name) || classes.containsKey(name) || typeAliases.containsKey(name);
    }

    public boolean hasExtensionMethod(SType type, String name, List<BoundParameterNode> parameters) {
        for (ExtensionDeclaration declaration : extensions) {
            if (declaration.getBaseType().equals(type) && declaration.hasMethod(name, parameters)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasExtensionUnaryOperationOverload(SType type, UnaryOperator operator) {
        for (ExtensionDeclaration declaration : extensions) {
            if (declaration.getBaseType().equals(type) && declaration.hasUnaryOperation(operator)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasExtensionBinaryOperationOverload(BinaryOperator operator, SType left, SType right) {
        for (ExtensionDeclaration declaration : extensions) {
            if (declaration.hasBinaryOperation(operator, left, right)) {
                return true;
            }
        }

        return false;
    }
}
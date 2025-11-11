package com.zergatul.scripting.binding;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.binding.nodes.BoundParameterNode;
import com.zergatul.scripting.parser.nodes.*;
import com.zergatul.scripting.symbols.SymbolRef;
import com.zergatul.scripting.type.ExtensionMethodReference;
import com.zergatul.scripting.type.MethodReference;
import com.zergatul.scripting.type.SType;
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
        return "$_extension_$_" + type.asMethodPart() + "_" + methodName;
    }

    public void appendExtensionMethods(SType type, List<MethodReference> methods) {
        for (ExtensionMethodReference method : extensionMethods) {
            if (type.isInstanceOf(method.getOwner())) {
                methods.add(method);
            }
        }
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
        return staticVariables.containsKey(name) || functions.containsKey(name) || classes.containsKey(name);
    }

    public boolean hasExtensionMethod(SType type, String name, List<BoundParameterNode> parameters) {
        for (ExtensionDeclaration declaration : extensions) {
            if (declaration.getBaseType().equals(type) && declaration.hasMethod(name, parameters)) {
                return true;
            }
        }

        return false;
    }
}
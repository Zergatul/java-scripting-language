package com.zergatul.scripting.analysis.definition;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.symbols.Symbol;
import com.zergatul.scripting.type.*;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class DefinitionProvider {

    @Nullable
    public TextRange get(BinderOutput output, int line, int column) {
        List<BoundNode> chain = output.unit().find(line, column);
        if (chain.isEmpty()) {
            return null;
        }

        BoundNode node = chain.getFirst();
        return switch (node.getNodeType()) {
            case NAME_EXPRESSION -> {
                BoundNameExpressionNode name = (BoundNameExpressionNode) node;
                Symbol symbol = name.getSymbol();
                if (symbol == null) {
                    yield null;
                }
                yield symbol.getDefinition();
            }

            case FUNCTION_REFERENCE -> {
                BoundFunctionReferenceNode functionReference = (BoundFunctionReferenceNode) node;
                Symbol symbol = functionReference.getFunction();
                yield symbol.getDefinition();
            }

            case ALIASED_TYPE -> {
                BoundAliasedTypeNode aliasedTypeNode = (BoundAliasedTypeNode) node;
                yield aliasedTypeNode.getSymbol().getDefinition();
            }

            case DECLARED_CLASS_TYPE -> {
                BoundDeclaredClassTypeNode declaredClassTypeNode = (BoundDeclaredClassTypeNode) node;
                BoundNode parent = chain.get(1);
                if (parent.is(BoundNodeType.OBJECT_CREATION_EXPRESSION)) {
                    BoundObjectCreationExpressionNode creationExpressionNode = (BoundObjectCreationExpressionNode) parent;
                    if (creationExpressionNode.constructor != UnknownConstructorReference.instance) {
                        BoundClassNode classNode = findClassByType(output, declaredClassTypeNode.getSymbol().getDeclaredType());
                        if (classNode != null) {
                            TextRange definition = findClassConstructorDefinition(classNode, creationExpressionNode.constructor);
                            if (definition != null) {
                                yield definition;
                            }
                        }
                    }
                }
                yield declaredClassTypeNode.getSymbol().getDefinition();
            }

            case CONSTRUCTOR_INITIALIZER -> {
                BoundConstructorInitializerNode initializerNode = (BoundConstructorInitializerNode) node;
                if (initializerNode.syntaxNode.keyword.getRange().contains(line, column)) {
                    if (initializerNode.constructor != UnknownConstructorReference.instance) {
                        SType owner = initializerNode.constructor.getOwner();
                        if (owner instanceof SDeclaredType declaredType) {
                            BoundClassNode classNode = findClassByType(output, declaredType);
                            if (classNode != null) {
                                TextRange definition = findClassConstructorDefinition(classNode, initializerNode.constructor);
                                if (definition != null) {
                                    yield definition;
                                }
                            }
                        }
                    }
                }
                yield null;
            }

            case PROPERTY -> {
                BoundPropertyNode propertyNode = (BoundPropertyNode) node;
                if (propertyNode.property instanceof DeclaredFieldReference field) {
                    BoundClassNode classNode = findClassByType(output, field.getOwner());
                    if (classNode != null) {
                        yield findClassFieldDefinition(classNode, propertyNode.property);
                    }
                }
                yield null;
            }

            case METHOD -> {
                BoundMethodNode methodNode = (BoundMethodNode) node;
                if (methodNode.method instanceof DeclaredMethodReference method) {
                    BoundClassNode classNode = findClassByType(output, (SDeclaredType) method.getOwner());
                    if (classNode != null) {
                        yield findClassMethodDefinition(classNode, method);
                    }
                }
                if (methodNode.method instanceof ExtensionMethodReference method) {
                    yield findExtensionMethodDefinition(output, method);
                }
                yield null;
            }

            default -> null;
        };
    }

    @Nullable
    private static BoundClassNode findClassByType(BinderOutput output, SDeclaredType type) {
        for (BoundCompilationUnitMemberNode member : output.unit().members.members) {
            if (member.is(BoundNodeType.CLASS_DECLARATION)) {
                BoundClassNode classNode = (BoundClassNode) member;
                if (classNode.getDeclaredType() == type) {
                    return classNode;
                }
            }
        }
        return null;
    }

    @Nullable
    private static TextRange findClassFieldDefinition(BoundClassNode classNode, PropertyReference property) {
        for (BoundClassMemberNode member : classNode.members) {
            if (member.is(BoundNodeType.CLASS_FIELD)) {
                BoundClassFieldNode fieldNode = (BoundClassFieldNode) member;
                if (fieldNode.property == property) {
                    return fieldNode.name.getSymbolOrThrow().getDefinition();
                }
            }
        }
        return null;
    }

    @Nullable
    private static TextRange findClassConstructorDefinition(BoundClassNode classNode, ConstructorReference constructor) {
        for (BoundClassMemberNode member : classNode.members) {
            if (member.is(BoundNodeType.CLASS_CONSTRUCTOR)) {
                BoundClassConstructorNode constructorNode = (BoundClassConstructorNode) member;
                if (constructorNode.constructor == constructor) {
                    return TextRange.combine(constructorNode.syntaxNode.keyword, constructorNode.syntaxNode.parameters);
                }
            }
        }
        return null;
    }

    @Nullable
    private static TextRange findClassMethodDefinition(BoundClassNode classNode, DeclaredMethodReference method) {
        for (BoundClassMemberNode member : classNode.members) {
            if (member.is(BoundNodeType.CLASS_METHOD)) {
                BoundClassMethodNode methodNode = (BoundClassMethodNode) member;
                if (methodNode.method == method) {
                    return methodNode.name.getSymbolOrThrow().getDefinition();
                }
            }
        }
        return null;
    }

    @Nullable
    private static TextRange findExtensionMethodDefinition(BinderOutput output, ExtensionMethodReference method) {
        for (BoundCompilationUnitMemberNode member : output.unit().members.members) {
            if (member.is(BoundNodeType.EXTENSION_DECLARATION)) {
                BoundExtensionNode extensionNode = (BoundExtensionNode) member;
                for (BoundExtensionMethodNode methodNode : extensionNode.methods) {
                    if (methodNode.method == method) {
                        return methodNode.name.getSymbolOrThrow().getDefinition();
                    }
                }
            }
        }
        return null;
    }
}
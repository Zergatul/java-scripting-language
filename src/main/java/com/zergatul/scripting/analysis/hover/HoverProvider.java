package com.zergatul.scripting.analysis.hover;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.parser.nodes.BaseExpressionNode;
import com.zergatul.scripting.parser.nodes.ParserNode;
import com.zergatul.scripting.parser.nodes.ParserNodeType;
import com.zergatul.scripting.symbols.*;
import com.zergatul.scripting.type.*;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class HoverProvider<T> {

    private final HoverFactory<T> factory;

    public HoverProvider(HoverFactory<T> factory) {
        this.factory = factory;
    }

    public @Nullable HoverResponse<T> get(BinderOutput output, int line, int column) {
        List<BoundNode> chain = new ArrayList<>();
        findChain(chain, output.unit(), line, column);
        return get(chain, line, column);
    }

    private @Nullable HoverResponse<T> get(List<BoundNode> chain, int line, int column) {
        if (chain.isEmpty()) {
            return null;
        }

        BoundNode node = chain.getFirst();
        TextRange range = node.getRange();
        return switch (node.getNodeType()) {
            case BOOLEAN_LITERAL -> response(factory.getTypeHover(SBoolean.instance), range);
            case INTEGER_LITERAL -> response(factory.getTypeHover(SInt.instance), range);
            case INTEGER64_LITERAL -> response(factory.getTypeHover(SInt64.instance), range);
            case CHAR_LITERAL -> response(factory.getTypeHover(SChar.instance), range);
            case FLOAT_LITERAL -> response(factory.getTypeHover(SFloat.instance), range);
            case STRING_LITERAL -> response(factory.getTypeHover(SString.instance), range);

            case ALIASED_TYPE -> {
                BoundAliasedTypeNode aliasedTypeNode = (BoundAliasedTypeNode) node;
                yield response(factory.getTypeAliasHover(aliasedTypeNode.getSymbol().getAliasType()), range);
            }
            case CUSTOM_TYPE -> response(factory.getTypeHover(((BoundCustomTypeNode) node).type), range);
            case DECLARED_CLASS_TYPE -> response(factory.getTypeHover(((BoundDeclaredClassTypeNode) node).type), range);
            case PREDEFINED_TYPE -> response(factory.getTypeHover(((BoundPredefinedTypeNode) node).type), range);

            case NAME_EXPRESSION -> {
                BoundNameExpressionNode name = (BoundNameExpressionNode) node;
                if (name.getSymbol() instanceof ExternalParameter external) {
                    yield response(factory.getExternalParameterHover(external), range);
                } else if (name.getSymbol() instanceof LocalParameter local) {
                    yield response(factory.getParameterHover(local), range);
                } else if (name.getSymbol() instanceof LocalRefParameter local) {
                    yield response(factory.getRefParameterHover(local), range);
                } else if (name.getSymbol() instanceof LocalVariable local) {
                    yield response(factory.getLocalVariableHover(local), range);
                } else if (name.getSymbol() instanceof StaticFieldConstantStaticVariable field) {
                    yield response(factory.getStaticConstantHover(field), range);
                } else if (name.getSymbol() instanceof StaticVariable staticVariable) {
                    yield response(factory.getStaticVariableHover(staticVariable), range);
                } else if (name.getSymbol() instanceof Function function) {
                    yield response(factory.getFunctionHover(function), range);
                } else {
                    yield null;
                }
            }
            case SYMBOL -> {
                BoundSymbolNode symbolNode = (BoundSymbolNode) node;
                if (symbolNode.symbolRef.get() instanceof LocalVariable local) {
                    yield response(factory.getLocalVariableHover(local), range);
                } else {
                    yield null;
                }
            }
            case THIS_EXPRESSION -> response(factory.getThisHover(((BoundThisExpressionNode) node).type), range);
            case METHOD -> {
                MethodReference method = ((BoundMethodNode) node).method;
                if (method instanceof UnknownMethodReference) {
                    yield null;
                }
                yield response(factory.getMethodHover(method), range);
            }
            case PROPERTY -> {
                PropertyReference property = ((BoundPropertyNode) node).property;
                if (property instanceof UnknownPropertyReference) {
                    yield null;
                }
                BoundPropertyAccessExpressionNode access = (BoundPropertyAccessExpressionNode) chain.get(1);
                yield response(factory.getPropertyHover(access.callee.type, property), range);
            }
            case BINARY_OPERATOR -> response(
                    factory.getBinaryOperationHover(((BoundBinaryOperatorNode) node).operation),
                    range);
            case FUNCTION -> response(
                    factory.getFunctionDeclarationHover(((BoundFunctionNode) node).function),
                    range);

            case BASE_METHOD_INVOCATION_EXPRESSION -> {
                BoundBaseMethodInvocationExpressionNode invocation = (BoundBaseMethodInvocationExpressionNode) node;
                BaseExpressionNode baseExpression = invocation.getBaseExpressionSyntaxNode();
                if (baseExpression.getRange().contains(line, column)) {
                    BoundClassNode classNode = closestNodeOfType(chain, BoundNodeType.CLASS_DECLARATION, BoundClassNode.class);
                    if (classNode != null) {
                        SDeclaredType classType = (SDeclaredType) classNode.name.getSymbolOrThrow().getType();
                        yield response(factory.getBaseHover(classType.getBaseType()), baseExpression.getRange());
                    }
                }
                yield null;
            }
            case CONSTRUCTOR_INITIALIZER -> {
                BoundConstructorInitializerNode initializer = (BoundConstructorInitializerNode) node;
                if (initializer.syntaxNode.keyword.getRange().contains(line, column)) {
                    yield response(
                            factory.getConstructorHover(initializer.constructor),
                            initializer.syntaxNode.keyword.getRange());
                }
                yield null;
            }
            case INVALID_EXPRESSION -> {
                BoundInvalidExpressionNode invalidExpression = (BoundInvalidExpressionNode) node;
                HoverResponse<T> hover = null;
                for (ParserNode syntaxNode : invalidExpression.unboundNodes) {
                    if (syntaxNode.is(ParserNodeType.BASE_EXPRESSION) && syntaxNode.getRange().contains(line, column)) {
                        BoundClassNode classNode = closestNodeOfType(chain, BoundNodeType.CLASS_DECLARATION, BoundClassNode.class);
                        if (classNode != null) {
                            SDeclaredType classType = (SDeclaredType) classNode.name.getSymbolOrThrow().getType();
                            hover = response(factory.getBaseHover(classType.getBaseType()), syntaxNode.getRange());
                            break;
                        }
                    }
                }
                yield hover;
            }

            default -> null;
        };
    }

    private HoverResponse<T> response(T content, TextRange range) {
        return new HoverResponse<>(content, range);
    }

    private static void findChain(List<BoundNode> chain, BoundNode node, int line, int column) {
        if (node.getRange().contains(line, column)) {
            for (BoundNode child : node.getChildren()) {
                if (child.getRange().contains(line, column)) {
                    findChain(chain, child, line, column);
                }
            }
            chain.add(node);
        }
    }

    private static <T extends BoundNode> T closestNodeOfType(List<BoundNode> chain, BoundNodeType type, Class<T> clazz) {
        for (BoundNode node : chain) {
            if (node.is(type)) {
                return clazz.cast(node);
            }
        }
        return null;
    }

    public record HoverResponse<T>(T content, TextRange range) {}
}
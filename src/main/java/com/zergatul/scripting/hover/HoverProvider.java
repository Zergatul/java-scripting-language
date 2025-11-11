package com.zergatul.scripting.hover;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.parser.nodes.BaseExpressionNode;
import com.zergatul.scripting.parser.nodes.ParserNode;
import com.zergatul.scripting.parser.nodes.ParserNodeType;
import com.zergatul.scripting.symbols.*;
import com.zergatul.scripting.type.*;
import com.zergatul.scripting.type.operation.BinaryOperation;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HoverProvider {

    protected final DocumentationProvider documentationProvider;

    public HoverProvider() {
        this.documentationProvider = new DocumentationProvider();
    }

    public HoverResponse get(BinderOutput output, int line, int column) {
        List<BoundNode> chain = new ArrayList<>();
        findChain(chain, output.unit(), line, column);
        return get(chain, line, column);
    }

    private HoverResponse get(List<BoundNode> chain, int line, int column) {
        if (chain.isEmpty()) {
            return null;
        }

        BoundNode node = chain.getFirst();
        if (node == null) {
            return null;
        }

        TextRange range = node.getRange();
        return switch (node.getNodeType()) {
            case BOOLEAN_LITERAL -> getBooleanHover(range);
            case INTEGER_LITERAL -> getInt32Hover(range);
            case INTEGER64_LITERAL -> getInt64Hover(range);
            case CHAR_LITERAL -> getCharHover(range);
            case FLOAT_LITERAL -> getFloat64Hover(range);
            case STRING_LITERAL -> getStringHover(range);

            case ALIASED_TYPE -> {
                BoundAliasedTypeNode aliasedTypeNode = (BoundAliasedTypeNode) node;
                String text =
                        formatKeyword("typealias") + " " +
                        formatType(aliasedTypeNode.syntaxNode.value) + " = " +
                        formatType(aliasedTypeNode.getSymbol().getAliasType().getFinalType());
                yield new HoverResponse(text, range);
            }

            case CUSTOM_TYPE -> new HoverResponse(formatType(((BoundCustomTypeNode) node).type), range);
            case DECLARED_CLASS_TYPE -> new HoverResponse(formatType(((BoundDeclaredClassTypeNode) node).type), range);
            case PREDEFINED_TYPE -> {
                SType type = ((BoundPredefinedTypeNode) node).type;
                if (type == SBoolean.instance) {
                    yield getBooleanHover(range);
                } else if (type == SInt.instance) {
                    yield getInt32Hover(range);
                } else if (type == SInt64.instance) {
                    yield getInt64Hover(range);
                } else if (type == SChar.instance) {
                    yield getCharHover(range);
                } else if (type == SFloat.instance) {
                    yield getFloat64Hover(range);
                } else if (type == SString.instance) {
                    yield getStringHover(range);
                } else {
                    yield null;
                }
            }
            case NAME_EXPRESSION -> {
                BoundNameExpressionNode name = (BoundNameExpressionNode) node;
                if (name.getSymbol() instanceof ExternalParameter external) {
                    String text = formatDescription("(external parameter)") + " " + formatType(external.getType()) + " " + formatIdentifier(external.getName());
                    yield new HoverResponse(text, range);
                } else if (name.getSymbol() instanceof LocalParameter local) {
                    String text = formatDescription("(parameter)") + " " + formatType(local.getType()) + " " + formatIdentifier(local.getName());
                    yield new HoverResponse(text, range);
                } else if (name.getSymbol() instanceof LocalRefParameter local) {
                    String text = formatDescription("(parameter)") + " " + formatPredefinedType("ref") + " " + formatType(local.getType()) + " " + formatIdentifier(local.getName());
                    yield new HoverResponse(text, range);
                } else if (name.getSymbol() instanceof LocalVariable local) {
                    String text = formatDescription("(local variable)") + " " + formatType(local.getType()) + " " + formatIdentifier(local.getName());
                    yield new HoverResponse(text, range);
                } else if (name.getSymbol() instanceof StaticFieldConstantStaticVariable field) {
                    String text = formatDescription("(external static constant)") + " " + formatType(field.getType()) + " " + formatIdentifier(field.getName());
                    yield new HoverResponse(text, range);
                } else if (name.getSymbol() instanceof StaticVariable staticVariable) {
                    String text = formatDescription("(static variable)") + " " + formatType(staticVariable.getType()) + " " + formatIdentifier(staticVariable.getName());
                    yield new HoverResponse(text, range);
                } else if (name.getSymbol() instanceof Function function) {
                    SStaticFunction type = function.getFunctionType();
                    StringBuilder sb = new StringBuilder();
                    sb.append(formatType(type.getReturnType())).append(' ');
                    sb.append(formatMethod(function.getName()));
                    sb.append(formatBrackets("("));
                    List<MethodParameter> parameters = type.getParameters();
                    for (int i = 0; i < parameters.size(); i++) {
                        sb.append(formatType(parameters.get(i).type())).append(' ');
                        sb.append(formatParameter(parameters.get(i).name()));
                        if (i < parameters.size() - 1) {
                            sb.append(", ");
                        }
                    }
                    sb.append(formatBrackets(")"));
                    yield new HoverResponse(sb.toString(), range);
                } else {
                    yield null;
                }
            }
            case THIS_EXPRESSION -> {
                BoundThisExpressionNode expression = (BoundThisExpressionNode) node;
                String text = formatType(expression.type) + " " + formatPredefinedType("this");
                yield new HoverResponse(text, range);
            }
            case METHOD -> {
                BoundMethodNode methodNode = (BoundMethodNode) node;
                MethodReference methodReference = methodNode.method;
                if (methodReference instanceof UnknownMethodReference) {
                    yield null;
                }

                StringBuilder sb = new StringBuilder();
                if (methodReference instanceof ExtensionMethodReference) {
                    sb.append(formatDescription("(extension)")).append(' ');
                }

                sb.append(formatType(methodReference.getReturn())).append(' ');
                sb.append(formatType(methodReference.getOwner()));
                sb.append(".");
                sb.append(formatMethod(methodReference.getName()));
                sb.append(formatMethodParameters(methodReference.getParameters()));

                List<String> lines = new ArrayList<>();
                lines.add(sb.toString());
                Optional<String> documentation = documentationProvider.getMethodDocumentation(methodReference);
                if (documentation.isPresent()) {
                    lines.add(documentation.get().replace("\n", "<br>"));
                }
                yield new HoverResponse(lines, range);
            }
            case PROPERTY -> {
                BoundPropertyNode propertyNode = (BoundPropertyNode) node;
                PropertyReference property = propertyNode.property;
                if (property instanceof UnknownPropertyReference) {
                    yield null;
                }
                BoundPropertyAccessExpressionNode access = (BoundPropertyAccessExpressionNode) chain.get(1);
                String text = formatDescription("(property)") + " " + formatType(property.getType()) + " " + formatType(access.callee.type) + "." + formatIdentifier(property.getName());
                yield new HoverResponse(text, range);
            }
            case BINARY_OPERATOR -> {
                BoundBinaryOperatorNode operator = (BoundBinaryOperatorNode) node;
                BinaryOperation operation = operator.operation;
                String text = formatType(operation.type) + " " + formatDescription(operation.operator.toString()) + formatBrackets("(") + formatType(operation.getLeft()) + " " + formatParameter("left") + "," + " " + formatType(operation.getRight()) + " " + formatParameter("right") + formatBrackets(")");
                yield new HoverResponse(text, range);
            }
            case FUNCTION_REFERENCE -> {
                BoundFunctionReferenceNode reference = (BoundFunctionReferenceNode) node;
                SFunction functionType = (SFunction) reference.type;
                String text = formatDescription("(function)") + " " + formatType(functionType.getReturnType()) + " " + formatIdentifier(reference.name) + formatMethodParameters(functionType.getParameters());
                yield new HoverResponse(text, range);
            }

            case BASE_METHOD_INVOCATION_EXPRESSION -> {
                BoundBaseMethodInvocationExpressionNode invocationExpression = (BoundBaseMethodInvocationExpressionNode) node;
                BaseExpressionNode baseExpression = invocationExpression.getBaseExpressionSyntaxNode();
                if (baseExpression.getRange().contains(line, column)) {
                    BoundClassNode classNode = closestNodeOfType(chain, BoundNodeType.CLASS_DECLARATION, BoundClassNode.class);
                    if (classNode != null) {
                        SDeclaredType classType = (SDeclaredType) classNode.name.getSymbolOrThrow().getType();
                        String text = formatType(classType.getActualBaseType()) + " " + formatPredefinedType("base");
                        yield new HoverResponse(text, baseExpression.getRange());
                    }
                }
                yield null;
            }

            case CONSTRUCTOR_INITIALIZER -> {
                BoundConstructorInitializerNode initializer = (BoundConstructorInitializerNode) node;
                if (initializer.syntaxNode.keyword.getRange().contains(line, column)) {
                    String text =
                            formatKeyword("constructor") + " " +
                            formatType(initializer.constructor.getOwner()) +
                            formatMethodParameters(initializer.constructor.getParameters());
                    yield new HoverResponse(text, initializer.syntaxNode.keyword.getRange());
                }

                yield null;
            }

            case INVALID_EXPRESSION -> {
                BoundInvalidExpressionNode invalidExpression = (BoundInvalidExpressionNode) node;
                for (ParserNode syntaxNode : invalidExpression.unboundNodes) {
                    if (syntaxNode.is(ParserNodeType.BASE_EXPRESSION) && syntaxNode.getRange().contains(line, column)) {
                        BoundClassNode classNode = closestNodeOfType(chain, BoundNodeType.CLASS_DECLARATION, BoundClassNode.class);
                        if (classNode != null) {
                            SDeclaredType classType = (SDeclaredType) classNode.name.getSymbolOrThrow().getType();
                            String text = formatType(classType.getActualBaseType()) + " " + formatPredefinedType("base");
                            yield new HoverResponse(text, syntaxNode.getRange());
                        }
                    }
                }
                yield null;
            }

            default -> null;
        };
    }

    protected HoverResponse getBooleanHover(TextRange range) {
        return new HoverResponse(List.of(formatPredefinedType("boolean"), formatDescription(documentationProvider.getTypeDocs(SBoolean.instance))), range);
    }

    protected HoverResponse getInt32Hover(TextRange range) {
        return new HoverResponse(List.of(formatPredefinedType("int"), formatDescription(documentationProvider.getTypeDocs(SInt.instance))), range);
    }

    protected HoverResponse getInt64Hover(TextRange range) {
        return new HoverResponse(List.of(formatPredefinedType("long"), formatDescription(documentationProvider.getTypeDocs(SInt64.instance))), range);
    }

    protected HoverResponse getCharHover(TextRange range) {
        return new HoverResponse(List.of(formatPredefinedType("char"), formatDescription(documentationProvider.getTypeDocs(SChar.instance))), range);
    }

    protected HoverResponse getFloat64Hover(TextRange range) {
        return new HoverResponse(List.of(formatPredefinedType("float"), formatDescription(documentationProvider.getTypeDocs(SFloat.instance))), range);
    }

    protected HoverResponse getStringHover(TextRange range) {
        return new HoverResponse(List.of(formatPredefinedType("string"), formatDescription(documentationProvider.getTypeDocs(SString.instance))), range);
    }

    protected String formatKeyword(String keyword) {
        return keyword;
    }

    protected String formatType(SType type) {
        switch (type) {
            case SPredefinedType sPredefinedType -> {
                return formatPredefinedType(type.toString());
            }
            case SArrayType array -> {
                return formatType(array.getElementsType()) + formatBrackets("[]");
            }
            case SFunctionalInterface func -> {
                StringBuilder sb = new StringBuilder();
                sb.append(formatType("Lambda<"));
                sb.append('(');
                SType[] types = func.getActualParameters();
                Parameter[] parameters = func.getInterfaceMethod().getParameters();
                for (int i = 0; i < types.length; i++) {
                    sb.append(formatType(types[i]));
                    sb.append(' ');
                    sb.append(parameters[i].getName());
                    if (i != types.length - 1) {
                        sb.append(", ");
                    }
                }
                sb.append(')');
                sb.append(" => ");
                sb.append(formatType(func.getActualReturnType()));
                sb.append(formatType(">"));
                return sb.toString();
            }
            case SFuture future -> {
                if (future.getUnderlying() == SVoidType.instance) {
                    return formatType("Future");
                } else {
                    return formatType("Future<") + formatType(future.getUnderlying()) + formatType(">");
                }
            }
            case SCustomType sCustomType -> {
                return formatType(type.toString());
            }
            case SClassType sClassType -> {
                Class<?> clazz = type.getJavaClass();
                return formatType(clazz.getName());
            }
            case SDeclaredType declaredType -> {
                return formatType(type.toString());
            }
            case null, default -> {
                return "TODO";
            }
        }
    }

    protected String formatPredefinedType(String text) {
        return text;
    }

    protected String formatType(String text) {
        return text;
    }

    protected String formatMethod(String text) {
        return text;
    }

    protected String formatIdentifier(String text) {
        return text;
    }

    protected String formatMethodParameters(List<MethodParameter> parameters) {
        StringBuilder sb = new StringBuilder();
        sb.append(formatBrackets("("));
        for (int i = 0; i < parameters.size(); i++) {
            sb.append(formatType(parameters.get(i).type())).append(' ');
            sb.append(formatParameter(parameters.get(i).name()));
            if (i < parameters.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(formatBrackets(")"));
        return sb.toString();
    }

    protected String formatParameter(String text) {
        return text;
    }

    protected String formatBrackets(String text) {
        return text;
    }

    protected String formatDescription(String text) {
        return text;
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
        for (var node : chain) {
            if (node.is(type)) {
                return clazz.cast(node);
            }
        }
        return null;
    }

    public record HoverResponse(List<String> content, TextRange range) {
        public HoverResponse(String line, TextRange range) {
            this(List.of(line), range);
        }
    }
}
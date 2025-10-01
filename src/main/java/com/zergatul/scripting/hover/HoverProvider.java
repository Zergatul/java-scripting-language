package com.zergatul.scripting.hover;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.*;
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
        return get(chain);
    }

    private HoverResponse get(List<BoundNode> chain) {
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
            case CUSTOM_TYPE -> new HoverResponse(formatType(((BoundCustomTypeNode) node).type), range);
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
                    String line = formatDescription("(external parameter)") + " " + formatType(external.getType()) + " " + formatDescription(external.getName());
                    yield new HoverResponse(line, range);
                } else if (name.getSymbol() instanceof LocalParameter local) {
                    String line = formatDescription("(parameter)") + " " + formatType(local.getType()) + " " + formatDescription(local.getName());
                    yield new HoverResponse(line, range);
                } else if (name.getSymbol() instanceof LocalRefParameter local) {
                    String line = formatDescription("(parameter)") + " " + formatPredefinedType("ref") + " " + formatType(local.getType()) + " " + formatDescription(local.getName());
                    yield new HoverResponse(line, range);
                } else if (name.getSymbol() instanceof LocalVariable local) {
                    String line = formatDescription("(local variable)") + " " + formatType(local.getType()) + " " + formatDescription(local.getName());
                    yield new HoverResponse(line, range);
                } else if (name.getSymbol() instanceof StaticFieldConstantStaticVariable field) {
                    String line = formatDescription("(external static constant)") + " " + formatType(field.getType()) + " " + formatDescription(field.getName());
                    yield new HoverResponse(line, range);
                } else if (name.getSymbol() instanceof StaticVariable staticVariable) {
                    String line = formatDescription("(static variable)") + " " + formatType(staticVariable.getType()) + " " + formatDescription(staticVariable.getName());
                    yield new HoverResponse(line, range);
                } else if (name.getSymbol() instanceof Function function) {
                    SStaticFunction type = function.getFunctionType();
                    StringBuilder sb = new StringBuilder();
                    sb.append(formatType(type.getReturnType())).append(' ');
                    sb.append(formatMethod(function.getName()));
                    sb.append(formatDescription("("));
                    List<MethodParameter> parameters = type.getParameters();
                    for (int i = 0; i < parameters.size(); i++) {
                        sb.append(formatType(parameters.get(i).type())).append(' ');
                        sb.append(formatParameter(parameters.get(i).name()));
                        if (i < parameters.size() - 1) {
                            sb.append(formatDescription(", "));
                        }
                    }
                    sb.append(formatDescription(")"));
                    yield new HoverResponse(sb.toString(), range);
                } else {
                    yield null;
                }
            }
            case METHOD -> {
                BoundMethodNode methodNode = (BoundMethodNode) node;
                MethodReference methodReference = methodNode.method;
                if (methodReference instanceof UnknownMethodReference) {
                    yield null;
                }
                StringBuilder sb = new StringBuilder();
                sb.append(formatType(methodReference.getReturn())).append(' ');
                sb.append(formatType(methodReference.getOwner()));
                sb.append(formatDescription("."));
                sb.append(formatMethod(methodReference.getName()));
                sb.append(formatDescription("("));
                List<MethodParameter> parameters = methodReference.getParameters();
                for (int i = 0; i < parameters.size(); i++) {
                    sb.append(formatType(parameters.get(i).type())).append(' ');
                    sb.append(formatParameter(parameters.get(i).name()));
                    if (i < parameters.size() - 1) {
                        sb.append(formatDescription(", "));
                    }
                }
                sb.append(formatDescription(")"));

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
                String line = formatDescription("(property)") + " " + formatType(property.getType()) + " " + formatType(access.callee.type) + "." + formatDescription(property.getName());
                yield new HoverResponse(line, range);
            }
            case BINARY_OPERATOR -> {
                BoundBinaryOperatorNode operator = (BoundBinaryOperatorNode) node;
                BinaryOperation operation = operator.operation;
                String line = formatType(operation.type) + " " + formatDescription(operation.operator.toString()) + formatDescription("(") + formatType(operation.getLeft()) + " " + formatParameter("left") + formatDescription(",") + " " + formatType(operation.getRight()) + " " + formatParameter("right") + formatDescription(")");
                yield new HoverResponse(line, range);
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

    protected String formatDescription(String text) {
        return text;
    }

    protected String formatParameter(String text) {
        return text;
    }

    protected String formatBrackets(String text) {
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

    public record HoverResponse(List<String> content, TextRange range) {
        public HoverResponse(String line, TextRange range) {
            this(List.of(line), range);
        }
    }
}
package com.zergatul.scripting.completion;

import com.zergatul.scripting.InterfaceHelper;
import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.SingleLineTextRange;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.CompilerContext;
import com.zergatul.scripting.compiler.JavaInteropPolicy;
import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.symbols.*;
import com.zergatul.scripting.type.*;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CompletionProvider<T> {

    private final SuggestionFactory<T> factory;

    public CompletionProvider(SuggestionFactory<T> factory) {
        this.factory = factory;
    }

    public List<T> get(CompilationParameters parameters, BinderOutput output, int line, int column) {
        BoundCompilationUnitNode unit = output.unit();
        CompletionContext completionContext = getCompletionContext(unit, line, column);
        return get(parameters, output, completionContext, line, column);
    }

    private List<T> get(CompilationParameters parameters, BinderOutput output, CompletionContext completionContext, int line, int column) {
        BoundCompilationUnitNode unit = output.unit();
        List<T> suggestions = new ArrayList<>();

        boolean canStatic = false;
        boolean canVoid = false;
        boolean canType = false;
        boolean canStatement = false;
        boolean canExpression = false;
        if (completionContext.entry == null) {
            switch (completionContext.type) {
                case NO_CODE -> {
                    canStatic = canVoid = canType = canStatement = true;
                }
                case BEFORE_FIRST -> {
                    canStatic = canVoid = canType = true;
                    canStatement = unit.members.members.isEmpty();
                }
                case AFTER_LAST -> {
                    canStatic = unit.statements.statements.isEmpty();
                    canVoid = canType = unit.statements.statements.isEmpty();
                    canStatement = true;
                }
            }
        } else {
            // handle cases like this:
            // i<cursor>
            // here we need to propose types, symbols, expressions
            if (isSingleWordStatementStart(completionContext.entry, line, column)) {
                canStatement = true;
            }

            switch (completionContext.entry.node.getNodeType()) {
                case COMPILATION_UNIT -> {
                    if (completionContext.prev == null) {
                        canStatic = true;
                        canVoid = canType = true;
                        if (completionContext.next == null || completionContext.next.getNodeType() == NodeType.STATEMENTS_LIST) {
                            canStatement = true;
                        }
                    } else if (completionContext.prev.getNodeType() == NodeType.COMPILATION_UNIT_MEMBERS) {
                        canStatic = canVoid = canType = true;
                        if (completionContext.next == null || completionContext.next.getNodeType() == NodeType.STATEMENTS_LIST) {
                            canStatement = true;
                        }
                    }
                }
                case STATEMENTS_LIST, BLOCK_STATEMENT -> {
                    // check if we are at the end of unfinished statement
                    if (completionContext.prev != null) {
                        BoundNode unfinished = getUnfinished(completionContext.prev, line, column);
                        if (unfinished != null) {
                            CompletionContext ctx = new CompletionContext(new SearchEntry(completionContext.entry, unfinished), line, column);
                            suggestions.addAll(get(parameters, output, ctx, line, column));
                            break;
                        }
                    }
                    canStatement = true;
                }
                case IF_STATEMENT -> {
                    if (completionContext.prev != null) {
                        BoundNode unfinished = getUnfinished(completionContext.prev, line, column);
                        if (unfinished != null) {
                            CompletionContext ctx = new CompletionContext(new SearchEntry(completionContext.entry, unfinished), line, column);
                            suggestions.addAll(get(parameters, output, ctx, line, column));
                        }
                    } else {
                        BoundIfStatementNode statement = (BoundIfStatementNode) completionContext.entry.node;
                        // if (...<here>...)
                        if (TextRange.isBetween(line, column, statement.lParen.getRange(), statement.rParen.getRange())) {
                            if (statement.condition.getRange().isAfter(line, column) || statement.condition.getRange().isEmpty()) {
                                // if (<here> ...) or if (<here>)
                                canExpression = true;
                            } else {
                                CompletionContext ctx = new CompletionContext(new SearchEntry(completionContext.entry.parent, completionContext.entry.parent.node), line, column);
                                suggestions.addAll(get(parameters, output, ctx, line, column));
                            }
                        }
                    }
                }
                case RETURN_STATEMENT -> {
                    if (completionContext.prev != null) {
                        BoundNode unfinished = getUnfinished(completionContext.prev, line, column);
                        if (unfinished != null) {
                            CompletionContext ctx = new CompletionContext(new SearchEntry(completionContext.entry, unfinished), line, column);
                            suggestions.addAll(get(parameters, output, ctx, line, column));
                        }
                    }
                }
                case ARGUMENTS_LIST -> {
                    // check if we are at the end of unfinished statement
                    if (completionContext.prev != null) {
                        BoundNode unfinished = getUnfinished(completionContext.prev, line, column);
                        if (unfinished != null) {
                            CompletionContext ctx = new CompletionContext(new SearchEntry(completionContext.entry, unfinished), line, column);
                            suggestions.addAll(get(parameters, output, ctx, line, column));
                            break;
                        }
                    }
                    canExpression = true;
                }
                case PROPERTY -> {
                    // get back to PROPERTY_ACCESS_EXPRESSION
                    completionContext = completionContext.up();
                    assert completionContext != null;
                    return get(parameters, output, completionContext, line, column);
                }
                case PROPERTY_ACCESS_EXPRESSION -> {
                    BoundPropertyAccessExpressionNode node = (BoundPropertyAccessExpressionNode) completionContext.entry.node;
                    SType type = node.callee.type;
                    String partial = ""; // return all properties/methods and vscode handles the rest
                    /*if (node.property.getRange().isAfter(line, column)) {
                        partial = "";
                    } else {
                        partial = "";
                    }*/

                    type.getInstanceProperties().stream()
                            .filter(p -> p.getName().toLowerCase().startsWith(partial.toLowerCase()))
                            .forEach(p -> suggestions.add(factory.getPropertySuggestion(p)));
                    type.getInstanceMethods().stream()
                            .filter(m -> m.getName().toLowerCase().startsWith(partial.toLowerCase()))
                            .filter(m -> {
                                if (m instanceof NativeMethodReference nativeRef) {
                                    JavaInteropPolicy checker = parameters.getPolicy();
                                    if (checker != null) {
                                        return checker.isMethodVisible(nativeRef.getUnderlying());
                                    } else {
                                        return true;
                                    }
                                } else {
                                    return true;
                                }
                            })
                            .forEach(m -> suggestions.add(factory.getMethodSuggestion(m)));
                }
                case METHOD_INVOCATION_EXPRESSION -> {
                    BoundMethodInvocationExpressionNode node = (BoundMethodInvocationExpressionNode) completionContext.entry.node;
                    SType type = node.objectReference.type;
                    String partial = "";
                    type.getInstanceProperties().stream()
                            .filter(p -> p.getName().toLowerCase().startsWith(partial.toLowerCase()))
                            .forEach(p -> suggestions.add(factory.getPropertySuggestion(p)));
                    type.getInstanceMethods().stream()
                            .filter(m -> m.getName().toLowerCase().startsWith(partial.toLowerCase()))
                            .forEach(m -> suggestions.add(factory.getMethodSuggestion(m)));
                }
                case VARIABLE_DECLARATION -> {
                    BoundVariableDeclarationNode node = (BoundVariableDeclarationNode) completionContext.entry.node;
                    if (node.expression == null) {
                        if (node.type.getRange().containsOrEnds(line, column)) {
                            canStatement = true;
                        }
                    } else {
                        // should be after "=" token, but let be this for now
                        if (node.name.getRange().isBefore(line, column)) {
                            canExpression = true;
                        }
                    }
                }
                case ASSIGNMENT_STATEMENT, AUGMENTED_ASSIGNMENT_STATEMENT -> {
                    BoundNode unfinished = getUnfinished(completionContext.entry.node, line, column);
                    if (unfinished != null) {
                        CompletionContext ctx = new CompletionContext(new SearchEntry(completionContext.entry, unfinished), line, column);
                        suggestions.addAll(get(parameters, output, ctx, line, column));
                    }
                }
                case FOR_LOOP_STATEMENT -> {
                    BoundNode unfinished = getUnfinished(completionContext.entry.node, line, column);
                    if (unfinished != null) {
                        CompletionContext ctx = new CompletionContext(new SearchEntry(completionContext.entry, unfinished), line, column);
                        suggestions.addAll(get(parameters, output, ctx, line, column));
                    } else {
                        BoundForLoopStatementNode loop = (BoundForLoopStatementNode) completionContext.entry.node;
                        if (loop.rParen.getRange().endsWith(line, column) || (loop.rParen.getRange().isBefore(line, column) && loop.body.getRange().isAfter(line, column))) {
                            canStatement = true;
                        }
                    }
                }
                case FOREACH_LOOP_STATEMENT -> {
                    BoundNode unfinished = getUnfinished(completionContext.entry.node, line, column);
                    if (unfinished != null) {
                        CompletionContext ctx = new CompletionContext(new SearchEntry(completionContext.entry, unfinished), line, column);
                        suggestions.addAll(get(parameters, output, ctx, line, column));
                    } else {
                        BoundForEachLoopStatementNode loop = (BoundForEachLoopStatementNode) completionContext.entry.node;
                        if (loop.rParen.getRange().endsWith(line, column) || (loop.rParen.getRange().isBefore(line, column) && loop.body.getRange().isAfter(line, column))) {
                            canStatement = true;
                        }
                    }
                }
                case BINARY_EXPRESSION, PARAMETER -> {
                    BoundNode unfinished = getUnfinished(completionContext.entry.node, line, column);
                    if (unfinished != null) {
                        CompletionContext ctx = new CompletionContext(new SearchEntry(completionContext.entry, unfinished), line, column);
                        suggestions.addAll(get(parameters, output, ctx, line, column));
                    }
                }
                case INVALID_EXPRESSION -> {
                    BoundInvalidExpressionNode invalid = (BoundInvalidExpressionNode) completionContext.entry.node;
                    if (invalid.getRange().isEmpty()) {
                        canExpression = true;
                    }
                }
                case PARAMETER_LIST -> {
                    BoundNode unfinished = getUnfinished(completionContext.entry.node, line, column);
                    if (unfinished != null) {
                        CompletionContext ctx = new CompletionContext(new SearchEntry(completionContext.entry, unfinished), line, column);
                        suggestions.addAll(get(parameters, output, ctx, line, column));
                    } else {
                        canType = true;
                    }
                }
                case METHOD -> {
                    completionContext = completionContext.up();
                    assert completionContext != null;
                    return get(parameters, output, completionContext, line, column);
                }
                case JAVA_TYPE -> {
                    BoundJavaTypeNode javaType = (BoundJavaTypeNode) completionContext.entry.node;
                    TextRange nameRange = javaType.name.getRange();
                    if (nameRange instanceof SingleLineTextRange singleLineNameRange && nameRange.containsOrEnds(line, column)) {
//                        SingleLineTextRange prefixRange = new SingleLineTextRange(
//                                singleLineNameRange.getLine1(),
//                                singleLineNameRange.getColumn1(),
//                                singleLineNameRange.getPosition(),
//                                singleLineNameRange.getLength() - (column - singleLineNameRange.getColumn2()));
//                        String prefix = prefixRange.extract(output.code());
//                        getClassesSuggestion(prefix);
                    } else {
                        canType = true;
                    }
                }
                case INVALID_TYPE, PREDEFINED_TYPE -> {
                    canType = true;
                }
                case STRING_LITERAL, INTEGER_LITERAL, FLOAT_LITERAL -> {
                    // do nothing
                }
                case NAME_EXPRESSION -> {
                    // skip for case like this:
                    // void func(int x<cursor>
                    // but process case like this:
                    // if (x<cursor>
                    if (completionContext.entry.parent.node.getNodeType() != NodeType.PARAMETER) {
                        canExpression = true;
                    }
                }
                case META_INVALID_EXPRESSION, META_TYPE_EXPRESSION, META_TYPE_OF_EXPRESSION -> {
                    suggestions.add(factory.getKeywordSuggestion(TokenType.META_TYPE));
                    suggestions.add(factory.getKeywordSuggestion(TokenType.META_TYPE_OF));
                }
                default -> {
                    canExpression = true; // good fallback?
                    //throw new InternalException();
                }
            }
        }

        canExpression |= canStatement;
        canType |= canStatement;

        if (canStatic) {
            suggestions.add(factory.getKeywordSuggestion(TokenType.STATIC));
        }
        if (canVoid) {
            suggestions.add(factory.getKeywordSuggestion(TokenType.VOID));
        }
        if (canType | canExpression) {
            for (SType type : new SType[] { SBoolean.instance, SInt.instance, SInt64.instance, SChar.instance, SFloat.instance, SString.instance }) {
                suggestions.addAll(factory.getTypeSuggestion(type));
            }
            for (Class<?> clazz : parameters.getCustomTypes()) {
                suggestions.add(factory.getCustomTypeSuggestion(clazz));
            }
        }
        if (canExpression) {
            suggestions.addAll(getSymbols(parameters, output, completionContext));
            suggestions.add(factory.getKeywordSuggestion(TokenType.META_TYPE));
            suggestions.add(factory.getKeywordSuggestion(TokenType.META_TYPE_OF));
            if (parameters.isAsync()) {
                suggestions.add(factory.getKeywordSuggestion(TokenType.AWAIT));
            }
        }
        if (canStatement) {
            suggestions.addAll(getStatementStart(parameters, output, completionContext));
        }
        if (canStatement && completionContext.prev instanceof BoundIfStatementNode ifStatement && ifStatement.elseStatement == null) {
            suggestions.add(factory.getKeywordSuggestion(TokenType.ELSE));
        }

        suggestions.removeIf(Objects::isNull);
        return suggestions;
    }

    private BoundNode getUnfinished(BoundNode node, int line, int column) {
        if (node instanceof BoundMethodNode method) {
            if (method.getRange().containsOrEnds(line, column)) {
                return method;
            }
        }

        if (node instanceof BoundParameterListNode parameters) {
            for (BoundParameterNode parameter : parameters.parameters) {
                if (parameter.getRange().containsOrEnds(line, column)) {
                    return getUnfinished(parameter, line, column);
                }
            }
            return null;
        }

        if (node instanceof BoundParameterNode parameter) {
            if (parameter.getTypeNode().getRange().containsOrEnds(line, column)) {
                return parameter.getTypeNode();
            } else if (parameter.getName().getRange().containsOrEnds(line, column)) {
                return parameter.getName();
            } else {
                return null;
            }
        }

        /*if (node instanceof BoundForEachLoopStatementNode loop) {
            if (loop.iterable.getRange().endsWith(line, column)) {
                return getUnfinished(loop.iterable, line, column);
            } else {
                return null;
            }
        }*/
        if (node instanceof BoundAssignmentStatementNode assignment) {
            if (assignment.right.getRange().endsWith(line, column)) {
                return getUnfinished(assignment.right, line, column);
            } else {
                return null;
            }
        }
        if (node instanceof BoundAugmentedAssignmentStatementNode assignment) {
            if (assignment.right.getRange().endsWith(line, column)) {
                return getUnfinished(assignment.right, line, column);
            } else {
                return null;
            }
        }

        if (node instanceof BoundStatementNode) {
            // check if cursor at the end of statement
            if (!node.getRange().endsWith(line, column)) {
                return null;
            }

            if (node instanceof BoundExpressionStatementNode expressionStatement) {
                return getUnfinished(expressionStatement.expression, line, column);
            }

            if (node instanceof BoundIfStatementNode ifStatement) {
                if (ifStatement.elseStatement != null) {
                    return getUnfinished(ifStatement.elseStatement, line, column);
                } else {
                    return getUnfinished(ifStatement.thenStatement, line, column);
                }
            }

            if (node instanceof BoundVariableDeclarationNode declaration) {
                if (declaration.expression != null && declaration.expression.getRange().endsWith(line, column)) {
                    return getUnfinished(declaration.expression, line, column);
                }
            }

            if (node instanceof BoundAssignmentStatementNode assignment) {
                if (assignment.right.getRange().containsOrEnds(line, column)) {
                    return getUnfinished(assignment.right, line, column);
                }
            }

            if (node instanceof BoundReturnStatementNode returnStatement) {
                if (returnStatement.expression != null && returnStatement.expression.getRange().endsWith(line, column)) {
                    return getUnfinished(returnStatement.expression, line, column);
                }
            }
        }

        if (node instanceof BoundExpressionNode) {
            if (node instanceof BoundPropertyAccessExpressionNode propertyAccess) {
                if (propertyAccess.property.getRange().endsWith(line, column)) {
                    return propertyAccess;
                }
            }
            if (node instanceof BoundImplicitCastExpressionNode implicitCast) {
                return getUnfinished(implicitCast.operand, line, column);
            }
            if (node instanceof BoundAwaitExpressionNode awaitExpression) {
                return getUnfinished(awaitExpression.expression, line, column);
            }
            if (node instanceof BoundBinaryExpressionNode binary) {
                if (binary.left.getRange().containsOrEnds(line, column)) {
                    return getUnfinished(binary.left, line, column);
                }
                if (binary.right.getRange().containsOrEnds(line, column)) {
                    return getUnfinished(binary.right, line, column);
                }
                if (binary.right.getRange().isBefore(line, column)) { // there can be space
                    return getUnfinished(binary.right, line, column);
                }
            }
            if (node instanceof BoundMethodInvocationExpressionNode invocation) {
                if (invocation.arguments.getRange().containsOrEnds(line, column)) {
                    for (BoundExpressionNode argument : invocation.arguments.arguments) {
                        return getUnfinished(argument, line, column);
                    }
                }
            }
            if (node instanceof BoundNameExpressionNode name) {
                return name;
            }
            if (node instanceof BoundInvalidExpressionNode) {
                return node;
            }
        }

        return null;
    }

    private CompletionContext getCompletionContext(BoundCompilationUnitNode unit, int line, int column) {
        SearchEntry entry = find(null, unit, line, column);
        if (entry == null) {
            if (unit.getRange().isAfter(line, column)) {
                return new CompletionContext(ContextType.BEFORE_FIRST, line, column);
            }
            if (unit.getRange().isBefore(line, column)) {
                if (unit.getRange().endsWith(line, column)) {
                    return getAtLastContext(unit, line, column);
                } else {
                    return new CompletionContext(ContextType.AFTER_LAST, line, column);
                }
            }
            return new CompletionContext(ContextType.NO_CODE, line, column);
        } else {
            return new CompletionContext(entry, line, column);
        }
    }

    private CompletionContext getAtLastContext(BoundCompilationUnitNode unit, int line, int column) {
        if (unit.statements.statements.isEmpty()) {
            return new CompletionContext(ContextType.AFTER_LAST, line, column);
        }

        SearchEntry root = new SearchEntry(null, unit);
        SearchEntry child = new SearchEntry(root, unit.statements);

        return new CompletionContext(child, line, column);
    }

    private List<T> getSymbols(CompilationParameters parameters, BinderOutput output, CompletionContext context) {
        List<T> list = new ArrayList<>();

        if (context.entry == null) {
            addStaticConstants(list, output.context());
            if (context.type == ContextType.AFTER_LAST) {
                addCompilationUnitMembers(list, output.unit().members.members);
                addLocalVariables(list, output.unit().statements.statements);
                addInputParameters(list, parameters);
            }
            if (context.type == ContextType.NO_CODE) {
                addInputParameters(list, parameters);
            }
            return list;
        }

        while (context != null) {
            if (context.entry == null) {
                break;
            }
            switch (context.entry.node.getNodeType()) {
                case COMPILATION_UNIT -> {
                    addStaticConstants(list, output.context());
                    addCompilationUnitMembers(list, output.unit().members.members);
                    /*if (context.prev != null) {
                        if (context.prev.getNodeType() == NodeType.MEMBER_ACCESS_EXPRESSION) {
                            addCompilationUnitMembers(list, output.unit().members.members);
                            addInputParameters(list, parameters);
                        }
                        if (context.prev.getNodeType() == NodeType.COMPILATION_UNIT_MEMBERS) {
                            addInputParameters(list, parameters);
                        }
                    }*/
                }
                case FUNCTION -> {
                    BoundFunctionNode function = (BoundFunctionNode) context.entry.node;
                    for (BoundParameterNode parameter : function.parameters.parameters) {
                        addLocalVariableSuggestion(list, (LocalVariable) parameter.getName().symbol);
                    }
                }
                case STATEMENTS_LIST -> {
                    addLocalVariables(list, getStatementsPriorTo(context));
                    addInputParameters(list, parameters);
                }
                case LAMBDA_EXPRESSION -> {
                    BoundLambdaExpressionNode lambda = (BoundLambdaExpressionNode) context.entry.node;
                    for (BoundParameterNode parameter : lambda.parameters) {
                        addLocalVariableSuggestion(list, (LocalVariable) parameter.getName().symbol);
                    }
                }
                case FOREACH_LOOP_STATEMENT -> {
                    BoundForEachLoopStatementNode loop = (BoundForEachLoopStatementNode) context.entry.node;
                    addLocalVariableSuggestion(list, (LocalVariable) loop.name.symbol);
                }
                default -> {
                    addLocalVariables(list, getStatementsPriorTo(context));
                }
            }

            context = context.up();
        }

        return list;
    }

    private List<BoundStatementNode> getStatementsPriorTo(CompletionContext context) {
        BoundNode parent = context.entry.node;
        BoundNode prev = context.prev;
        if (prev == null) {
            return List.of();
        }

        boolean unfinished = getUnfinished(prev, context.line, context.column) != null;

        List<BoundStatementNode> nodes = new ArrayList<>();
        List<BoundNode> children = parent.getChildren();
        for (BoundNode node : children) {
            if (unfinished && (node == prev)) {
                // when we have unfinished node, prev == current
                break;
            }
            if (node instanceof BoundStatementNode statement) {
                nodes.add(statement);
            }
            if (!unfinished && (node == prev)) {
                break;
            }
        }

        return nodes;
    }

    private List<T> getStatementStart(CompilationParameters parameters, BinderOutput output, CompletionContext completionContext) {
        List<T> suggestions = new ArrayList<>();
        suggestions.add(factory.getKeywordSuggestion(TokenType.LET));
        suggestions.add(factory.getKeywordSuggestion(TokenType.FOR));
        suggestions.add(factory.getKeywordSuggestion(TokenType.FOREACH));
        suggestions.add(factory.getKeywordSuggestion(TokenType.IF));
        suggestions.add(factory.getKeywordSuggestion(TokenType.WHILE));
        suggestions.add(factory.getKeywordSuggestion(TokenType.RETURN));

        boolean isInsideLoop = false;
        SearchEntry entry = completionContext.entry;
        while (entry != null) {
            boolean isFunctionBoundary =
                    entry.node.getNodeType() == NodeType.FUNCTION ||
                    entry.node.getNodeType() == NodeType.LAMBDA_EXPRESSION;
            if (isFunctionBoundary) {
                break;
            }
            boolean isLoopStatement =
                    entry.node.getNodeType() == NodeType.FOR_LOOP_STATEMENT ||
                    entry.node.getNodeType() == NodeType.FOREACH_LOOP_STATEMENT ||
                    entry.node.getNodeType() == NodeType.WHILE_LOOP_STATEMENT;
            if (isLoopStatement) {
                isInsideLoop = true;
                break;
            }
            entry = entry.parent;
        }

        if (isInsideLoop) {
            suggestions.add(factory.getKeywordSuggestion(TokenType.BREAK));
            suggestions.add(factory.getKeywordSuggestion(TokenType.CONTINUE));
        }

        return suggestions;
    }

    private void addStaticConstants(List<T> suggestions, CompilerContext context) {
        for (Symbol symbol : context.getStaticSymbols()) {
            if (symbol instanceof StaticFieldConstantStaticVariable constant) {
                suggestions.add(factory.getStaticConstantSuggestion(constant));
            }
        }
    }

    private void addCompilationUnitMembers(List<T> suggestions, List<BoundCompilationUnitMemberNode> members) {
        for (BoundCompilationUnitMemberNode member : members) {
            if (member.getNodeType() == NodeType.STATIC_FIELD) {
                suggestions.add(factory.getStaticFieldSuggestion((DeclaredStaticVariable) ((BoundStaticFieldNode) member).declaration.name.symbol));
            } else if (member.getNodeType() == NodeType.FUNCTION) {
                suggestions.add(factory.getFunctionSuggestion((Function) ((BoundFunctionNode) member).name.symbol));
            } else {
                throw new InternalException();
            }
        }
    }

    private void addLocalVariables(List<T> suggestions, List<BoundStatementNode> statements) {
        for (BoundStatementNode statement : statements) {
            if (statement instanceof BoundVariableDeclarationNode declaration) {
                if (declaration.name.symbol instanceof LocalVariable local) {
                    addLocalVariableSuggestion(suggestions, local);
                }
                // can be lifted variable?
            }
        }
    }

    private void addInputParameters(List<T> suggestions, CompilationParameters parameters) {
        for (Parameter parameter : InterfaceHelper.getFuncInterfaceMethod(parameters.getFunctionalInterface()).getParameters()) {
            suggestions.add(factory.getInputParameterSuggestion(parameter.getName(), SType.fromJavaType(parameter.getType())));
        }
    }

    private SearchEntry find(SearchEntry parent, BoundNode node, int line, int column) {
        if (node.getRange().containsOrEnds(line, column) && node.getRange().getLength() > 0) {
            SearchEntry entry = new SearchEntry(parent, node);
            for (BoundNode child : node.getChildren()) {
                if (child.getRange().containsOrEnds(line, column) && child.getRange().getLength() > 0) {
                    return find(entry, child, line, column);
                }
            }
            return entry;
        } else {
            return null;
        }
    }

    private boolean isSingleWordStatementStart(CompletionProvider.SearchEntry entry, int line, int column) {
        if (entry.node.getNodeType() == NodeType.NAME_EXPRESSION) {
            if (entry.parent.node.getNodeType() == NodeType.EXPRESSION_STATEMENT) {
                if (entry.node.getRange().containsOrEnds(line, column)) {
                    return true;
                }
            }
        }
        if (entry.node.getNodeType() == NodeType.PREDEFINED_TYPE) {
            if (entry.parent.node.getNodeType() == NodeType.VARIABLE_DECLARATION) {
                BoundVariableDeclarationNode declaration = (BoundVariableDeclarationNode) entry.parent.node;
                return declaration.name.value.isEmpty() && declaration.expression == null;
            }
        }
        if (entry.node.getNodeType() == NodeType.IF_STATEMENT) {
            BoundIfStatementNode statement = (BoundIfStatementNode) entry.node;
            return  statement.lParen.getRange().isEmpty() &&
                    statement.condition.getRange().isEmpty() &&
                    statement.rParen.getRange().isEmpty() &&
                    statement.thenStatement.getNodeType() == NodeType.INVALID_STATEMENT &&
                    statement.elseStatement == null;
        }
        if (entry.node.getNodeType() == NodeType.FOR_LOOP_STATEMENT) {
            BoundForLoopStatementNode statement = (BoundForLoopStatementNode) entry.node;
            return  statement.lParen.getRange().isEmpty() &&
                    statement.init.getNodeType() == NodeType.INVALID_STATEMENT &&
                    statement.condition.getRange().isEmpty() &&
                    statement.update.getNodeType() == NodeType.INVALID_STATEMENT &&
                    statement.rParen.getRange().isEmpty() &&
                    statement.body.getNodeType() == NodeType.INVALID_STATEMENT;
        }
        if (entry.node.getNodeType() == NodeType.FOREACH_LOOP_STATEMENT) {
            BoundForEachLoopStatementNode statement = (BoundForEachLoopStatementNode) entry.node;
            return  statement.lParen.getRange().isEmpty() &&
                    statement.typeNode.getNodeType() == NodeType.INVALID_TYPE &&
                    statement.name.value.isEmpty() &&
                    statement.iterable.getNodeType() == NodeType.INVALID_EXPRESSION &&
                    statement.rParen.getRange().isEmpty() &&
                    statement.body.getNodeType() == NodeType.INVALID_STATEMENT;
        }
        if (entry.node.getNodeType() == NodeType.WHILE_LOOP_STATEMENT) {
            BoundWhileLoopStatementNode statement = (BoundWhileLoopStatementNode) entry.node;
            return  statement.condition.getRange().isEmpty() &&
                    statement.body.getNodeType() == NodeType.INVALID_STATEMENT;
        }
        /*if (entry.node.getNodeType() == NodeType.RETURN_STATEMENT) {
            return false;
        }*/
        return false;
    }

    private void addLocalVariableSuggestion(List<T> suggestions, LocalVariable variable) {
        if (variable.getName() == null || variable.getName().isEmpty()) {
            return;
        }
        suggestions.add(factory.getLocalVariableSuggestion(variable));
    }

//    private String[] getClassesSuggestion(String prefix) {
//        getLoadedClasses();
//        return new String[0];
//    }

//    @SuppressWarnings("unchecked")
//    private List<Class<?>> getLoadedClasses() {
//        try {
//            ClassLoader loader = ClassLoader.getSystemClassLoader();
//            Field field = loader.getClass().getDeclaredField("classes");
//            field.setAccessible(true);
//            return List.copyOf((List<Class<?>>) field.get(loader));
//        } catch (Throwable e) {
//            return List.of();
//        }
//    }

    private record SearchEntry(SearchEntry parent, BoundNode node) {}

    private static class CompletionContext {

        public final ContextType type;
        public final SearchEntry entry;
        public final BoundNode prev;
        public final BoundNode next;
        public final int line;
        public final int column;

        public CompletionContext(ContextType type, int line, int column) {
            this.type = type;
            this.entry = null;
            this.prev = null;
            this.next = null;
            this.line = line;
            this.column = column;
        }

        public CompletionContext(SearchEntry entry, int line, int column) {
            this.type = ContextType.WITHIN;
            this.entry = entry;

            BoundNode prev = null;
            BoundNode next = null;
            List<BoundNode> children = entry.node.getChildren();
            for (int i = -1; i < children.size(); i++) {
                if (i < 0 || children.get(i).getRange().isBefore(line, column)) {
                    if (i + 1 >= children.size() || children.get(i + 1).getRange().isAfter(line, column)) {
                        prev = i >= 0 ? children.get(i) : null;
                        next = i < children.size() - 1 ? children.get(i + 1) : null;
                        break;
                    }
                    if (i + 1 >= children.size() || children.get(i + 1).getRange().contains(line, column)) {
                        prev = i >= 0 ? children.get(i) : null;
                        next = i < children.size() - 2 ? children.get(i + 2) : null;
                        break;
                    }
                }
            }

            this.prev = prev;
            this.next = next;
            this.line = line;
            this.column = column;
        }

        public CompletionContext up() {
            if (this.type != ContextType.WITHIN) {
                return null;
            }
            if (this.entry == null || this.entry.parent == null) {
                return null;
            }
            return new CompletionContext(this.entry.parent, line, column);
        }
    }

    private enum ContextType {
        NO_CODE,
        BEFORE_FIRST,
        AFTER_LAST,
        WITHIN
    }
}
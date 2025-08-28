package com.zergatul.scripting.completion;

import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.symbols.LocalVariable;

import java.util.ArrayList;
import java.util.List;

public class LocalVariablesCompletionProvider<T> extends AbstractCompletionProvider<T> {

    public LocalVariablesCompletionProvider(SuggestionFactory<T> factory) {
        super(factory);
    }

    @Override
    public List<T> provide(CompilationParameters parameters, BinderOutput output, CompletionContext context) {
        if (!context.canExpression()) {
            return List.of();
        }

        context = context.closestStatement(output);
        if (context == null) {
            return List.of();
        }

        List<T> suggestions = new ArrayList<>();
        for (BoundStatementNode statement : getStatementsPriorTo(context)) {
            if (statement instanceof BoundVariableDeclarationNode declaration) {
                // can be lifted variable?
                if (declaration.name.getSymbol() instanceof LocalVariable local) {
                    if (local.getName() == null || local.getName().isEmpty()) {
                        continue;
                    }
                    suggestions.add(factory.getLocalVariableSuggestion(local));
                }
            }
        }
        return suggestions;
    }

    private List<BoundStatementNode> getStatementsPriorTo(CompletionContext context) {
        if (context.entry == null) {
            return List.of();
        }

        List<BoundStatementNode> children = switch (context.entry.node.getNodeType()) {
            case STATEMENTS_LIST -> ((BoundStatementsListNode) context.entry.node).statements;
            case BLOCK_STATEMENT -> ((BoundBlockStatementNode) context.entry.node).statements;
            default -> null;
        };

        if (children != null) {
            List<BoundStatementNode> statements = new ArrayList<>();
            for (int i = 0; i < children.size(); i++) {
                BoundStatementNode statement = children.get(i);
                if (statement.getRange().getEnd().isAfter(context.line, context.column)) {
                    break;
                }
                boolean beforeNext = i == children.size() - 1 || children.get(i + 1).getRange().getStart().isBefore(context.line, context.column);
                if (beforeNext && statement.isOpen()) {
                    // we technically in the context of current statement, even though we are outside of its range
                    continue;
                }
                statements.add(statement);
            }
            statements.addAll(getFromParentScopes(context));
            return statements;
        }

        CompletionContext parent = context.closestStatement(null);
        if (parent == null || parent.entry == null) {
            return List.of();
        }

        if (parent.entry.node instanceof BoundStatementNode) {
            List<BoundStatementNode> statements = new ArrayList<>();
            for (BoundNode child : parent.entry.node.getChildren()) {
                if (child == context.entry.node) {
                    break;
                }
                if (child instanceof BoundStatementNode statement) {
                    statements.add(statement);
                }
            }
            statements.addAll(getFromParentScopes(parent));
            return statements;
        }

        return List.of();
    }

    private List<BoundStatementNode> getFromParentScopes(CompletionContext context) {
        CompletionContext parent = context.up();
        if (parent == null) {
            return List.of();
        }

        return getStatementsPriorTo(parent);
    }
}
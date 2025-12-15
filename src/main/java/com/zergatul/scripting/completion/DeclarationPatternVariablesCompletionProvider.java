package com.zergatul.scripting.completion;

import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.BoundIfStatementNode;
import com.zergatul.scripting.binding.nodes.BoundNodeType;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.symbols.SymbolRef;

import java.util.ArrayList;
import java.util.List;

public class DeclarationPatternVariablesCompletionProvider<T> extends AbstractCompletionProvider<T> {

    public DeclarationPatternVariablesCompletionProvider(SuggestionFactory<T> factory) {
        super(factory);
    }

    @Override
    public List<T> provide(CompilationParameters parameters, BinderOutput output, CompletionContext context) {
        if (!context.canExpression()) {
            return List.of();
        }

        List<T> suggestions = new ArrayList<>();

        while (context != null && !context.isFunctionBoundary()) {
            if (context.entry == null) {
                break;
            }

            if (context.entry.node.is(BoundNodeType.IF_STATEMENT)) {
                BoundIfStatementNode ifStatement = (BoundIfStatementNode) context.entry.node;
                // if cursor is inside then/else statement
                if (ifStatement.getRange().contains(context.line, context.column)) {
                    if (ifStatement.syntaxNode.closeParen.getRange().isBefore(context.line, context.column)) {
                        boolean wasInsideThenStatement;
                        if (ifStatement.elseStatement != null) {
                            assert ifStatement.syntaxNode.elseToken != null;
                            wasInsideThenStatement = ifStatement.syntaxNode.elseToken.getRange().isAfter(context.line, context.column);
                        } else {
                            wasInsideThenStatement = true;
                        }
                        if (wasInsideThenStatement) {
                            for (SymbolRef ref : ifStatement.flow.whenTrueLocals()) {
                                suggestions.add(factory.getLocalVariableSuggestion(ref.asLocalVariable()));
                            }
                        } else {
                            for (SymbolRef ref : ifStatement.flow.whenFalseLocals()) {
                                suggestions.add(factory.getLocalVariableSuggestion(ref.asLocalVariable()));
                            }
                        }
                    }
                }
            }

            context = context.up();
        }

        return suggestions;
    }
}

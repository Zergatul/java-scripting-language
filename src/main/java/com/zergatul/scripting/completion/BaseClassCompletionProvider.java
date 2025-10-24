package com.zergatul.scripting.completion;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.BoundClassNode;
import com.zergatul.scripting.binding.nodes.BoundCompilationUnitMemberNode;
import com.zergatul.scripting.binding.nodes.BoundNodeType;
import com.zergatul.scripting.binding.nodes.BoundTypeNode;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.symbols.ClassSymbol;

import java.util.ArrayList;
import java.util.List;

public class BaseClassCompletionProvider<T> extends AbstractCompletionProvider<T> {

    public BaseClassCompletionProvider(SuggestionFactory<T> factory) {
        super(factory);
    }

    @Override
    public List<T> provide(CompilationParameters parameters, BinderOutput output, CompletionContext context) {
        if (context.entry == null) {
            return List.of();
        }

        if (context.entry.node instanceof BoundTypeNode) {
            CompletionContext parent = context.up();
            if (parent != null && parent.entry != null && parent.entry.node.is(BoundNodeType.CLASS_DECLARATION)) {
                return provide(parameters, output, parent);
            }
        }

        if (context.entry.node.is(BoundNodeType.CLASS_DECLARATION)) {
            BoundClassNode currentClassNode = (BoundClassNode) context.entry.node;
            if (currentClassNode.syntaxNode.colon != null) {
                if (TextRange.isBetween(context.line, context.column, currentClassNode.syntaxNode.colon, currentClassNode.syntaxNode.openBrace)) {
                    List<T> suggestions = new ArrayList<>();
                    for (BoundCompilationUnitMemberNode memberNode : output.unit().members.members) {
                        if (memberNode.getNodeType() == BoundNodeType.CLASS_DECLARATION) {
                            BoundClassNode classNode = (BoundClassNode) memberNode;
                            if (classNode != currentClassNode && !classNode.name.value.isEmpty()) {
                                suggestions.add(factory.getClassSuggestion((ClassSymbol) classNode.name.getSymbol()));
                            }
                        }
                    }
                    return suggestions;
                }
            }
        }

        return List.of();
    }
}
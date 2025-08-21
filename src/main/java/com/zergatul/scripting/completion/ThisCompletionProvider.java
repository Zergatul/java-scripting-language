package com.zergatul.scripting.completion;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.BoundClassNode;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.symbols.ClassSymbol;

import java.util.List;

public class ThisCompletionProvider<T> extends AbstractCompletionProvider<T> {

    public ThisCompletionProvider(SuggestionFactory<T> factory) {
        super(factory);
    }

    @Override
    public List<T> provide(CompilationParameters parameters, BinderOutput output, CompletionContext context) {
        if (context.canExpression()) {
            if (context.entry == null) {
                return List.of();
            }

            for (CompletionContext current = context; current != null; current = current.up()) {
                NodeType nodeType = current.entry.node.getNodeType();
                if (nodeType == NodeType.CLASS_CONSTRUCTOR || nodeType == NodeType.CLASS_METHOD) {
                    current = current.up();
                    if (current.entry.node.getNodeType() != NodeType.CLASS) {
                        throw new InternalException();
                    }
                    BoundClassNode classNode = (BoundClassNode) current.entry.node;
                    ClassSymbol symbol = classNode.name.symbolRef.asClass();
                    return List.of(factory.getThisSuggestion(symbol.getDeclaredType()));
                }
            }
        }

        return List.of();
    }
}
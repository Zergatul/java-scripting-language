package com.zergatul.scripting.completion;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.BoundClassNode;
import com.zergatul.scripting.binding.nodes.BoundNodeType;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.JavaInteropPolicy;
import com.zergatul.scripting.symbols.ClassSymbol;
import com.zergatul.scripting.type.NativeMethodReference;

import java.util.ArrayList;
import java.util.List;

public class ClassMembersCompletionProvider<T> extends AbstractCompletionProvider<T> {

    public ClassMembersCompletionProvider(SuggestionFactory<T> factory) {
        super(factory);
    }

    @Override
    public List<T> provide(CompilationParameters parameters, BinderOutput output, CompletionContext context) {
        if (context.canExpression()) {
            if (context.entry == null) {
                return List.of();
            }

            for (CompletionContext current = context; current != null && current.entry != null; current = current.up()) {
                BoundNodeType nodeType = current.entry.node.getNodeType();
                if (nodeType == BoundNodeType.CLASS_CONSTRUCTOR || nodeType == BoundNodeType.CLASS_METHOD) {
                    current = current.up();
                    if (current.entry == null) {
                        break;
                    }

                    if (current.entry.node.getNodeType() != BoundNodeType.CLASS_DECLARATION) {
                        throw new InternalException();
                    }

                    BoundClassNode classNode = (BoundClassNode) current.entry.node;
                    ClassSymbol symbol = classNode.name.symbolRef.asClass();

                    List<T> suggestions = new ArrayList<>();
                    symbol.getDeclaredType().getInstanceProperties().forEach(p -> suggestions.add(factory.getPropertySuggestion(p)));
                    symbol.getDeclaredType().getInstanceMethods().stream()
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
                    return suggestions;
                }
            }
        }

        return List.of();
    }
}
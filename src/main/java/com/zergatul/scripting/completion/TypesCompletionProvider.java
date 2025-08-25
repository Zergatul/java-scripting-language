package com.zergatul.scripting.completion;

import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.BoundClassNode;
import com.zergatul.scripting.binding.nodes.BoundCompilationUnitMemberNode;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.symbols.ClassSymbol;
import com.zergatul.scripting.type.*;

import java.util.ArrayList;
import java.util.List;

public class TypesCompletionProvider<T> extends AbstractCompletionProvider<T> {

    private static final SType[] PREDEFINED_TYPES = new SType[] {
            SBoolean.instance,
            SInt8.instance,
            SInt16.instance,
            SInt.instance,
            SInt64.instance,
            SChar.instance,
            SFloat32.instance,
            SFloat.instance,
            SString.instance
    };

    public TypesCompletionProvider(SuggestionFactory<T> factory) {
        super(factory);
    }

    @Override
    public List<T> provide(CompilationParameters parameters, BinderOutput output, CompletionContext context) {
        if (context.canExpression()) {
            return getTypes(parameters, output);
        } else {
            return List.of();
        }
    }

    private List<T> getTypes(CompilationParameters parameters, BinderOutput output) {
        List<T> suggestions = new ArrayList<>();
        for (SType type : PREDEFINED_TYPES) {
            suggestions.addAll(factory.getTypeSuggestion(type));
        }
        for (Class<?> clazz : parameters.getCustomTypes()) {
            suggestions.add(factory.getCustomTypeSuggestion(clazz));
        }
        for (BoundCompilationUnitMemberNode memberNode : output.unit().members.members) {
            if (memberNode.getNodeType() == NodeType.CLASS) {
                BoundClassNode classNode = (BoundClassNode) memberNode;
                if (!classNode.name.value.isEmpty()) {
                    suggestions.add(factory.getClassSuggestion((ClassSymbol) classNode.name.getSymbol()));
                }
            }
        }
        return suggestions;
    }
}
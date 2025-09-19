package com.zergatul.scripting.tests.completion.suggestions;

import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.nodes.BoundClassNode;
import com.zergatul.scripting.NodeType;
import com.zergatul.scripting.symbols.ClassSymbol;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import org.junit.jupiter.api.Assertions;

public class ClassSuggestion extends Suggestion {

    private final ClassSymbol clazz;

    public ClassSuggestion(TestCompletionContext context, String name) {
        this(extract(context.output(), name));
    }

    public ClassSuggestion(ClassSymbol clazz) {
        this.clazz = clazz;
    }

    private static ClassSymbol extract(BinderOutput output, String name) {
        for (var memberNode : output.unit().members.members) {
            if (memberNode.getNodeType() == NodeType.CLASS_DECLARATION) {
                BoundClassNode classNode = (BoundClassNode) memberNode;
                if (classNode.name.value.equals(name)) {
                    return (ClassSymbol) classNode.name.getSymbol();
                }
            }
        }
        Assertions.fail();
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ClassSuggestion other) {
            return other.clazz == clazz;
        } else {
            return false;
        }
    }
}
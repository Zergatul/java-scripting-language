package com.zergatul.scripting.tests.completion.suggestions;

import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.binding.nodes.BoundVariableDeclarationNode;
import com.zergatul.scripting.symbols.LocalVariable;
import com.zergatul.scripting.tests.completion.TestCompletionContext;
import org.junit.jupiter.api.Assertions;

public class LocalVariableSuggestion extends Suggestion {

    private final LocalVariable variable;

    public LocalVariableSuggestion(TestCompletionContext context, String name) {
        this(extract(context.output(), name));
    }

    public LocalVariableSuggestion(LocalVariable variable) {
        this.variable = variable;
    }

    private static LocalVariable extract(BinderOutput output, String name) {
        LocalVariableVisitor visitor = new LocalVariableVisitor(name);
        output.unit().accept(visitor);
        Assertions.assertNotNull(visitor.result);
        return visitor.result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LocalVariableSuggestion other) {
            return other.variable == variable;
        } else {
            return false;
        }
    }

    private static class LocalVariableVisitor extends BinderTreeVisitor {

        public LocalVariable result;
        private final String name;

        public LocalVariableVisitor(String name) {
            this.name = name;
        }

        @Override
        public void visit(BoundVariableDeclarationNode node) {
            if (result == null && node.name.value.equals(name)) {
                result = (LocalVariable) node.name.symbol;
            }
        }
    }
}
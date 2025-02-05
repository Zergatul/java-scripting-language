package com.zergatul.scripting.tests.completion.suggestions;

import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.binding.nodes.BoundStaticFieldNode;
import com.zergatul.scripting.symbols.DeclaredStaticVariable;
import com.zergatul.scripting.tests.completion.TestCompletionContext;
import org.junit.jupiter.api.Assertions;

public class StaticFieldSuggestion extends Suggestion {

    private final DeclaredStaticVariable variable;

    public StaticFieldSuggestion(TestCompletionContext context, String name) {
        this(extract(context.output(), "x"));
    }

    public StaticFieldSuggestion(DeclaredStaticVariable variable) {
        this.variable = variable;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StaticFieldSuggestion other) {
            return other.variable == variable;
        } else {
            return false;
        }
    }

    private static DeclaredStaticVariable extract(BinderOutput output, String name) {
        DeclaredStaticVariableVisitor visitor = new DeclaredStaticVariableVisitor(name);
        output.unit().accept(visitor);
        Assertions.assertNotNull(visitor.result);
        return visitor.result;
    }

    private static class DeclaredStaticVariableVisitor extends BinderTreeVisitor {

        public DeclaredStaticVariable result;
        private final String name;

        public DeclaredStaticVariableVisitor(String name) {
            this.name = name;
        }

        @Override
        public void visit(BoundStaticFieldNode node) {
            if (result == null && node.declaration.name.value.equals(name)) {
                result = (DeclaredStaticVariable) node.declaration.name.symbol;
            }
        }
    }
}
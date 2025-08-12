package com.zergatul.scripting.tests.completion.suggestions;

import com.zergatul.scripting.binding.nodes.BoundCompilationUnitMemberNode;
import com.zergatul.scripting.binding.nodes.BoundCompilationUnitNode;
import com.zergatul.scripting.binding.nodes.BoundFunctionNode;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.symbols.Function;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import org.junit.jupiter.api.Assertions;

public class FunctionSuggestion extends Suggestion {

    private final Function function;

    public FunctionSuggestion(TestCompletionContext context, String name) {
        this(extract(context.output().unit(), name));
    }

    public FunctionSuggestion(Function function) {
        this.function = function;
    }

    private static Function extract(BoundCompilationUnitNode unit, String name) {
        for (BoundCompilationUnitMemberNode node : unit.members.members) {
            if (node.getNodeType() == NodeType.FUNCTION) {
                BoundFunctionNode function = (BoundFunctionNode) node;
                if (function.name.value.equals(name)) {
                    return (Function) function.name.getSymbol();
                }
            }
        }
        Assertions.fail();
        throw new AssertionError();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FunctionSuggestion other) {
            return other.function.equals(function);
        } else {
            return false;
        }
    }
}
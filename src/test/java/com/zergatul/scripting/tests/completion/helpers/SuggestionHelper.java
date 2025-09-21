package com.zergatul.scripting.tests.completion.helpers;

import com.zergatul.scripting.binding.nodes.BoundClassNode;
import com.zergatul.scripting.binding.nodes.BoundNodeType;
import com.zergatul.scripting.type.SDeclaredType;
import org.junit.jupiter.api.Assertions;

public class SuggestionHelper {

    public static SDeclaredType extractClassType(TestCompletionContext context, String className) {
        for (var memberNode : context.output().unit().members.members) {
            if (memberNode.getNodeType() == BoundNodeType.CLASS_DECLARATION) {
                BoundClassNode classNode = (BoundClassNode) memberNode;
                if (classNode.name.value.equals(className)) {
                    return (SDeclaredType) classNode.name.getSymbol().getType();
                }
            }
        }
        Assertions.fail();
        return null;
    }
}
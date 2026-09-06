package com.zergatul.scripting.tests.utility;

import com.zergatul.scripting.highlighting.SemanticToken;
import com.zergatul.scripting.utility.Lists;

import java.util.List;

public class SemanticDumper extends Dumper {

    public String dump(List<SemanticToken> tokens) {
        reset();

        for (SemanticToken token : tokens) {
            sb.append("new SemanticToken(SemanticTokenType.");
            sb.append(token.type().name());
            sb.append(", ");

            if (!token.modifiers().isEmpty()) {
                sb.append("Lists.of(");
                sb.append(String.join(", ", Lists.from(token.modifiers().stream().map(mod -> "SemanticTokenModifier." + mod))));
                sb.append("), ");
            }

            dump(token.range());
            sb.append(")");
            commaBreak();
        }

        return sb.toString();
    }
}
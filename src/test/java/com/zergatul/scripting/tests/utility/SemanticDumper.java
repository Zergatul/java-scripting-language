package com.zergatul.scripting.tests.utility;

import com.zergatul.scripting.highlighting.SemanticToken;

import java.util.List;

public class SemanticDumper extends Dumper {

    public String dump(List<SemanticToken> tokens) {
        reset();

        for (SemanticToken token : tokens) {
            sb.append("new SemanticToken(SemanticTokenType.");
            sb.append(token.type().name());
            sb.append(", ");
            dump(token.range());
            sb.append(")");
            commaBreak();
        }

        return sb.toString();
    }
}
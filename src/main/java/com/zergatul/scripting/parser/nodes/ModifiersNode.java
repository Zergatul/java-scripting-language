package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.NodeType;
import com.zergatul.scripting.lexer.TokenType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;
import java.util.Objects;

public class ModifiersNode extends ParserNode {

    public final List<Token> tokens;

    public ModifiersNode(List<Token> tokens, TextRange range) {
        super(NodeType.MODIFIERS, range);
        this.tokens = tokens;
    }

    public boolean isAsync() {
        return tokens.stream().anyMatch(t -> t.is(TokenType.ASYNC));
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {}

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ModifiersNode other) {
            return Objects.equals(other.tokens, tokens);
        } else {
            return false;
        }
    }
}
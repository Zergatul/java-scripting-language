package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public abstract class ParserNode implements Locatable {

    private final ParserNodeType nodeType;
    private final TextRange range;

    protected ParserNode(ParserNodeType nodeType, TextRange range) {
        this.nodeType = nodeType;
        this.range = range;
    }

    public ParserNodeType getNodeType() {
        return nodeType;
    }

    public TextRange getRange() {
        return this.range;
    }

    public boolean is(ParserNodeType nodeType) {
        return this.nodeType == nodeType;
    }

    public boolean isNot(ParserNodeType nodeType) {
        return this.nodeType != nodeType;
    }

    public abstract void accept(ParserTreeVisitor visitor);
    public abstract void acceptChildren(ParserTreeVisitor visitor);

    public boolean isOpen() {
        return false;
    }

    public abstract List<Locatable> getChildNodes();

    public String asFullSource(String code) {
        StringBuilder builder = new StringBuilder();
        for (Locatable node : getChildNodes()) {
            if (node instanceof Token token) {
                builder.append(token.asFullSource(code));
            }
            if (node instanceof ParserNode syntaxNode) {
                builder.append(syntaxNode.asFullSource(code));
            }
        }
        return builder.toString();
    }
}
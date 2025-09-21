package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.ParserTreeVisitor;

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
}
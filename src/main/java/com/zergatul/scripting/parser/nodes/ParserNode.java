package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Node;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public abstract class ParserNode extends Node {

    protected ParserNode(NodeType nodeType, TextRange range) {
        super(nodeType, range);
    }

    public abstract void accept(ParserTreeVisitor visitor);
    public abstract void acceptChildren(ParserTreeVisitor visitor);

    public boolean isOpen() {
        return false;
    }
}
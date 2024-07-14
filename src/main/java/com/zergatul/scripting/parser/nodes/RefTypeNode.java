package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserVisitor;

public class RefTypeNode extends TypeNode {

    public final TypeNode underlying;

    public RefTypeNode(TypeNode underlying, TextRange range) {
        super(NodeType.REF_TYPE, range);
        this.underlying = underlying;
    }

    @Override
    public void accept(ParserVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserVisitor visitor) {
        underlying.accept(visitor);
    }
}
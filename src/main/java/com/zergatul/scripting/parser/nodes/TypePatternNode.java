package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.List;

public class TypePatternNode extends PatternNode {

    public final TypeNode typeNode;

    public TypePatternNode(TypeNode typeNode) {
        super(ParserNodeType.TYPE_PATTERN, typeNode.getRange());
        this.typeNode = typeNode;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        typeNode.accept(visitor);
    }

    @Override
    public List<Locatable> getChildNodes() {
        return List.of(typeNode);
    }
}
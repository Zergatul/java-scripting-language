package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class DeclarationPatternNode extends PatternNode {

    public final VariableDeclarationNode declaration;

    public DeclarationPatternNode(VariableDeclarationNode declaration, TextRange range) {
        super(NodeType.DECLARATION_PATTERN, range);
        this.declaration = declaration;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        declaration.accept(visitor);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DeclarationPatternNode other) {
            return other.declaration.equals(declaration) && other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}
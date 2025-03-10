package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class StaticFieldNode extends CompilationUnitMemberNode {

    public final VariableDeclarationNode declaration;

    public StaticFieldNode(VariableDeclarationNode declaration, TextRange range) {
        super(NodeType.STATIC_FIELD, range);
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
}
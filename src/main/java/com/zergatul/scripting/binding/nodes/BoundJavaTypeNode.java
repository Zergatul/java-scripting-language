package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.nodes.JavaQualifiedTypeNameNode;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundJavaTypeNode extends BoundTypeNode {

    public final Token lBracket;
    public final JavaQualifiedTypeNameNode name;
    public final Token rBracket;

    public BoundJavaTypeNode(Token lBracket, JavaQualifiedTypeNameNode name, Token rBracket, SType type, TextRange range) {
        super(BoundNodeType.JAVA_TYPE, type, range);
        this.lBracket = lBracket;
        this.name = name;
        this.rBracket = rBracket;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {}

    @Override
    public List<BoundNode> getChildren() {
        return List.of();
    }
}
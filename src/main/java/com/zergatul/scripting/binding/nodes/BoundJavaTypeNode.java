package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.nodes.JavaQualifiedTypeNameNode;
import com.zergatul.scripting.parser.nodes.JavaTypeNode;
import com.zergatul.scripting.type.SType;

import java.util.List;

public class BoundJavaTypeNode extends BoundTypeNode {

    public final Token java;
    public final Token openBracket;
    public final JavaQualifiedTypeNameNode name;
    public final Token closeBracket;

    public BoundJavaTypeNode(JavaTypeNode node, SType type) {
        this(node.java, node.openBracket, node.name, node.closeBracket, type, node.getRange());
    }

    public BoundJavaTypeNode(Token java, Token openBracket, JavaQualifiedTypeNameNode name, Token closeBracket, SType type, TextRange range) {
        super(BoundNodeType.JAVA_TYPE, type, range);
        this.java = java;
        this.openBracket = openBracket;
        this.name = name;
        this.closeBracket = closeBracket;
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
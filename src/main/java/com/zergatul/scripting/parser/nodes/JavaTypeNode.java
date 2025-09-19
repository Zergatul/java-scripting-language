package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.NodeType;
import com.zergatul.scripting.parser.ParserTreeVisitor;

public class JavaTypeNode extends TypeNode {

    public final Token lBracket;
    public final JavaQualifiedTypeNameNode name;
    public final Token rBracket;

    public JavaTypeNode(Token lBracket, JavaQualifiedTypeNameNode name, Token rBracket, TextRange range) {
        super(NodeType.JAVA_TYPE, range);
        this.lBracket = lBracket;
        this.name = name;
        this.rBracket = rBracket;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        name.accept(visitor);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JavaTypeNode other) {
            return  other.lBracket.equals(lBracket) &&
                    other.name.equals(name) &&
                    other.rBracket.equals(rBracket) &&
                    other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}
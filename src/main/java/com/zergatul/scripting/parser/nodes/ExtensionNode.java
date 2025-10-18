package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.ParserTreeVisitor;

import java.util.ArrayList;
import java.util.List;

public class ExtensionNode extends CompilationUnitMemberNode {

    public final Token keyword;
    public final Token openParen;
    public final TypeNode typeNode;
    public final Token closeParen;
    public final Token openBrace;
    public final List<ClassMethodNode> methods;
    public final Token closeBrace;

    public ExtensionNode(Token keyword, Token openParen, TypeNode typeNode, Token closeParen, Token openBrace, List<ClassMethodNode> methods, Token closeBrace) {
        super(ParserNodeType.EXTENSION_DECLARATION, TextRange.combine(keyword, closeBrace));
        this.keyword = keyword;
        this.openParen = openParen;
        this.typeNode = typeNode;
        this.closeParen = closeParen;
        this.openBrace = openBrace;
        this.methods = methods;
        this.closeBrace = closeBrace;
    }

    @Override
    public void accept(ParserTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(ParserTreeVisitor visitor) {
        typeNode.accept(visitor);
        for (ClassMemberNode method : methods) {
            method.accept(visitor);
        }
    }

    @Override
    public List<Locatable> getChildNodes() {
        List<Locatable> nodes = new ArrayList<>();
        nodes.add(keyword);
        nodes.add(openParen);
        nodes.add(typeNode);
        nodes.add(closeParen);
        nodes.add(openBrace);
        nodes.addAll(methods);
        nodes.add(closeBrace);
        return nodes;
    }
}
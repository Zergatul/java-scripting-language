package com.zergatul.scripting.visitors;

import com.zergatul.scripting.parser.ParserTreeVisitor;
import com.zergatul.scripting.parser.nodes.DeclarationPatternNode;

public class PatternVariablesVisitor extends ParserTreeVisitor {

    private boolean hasDeclarationPattern;

    public boolean hasDeclarationPattern() {
        return hasDeclarationPattern;
    }

    @Override
    public void explicitVisit(DeclarationPatternNode node) {
        super.explicitVisit(node);
        hasDeclarationPattern = true;
    }
}
package com.zergatul.scripting.parser;

import com.zergatul.scripting.parser.nodes.AwaitExpressionNode;

public class AsyncParserTreeVisitor extends ParserTreeVisitor {

    private boolean isAsync;

    public boolean isAsync() {
        return isAsync;
    }

    @Override
    public void visit(AwaitExpressionNode node) {
        isAsync = true;
    }
}
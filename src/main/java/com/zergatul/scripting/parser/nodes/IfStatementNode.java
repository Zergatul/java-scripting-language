package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.parser.NodeType;

import java.util.Objects;

public class IfStatementNode extends StatementNode {

    public final ExpressionNode condition;
    public final StatementNode thenStatement;
    public final StatementNode elseStatement;

    public IfStatementNode(ExpressionNode condition, StatementNode thenStatement, StatementNode elseStatement, TextRange range) {
        super(NodeType.IF_STATEMENT, range);
        this.condition = condition;
        this.thenStatement = thenStatement;
        this.elseStatement = elseStatement;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IfStatementNode other) {
            return  other.condition.equals(condition) &&
                    other.thenStatement.equals(thenStatement) &&
                    Objects.equals(other.elseStatement, elseStatement) &&
                    other.getRange().equals(getRange());
        } else {
            return false;
        }
    }
}
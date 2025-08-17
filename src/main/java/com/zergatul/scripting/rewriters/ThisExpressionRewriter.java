package com.zergatul.scripting.rewriters;

import com.zergatul.scripting.binding.BinderTreeRewriter;
import com.zergatul.scripting.binding.nodes.*;
import com.zergatul.scripting.symbols.LocalVariable;
import com.zergatul.scripting.symbols.MutableSymbolRef;
import com.zergatul.scripting.symbols.SymbolRef;
import com.zergatul.scripting.type.SType;
import com.zergatul.scripting.visitors.ThisExpressionVisitor;

import java.util.ArrayList;
import java.util.List;

public class ThisExpressionRewriter extends BinderTreeRewriter {

    private SType thisType;
    private LocalVariable thisVariable;
    private SymbolRef thisVariableRef;

    @Override
    public BoundClassMethodNode visit(BoundClassMethodNode node) {
        ThisExpressionVisitor visitor = new ThisExpressionVisitor();
        node.accept(visitor);
        if (!visitor.hasThis) {
            return node;
        }

        thisType = visitor.type;
        thisVariable = new LocalVariable("@this", visitor.type, null);
        thisVariableRef = new MutableSymbolRef(thisVariable);
        BoundNameExpressionNode name = new BoundNameExpressionNode(thisVariableRef);

        List<BoundStatementNode> statements = new ArrayList<>(node.body.statements.size() + 1);
        statements.add(new BoundVariableDeclarationNode(name, new BoundStackLoadNode(0, visitor.type)));
        for (BoundStatementNode statement : node.body.statements) {
            statements.add((BoundStatementNode) statement.accept(this));
        }

        BoundBlockStatementNode body = new BoundBlockStatementNode(statements, node.body.getRange());
        return node.update(body);
    }

    @Override
    public BoundExpressionNode visit(BoundThisExpressionNode node) {
        return new BoundNameExpressionNode(thisVariableRef);
    }
}
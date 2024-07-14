package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.binding.BinderTreeVisitor;
import com.zergatul.scripting.parser.NodeType;

import java.util.List;

public class BoundCompilationUnitNode extends BoundNode {

    public final BoundStaticVariablesListNode variables;
    public final BoundFunctionsListNode functions;
    public final BoundStatementsListNode statements;

    public BoundCompilationUnitNode(BoundStaticVariablesListNode variables, BoundFunctionsListNode functions, BoundStatementsListNode statements, TextRange range) {
        super(NodeType.COMPILATION_UNIT, range);
        this.variables = variables;
        this.functions = functions;
        this.statements = statements;
    }

    @Override
    public void accept(BinderTreeVisitor visitor) {
        visitor.explicitVisit(this);
    }

    @Override
    public void acceptChildren(BinderTreeVisitor visitor) {
        variables.accept(visitor);
        functions.accept(visitor);
        statements.accept(visitor);
    }

    @Override
    public List<BoundNode> getChildren() {
        return List.of(variables, functions, statements);
    }
}
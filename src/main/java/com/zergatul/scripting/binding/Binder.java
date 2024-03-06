package com.zergatul.scripting.binding;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.binding.nodes.BoundAssignmentStatementNode;
import com.zergatul.scripting.parser.ParserOutput;
import com.zergatul.scripting.parser.nodes.AssignmentStatementNode;
import com.zergatul.scripting.parser.nodes.CompilationUnitNode;
import com.zergatul.scripting.parser.nodes.StatementNode;

import java.util.List;

public class Binder {

    private final String code;
    private final CompilationUnitNode unit;
    private final List<DiagnosticMessage> diagnostics;

    public Binder(ParserOutput input) {
        this.code = input.code();
        this.unit = input.unit();
        this.diagnostics = input.diagnostics();
    }

    public BinderOutput bind() {
        return new BinderOutput(code, bindCompilationUnit(unit), diagnostics);
    }

    private CompilationUnitNode bindCompilationUnit(CompilationUnitNode node) {
        return new CompilationUnitNode(node.statements.stream().map(this::bindStatement).toList(), node.getRange());
    }

    private StatementNode bindStatement(StatementNode statement) {
        if (statement instanceof AssignmentStatementNode assignmentStatement) {
            return bindAssignmentStatement(assignmentStatement);
        }
        return statement;
    }

    private BoundAssignmentStatementNode bindAssignmentStatement(AssignmentStatementNode assignmentStatement) {
        return null;
    }
}
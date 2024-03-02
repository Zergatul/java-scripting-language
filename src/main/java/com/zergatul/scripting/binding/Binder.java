package com.zergatul.scripting.binding;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.parser.ParserOutput;
import com.zergatul.scripting.parser.nodes.CompilationUnitNode;

import java.util.List;

public class Binder {

    private final CompilationUnitNode unit;
    private final List<DiagnosticMessage> diagnostics;

    public Binder(ParserOutput input) {
        this.unit = input.unit();
        this.diagnostics = input.diagnostics();
    }

    public void bind() {

    }
}
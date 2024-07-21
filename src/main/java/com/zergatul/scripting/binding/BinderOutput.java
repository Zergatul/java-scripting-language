package com.zergatul.scripting.binding;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.binding.nodes.BoundCompilationUnitNode;
import com.zergatul.scripting.compiler.CompilerContext;

import java.util.List;

public record BinderOutput(
        String code,
        BoundCompilationUnitNode unit,
        CompilerContext context,
        List<DiagnosticMessage> diagnostics
) {}
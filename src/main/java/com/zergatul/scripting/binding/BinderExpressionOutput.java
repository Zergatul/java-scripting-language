package com.zergatul.scripting.binding;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.binding.nodes.BoundExpressionUnitNode;
import com.zergatul.scripting.compiler.CompilerContext;

import java.util.List;

public record BinderExpressionOutput(
        String code,
        BoundExpressionUnitNode unit,
        CompilerContext context,
        List<DiagnosticMessage> diagnostics
) {}
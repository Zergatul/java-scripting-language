package com.zergatul.scripting.binding;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.binding.nodes.BoundCompilationUnitNode;

import java.util.List;

public record BinderOutput(String code, BoundCompilationUnitNode unit, List<DiagnosticMessage> diagnostics) {}
package com.zergatul.scripting.binding;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.parser.nodes.CompilationUnitNode;

import java.util.List;

public record BinderOutput(String code, CompilationUnitNode unit, List<DiagnosticMessage> diagnostics) {}
package com.zergatul.scripting.parser;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.parser.nodes.CompilationUnitNode;

import java.util.List;

public record ParserOutput(CompilationUnitNode unit, List<DiagnosticMessage> diagnostics) {}

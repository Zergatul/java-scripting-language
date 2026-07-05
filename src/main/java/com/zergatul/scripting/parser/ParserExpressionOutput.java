package com.zergatul.scripting.parser;

import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.parser.nodes.ExpressionUnitNode;

import java.util.List;

public record ParserExpressionOutput(String code, ExpressionUnitNode unit, List<DiagnosticMessage> diagnostics) {}
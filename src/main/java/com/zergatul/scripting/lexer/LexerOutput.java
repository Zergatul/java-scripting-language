package com.zergatul.scripting.lexer;

import com.zergatul.scripting.DiagnosticMessage;

import java.util.List;

public record LexerOutput(String code, List<Line> lines, TokenQueue tokens, List<DiagnosticMessage> diagnostics) {}
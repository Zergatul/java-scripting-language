package com.zergatul.scripting.tests.completion.helpers;

import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.compiler.CompilationParameters;

public record TestCompletionContext(CompilationParameters parameters, BinderOutput output) {}
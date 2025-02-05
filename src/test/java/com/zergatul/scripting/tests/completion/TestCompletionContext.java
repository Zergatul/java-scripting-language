package com.zergatul.scripting.tests.completion;

import com.zergatul.scripting.binding.BinderOutput;
import com.zergatul.scripting.compiler.CompilationParameters;

public record TestCompletionContext(CompilationParameters parameters, BinderOutput output) {}
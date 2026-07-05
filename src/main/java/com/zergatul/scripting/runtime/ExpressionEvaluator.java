package com.zergatul.scripting.runtime;

@FunctionalInterface
public interface ExpressionEvaluator {
    ExpressionEvaluationResult evaluate();
}
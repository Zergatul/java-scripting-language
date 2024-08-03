package com.zergatul.scripting.runtime;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public interface AsyncStateMachine<T> {

    CompletableFuture<T> next(Object result);

    default Function<?, CompletableFuture<?>> getContinuation() {
        return this::next;
    }
}
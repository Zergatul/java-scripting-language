package com.zergatul.scripting.runtime;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@SuppressWarnings("unused")
public interface AsyncStateMachine<T> {

    CompletableFuture<T> next(Object result);

    default Runnable runContinuation() {
        return () -> this.next(null);
    }

    default Consumer<Object> acceptContinuation() {
        return this::next;
    }
}
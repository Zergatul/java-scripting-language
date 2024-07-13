package com.zergatul.scripting.runtime;

import java.util.concurrent.CompletableFuture;

public interface AsyncStateMachine<T> {

    CompletableFuture<T> next();

    default Runnable continuation() {
        return this::next;
    }
}
package com.zergatul.scripting.runtime;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public final class FutureUtils {

    public static <T> CompletableFuture<T> failedFuture(Throwable exception) {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.completeExceptionally(exception);
        return future;
    }
}
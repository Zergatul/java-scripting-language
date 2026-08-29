package com.zergatul.scripting.runtime;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class AsyncCancellationToken {

    private boolean cancelled;
    private CompletableFuture<?> awaited;

    public void setAwaited(CompletableFuture<?> future) {
        Objects.requireNonNull(future);

        boolean cancel;
        synchronized (this) {
            cancel = cancelled;
            if (!cancel) {
                awaited = future;
            }
        }

        if (cancel) {
            future.cancel(false);
        }
    }

    public void cancel(boolean mayInterruptIfRunning) {
        CompletableFuture<?> future;
        synchronized (this) {
            if (cancelled) {
                return;
            }
            cancelled = true;
            future = awaited;
        }

        if (future != null) {
            future.cancel(mayInterruptIfRunning);
        }
    }

    public synchronized boolean isCancelled() {
        return cancelled;
    }

    public <T> CompletableFuture<T> wrap(CompletableFuture<T> future) {
        Objects.requireNonNull(future);

        CancellationFuture<T> result = new CancellationFuture<>(this, future);
        future.whenComplete((value, throwable) -> {
            if (throwable == null) {
                result.complete(value);
            } else {
                result.completeExceptionally(throwable);
            }
        });
        if (isCancelled()) {
            result.cancel(false);
        }
        return result;
    }

    public <T> CompletableFuture<T> cancelledFuture() {
        CompletableFuture<T> future = new CompletableFuture<>();
        future.cancel(false);
        return future;
    }

    private static class CancellationFuture<T> extends CompletableFuture<T> {

        private final AsyncCancellationToken token;
        private final CompletableFuture<?> source;

        private CancellationFuture(AsyncCancellationToken token, CompletableFuture<?> source) {
            this.token = token;
            this.source = source;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (!super.cancel(mayInterruptIfRunning)) {
                return false;
            }

            token.cancel(mayInterruptIfRunning);
            source.cancel(mayInterruptIfRunning);
            return true;
        }
    }
}
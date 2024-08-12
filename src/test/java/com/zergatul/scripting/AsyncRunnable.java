package com.zergatul.scripting;

import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface AsyncRunnable {
    CompletableFuture<?> run();
}
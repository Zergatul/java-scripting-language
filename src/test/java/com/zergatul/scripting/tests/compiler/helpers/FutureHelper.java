package com.zergatul.scripting.tests.compiler.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

public class FutureHelper {

    private final List<CompletableFuture<Void>> manuals = new ArrayList<>();

    public CompletableFuture<Void> sleep(int milliseconds) {
        Executor executor = CompletableFuture.delayedExecutor(milliseconds, TimeUnit.MILLISECONDS);
        return CompletableFuture.runAsync(() -> {}, executor);
    }

    public CompletableFuture<Void> manual() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        manuals.add(future);
        return future;
    }

    public CompletableFuture<Void> getManualFuture(int index) {
        return manuals.get(index);
    }
}
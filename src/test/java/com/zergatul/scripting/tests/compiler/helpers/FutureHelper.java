package com.zergatul.scripting.tests.compiler.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FutureHelper {

    private final List<CompletableFuture<Void>> voids = new ArrayList<>();
    private final List<CompletableFuture<Integer>> ints = new ArrayList<>();

    public CompletableFuture<Void> create() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        voids.add(future);
        return future;
    }

    public CompletableFuture<Integer> createInt() {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        ints.add(future);
        return future;
    }

    public CompletableFuture<Void> get(int index) {
        return voids.get(index);
    }

    public CompletableFuture<Integer> getInt(int index) {
        return ints.get(index);
    }

    public int getVoidCount() {
        return voids.size();
    }
}
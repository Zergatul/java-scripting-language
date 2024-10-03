package com.zergatul.scripting.tests.compiler.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unused")
public class FutureHelper {

    private final List<CompletableFuture<Void>> voids = new ArrayList<>();
    private final List<CompletableFuture<Boolean>> bools = new ArrayList<>();
    private final List<CompletableFuture<Integer>> ints = new ArrayList<>();
    private final List<CompletableFuture<Long>> longs = new ArrayList<>();
    private final List<CompletableFuture<Double>> floats = new ArrayList<>();

    public CompletableFuture<Void> create() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        voids.add(future);
        return future;
    }

    public CompletableFuture<Boolean> createBool() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        bools.add(future);
        return future;
    }

    public CompletableFuture<Integer> createInt() {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        ints.add(future);
        return future;
    }

    public CompletableFuture<Long> createLong() {
        CompletableFuture<Long> future = new CompletableFuture<>();
        longs.add(future);
        return future;
    }

    public CompletableFuture<Double> createFloat() {
        CompletableFuture<Double> future = new CompletableFuture<>();
        floats.add(future);
        return future;
    }

    public CompletableFuture<Void> get(int index) {
        return voids.get(index);
    }

    public CompletableFuture<Boolean> getBool(int index) {
        return bools.get(index);
    }

    public CompletableFuture<Integer> getInt(int index) {
        return ints.get(index);
    }

    public CompletableFuture<Long> getLong(int index) {
        return longs.get(index);
    }

    public CompletableFuture<Double> getFloat(int index) {
        return floats.get(index);
    }

    public int getVoidCount() {
        return voids.size();
    }
}
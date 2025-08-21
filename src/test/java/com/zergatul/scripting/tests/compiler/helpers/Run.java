package com.zergatul.scripting.tests.compiler.helpers;

import java.util.ArrayList;
import java.util.List;

public class Run {

    private final List<Action1<String>> stringHandlers = new ArrayList<>();
    private final List<Action1<Integer>> intHandlers = new ArrayList<>();
    private final List<Action1<Boolean>> booleanHandlers = new ArrayList<>();
    private final List<Action1<Double>> floatHandlers = new ArrayList<>();
    private final List<Action2<Integer, String>> intStringHandlers = new ArrayList<>();

    public void skip(Runnable runnable) {}

    public void once(Runnable runnable) {
        runnable.run();
    }

    public void multiple(int count, Runnable runnable) {
        for (int i = 0; i < count; i++) {
            runnable.run();
        }
    }

    public void onString(Action1<String> handler) {
        stringHandlers.add(handler);
    }

    public void triggerString(String value) {
        stringHandlers.forEach(s -> s.invoke(value));
    }

    public void onBoolean(Action1<Boolean> handler) {
        booleanHandlers.add(handler);
    }

    public void triggerBoolean(boolean value) {
        booleanHandlers.forEach(s -> s.invoke(value));
    }

    public void onInteger(Action1<Integer> handler) {
        intHandlers.add(handler);
    }

    public void triggerInteger(int value) {
        intHandlers.forEach(s -> s.invoke(value));
    }

    public void onFloat(Action1<Double> handler) {
        floatHandlers.add(handler);
    }

    public void triggerFloat(double value) {
        floatHandlers.forEach(s -> s.invoke(value));
    }

    public void onIntString(Action2<Integer, String> handler) {
        intStringHandlers.add(handler);
    }

    public void triggerIntString(int value1, String value2) {
        intStringHandlers.forEach(s -> s.invoke(value1, value2));
    }

    public int sumInts(int count, Function0<Integer> getter) {
        int sum = 0;
        for (int i = 0; i < count; i++) {
            sum += getter.invoke();
        }
        return sum;
    }

    public double sumFloats(int count, Function0<Double> getter) {
        double sum = 0;
        for (int i = 0; i < count; i++) {
            sum += getter.invoke();
        }
        return sum;
    }

    public int[] map(int[] array, Function1<Integer, Integer> mapper) {
        int[] result = new int[array.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = mapper.invoke(array[i]);
        }
        return result;
    }

    public int reduce(int[] array, int initial, Function2<Integer, Integer, Integer> reducer) {
        for (int element : array) {
            initial = reducer.invoke(initial, element);
        }
        return initial;
    }

    @FunctionalInterface
    public interface Action1<T> {
        void invoke(T param1);
    }

    @FunctionalInterface
    public interface Action2<T1, T2> {
        void invoke(T1 param1, T2 param2);
    }

    @FunctionalInterface
    public interface Function0<R> {
        R invoke();
    }

    @FunctionalInterface
    public interface Function1<R, T> {
        R invoke(T param);
    }

    @FunctionalInterface
    public interface Function2<R, T1, T2> {
        R invoke(T1 param1, T2 param2);
    }
}
package com.zergatul.scripting.tests.compiler.helpers;

import com.zergatul.scripting.runtime.Action0;
import com.zergatul.scripting.runtime.Action1;
import com.zergatul.scripting.runtime.Action2;
import com.zergatul.scripting.runtime.Function0;

import java.util.ArrayList;
import java.util.List;

public class Run {

    private final List<Action1<String>> stringHandlers = new ArrayList<>();
    private final List<Action1<Integer>> intHandlers = new ArrayList<>();
    private final List<Action1<Boolean>> booleanHandlers = new ArrayList<>();
    private final List<Action1<Double>> floatHandlers = new ArrayList<>();
    private final List<Action2<Integer, String>> intStringHandlers = new ArrayList<>();

    public void skip(Action0 action) {

    }

    public void once(Action0 action) {
        action.invoke();
    }

    public void multiple(int count, Action0 action) {
        for (int i = 0; i < count; i++) {
            action.invoke();
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
}
package com.zergatul.scripting.helpers;

public class Run {

    public void skip(Runnable runnable) {

    }

    public void once(Runnable runnable) {
        runnable.run();
    }
}

package com.zergatul.scripting.runtime;

public interface Future {
    Future continueWith(Runnable runnable);
}
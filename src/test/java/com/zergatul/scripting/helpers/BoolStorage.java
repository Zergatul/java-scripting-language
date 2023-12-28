package com.zergatul.scripting.helpers;

import java.util.ArrayList;
import java.util.List;

public class BoolStorage {

    public final List<Boolean> list = new ArrayList<>();

    public void add(boolean value) {
        list.add(value);
    }
}

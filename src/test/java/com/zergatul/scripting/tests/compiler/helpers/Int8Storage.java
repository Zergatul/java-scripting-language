package com.zergatul.scripting.tests.compiler.helpers;

import java.util.ArrayList;
import java.util.List;

public class Int8Storage {

    public final List<Byte> list = new ArrayList<>();

    public void add(byte value) {
        list.add(value);
    }
}
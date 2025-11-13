package com.zergatul.scripting.type.operation;

import com.zergatul.scripting.type.SUnknown;

public class UndefinedIndexOperation extends IndexOperation {

    public static final IndexOperation instance = new UndefinedIndexOperation();

    private UndefinedIndexOperation() {
        super(SUnknown.instance, SUnknown.instance);
    }
}
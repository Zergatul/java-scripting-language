package com.zergatul.scripting.symbols;

import com.zergatul.scripting.type.MethodParameter;
import com.zergatul.scripting.type.SStaticFunction;
import com.zergatul.scripting.type.SUnknown;

public class UknownFunction extends Function {

    public static final Function instance = new UknownFunction();

    private UknownFunction() {
        super("", new SStaticFunction(SUnknown.instance, new MethodParameter[0]), null);
    }
}
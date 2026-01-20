package com.zergatul.scripting.symbols;

import com.zergatul.scripting.TextRange;
import com.zergatul.scripting.type.MethodParameter;
import com.zergatul.scripting.type.SStaticFunction;
import com.zergatul.scripting.type.SUnknown;

public class UnknownFunction extends Function {

    public static final Function instance = new UnknownFunction();

    private UnknownFunction() {
        super("", new SStaticFunction(SUnknown.instance, new MethodParameter[0]), TextRange.MISSING);
    }
}
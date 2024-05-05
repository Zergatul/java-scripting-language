package com.zergatul.scripting.compiler;

public class RefHolder {

    private final LocalVariable holder;
    private final Variable variable;

    public RefHolder(LocalVariable holder, Variable variable) {
        this.holder = holder;
        this.variable = variable;
    }

    public LocalVariable getHolder() {
        return holder;
    }

    public Variable getVariable() {
        return variable;
    }
}
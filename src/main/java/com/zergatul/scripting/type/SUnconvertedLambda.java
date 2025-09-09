package com.zergatul.scripting.type;

public class SUnconvertedLambda extends SSyntheticType {

    private final boolean canBeAction;
    private final boolean canBeFunction;
    private final int parametersCount;

    public SUnconvertedLambda(boolean canBeAction, boolean canBeFunction, int parametersCount) {
        this.canBeAction = canBeAction;
        this.canBeFunction = canBeFunction;
        this.parametersCount = parametersCount;
    }

    public boolean canBeAction() {
        return canBeAction;
    }

    public boolean canBeFunction() {
        return canBeFunction;
    }

    public int getParametersCount() {
        return parametersCount;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("fn<");
        if (parametersCount == 0) {
            builder.append("()");
        } else if (parametersCount == 1) {
            builder.append("?");
        } else {
            builder.append("(");
            for (int i = 0; i < parametersCount; i++) {
                builder.append("?");
                if (i < parametersCount - 1) {
                    builder.append(", ");
                }
            }
            builder.append(")");
        }
        builder.append(" => ");
        if (!canBeFunction && canBeAction) {
            builder.append("void");
        } else {
            builder.append("?");
        }
        builder.append(">");
        return builder.toString();
    }
}
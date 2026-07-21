package com.zergatul.scripting.type;

public class MemberModifiers {

    private static final int ASYNC = 0x01;
    private static final int ABSTRACT = 0x02;
    private static final int VIRTUAL = 0x04;
    private static final int OVERRIDE = 0x08;
    private static final int FINAL = 0x10;

    private final int flags;
    private final Visibility visibility;

    public MemberModifiers(boolean isAsync, boolean isAbstract, boolean isVirtual, boolean isOverride, boolean isFinal, Visibility visibility) {
        flags =
                (isAsync ? ASYNC : 0) |
                (isAbstract ? ABSTRACT : 0) |
                (isVirtual ? VIRTUAL : 0) |
                (isOverride ? OVERRIDE : 0) |
                (isFinal ? FINAL : 0);
        this.visibility = visibility;
    }

    public boolean isAsync() {
        return (flags & ASYNC) != 0;
    }

    public boolean isAbstract() {
        return (flags & ABSTRACT) != 0;
    }

    public boolean isVirtual() {
        return (flags & VIRTUAL) != 0;
    }

    public boolean isOverride() {
        return (flags & OVERRIDE) != 0;
    }

    public boolean isFinal() {
        return (flags & FINAL) != 0;
    }

    public Visibility getVisibility() {
        return visibility;
    }
}
package com.zergatul.scripting.type;

public abstract class MemberReference {
    public abstract String getName();

    public Visibility getVisibility() {
        return Visibility.PUBLIC;
    }

    public boolean isStatic() {
        return false;
    }
}
package com.zergatul.scripting.type;

import com.zergatul.scripting.InternalException;

public abstract class MemberReference {

    public abstract String getName();

    @Override
    public boolean equals(Object obj) {
        throw new InternalException();
    }
}
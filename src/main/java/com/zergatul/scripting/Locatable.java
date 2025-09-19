package com.zergatul.scripting;

public interface Locatable {

    TextRange getRange();

    default boolean isMissing() {
        return getRange().isEmpty();
    }
}
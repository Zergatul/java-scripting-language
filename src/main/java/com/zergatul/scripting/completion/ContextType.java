package com.zergatul.scripting.completion;

public enum ContextType {
    NO_CODE,
    BEFORE_FIRST_NO_MEMBERS,
    BEFORE_FIRST_WITH_MEMBERS,
    AFTER_LAST_NO_STATEMENTS,
    AFTER_LAST_WITH_STATEMENTS,
    WITHIN
}

package com.zergatul.scripting.completion;

@FunctionalInterface
public interface SuggestionMapper<T> {
    T map(SuggestionInfo suggestion);
}
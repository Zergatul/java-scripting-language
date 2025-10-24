package com.zergatul.scripting.tests.completion.suggestions;

import com.zergatul.scripting.tests.completion.helpers.SuggestionHelper;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.type.SType;

public class BaseSuggestion extends Suggestion {

    private final SType type;

    public BaseSuggestion(TestCompletionContext context, String name) {
        this(SuggestionHelper.extractClassType(context, name));
    }

    public BaseSuggestion(SType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BaseSuggestion other) {
            return other.type.equals(type);
        } else {
            return false;
        }
    }
}
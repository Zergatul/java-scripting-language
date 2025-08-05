package com.zergatul.scripting.tests.completion.suggestions;

import com.zergatul.scripting.tests.completion.helpers.SuggestionHelper;
import com.zergatul.scripting.tests.completion.helpers.TestCompletionContext;
import com.zergatul.scripting.type.SDeclaredType;

public class ThisSuggestion extends Suggestion {

    private final SDeclaredType type;

    public ThisSuggestion(TestCompletionContext context, String name) {
        this(SuggestionHelper.extractClassType(context, name));
    }

    public ThisSuggestion(SDeclaredType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ThisSuggestion other) {
            return other.type == type;
        } else {
            return false;
        }
    }
}
package com.zergatul.scripting.hover;

import com.zergatul.scripting.highlighting.SemanticTokenType;

public abstract class Theme {
    public abstract String getTokenColor(SemanticTokenType type);
    public abstract String getPredefinedTypeColor();
    public abstract String getTypeColor();
    public abstract String getMethodColor();
    public abstract String getDescriptionColor();
    public abstract String getParameterColor();
}
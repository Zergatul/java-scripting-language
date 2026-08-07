package com.zergatul.scripting.type;

import java.util.List;

public record SJavaWildcardTypeArgument(List<SType> upperBounds, List<SType> lowerBounds) implements SJavaTypeArgument {}
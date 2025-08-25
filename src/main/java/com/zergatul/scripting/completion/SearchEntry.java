package com.zergatul.scripting.completion;

import com.zergatul.scripting.binding.nodes.BoundNode;

public class SearchEntry {

    public final SearchEntry parent;
    public final BoundNode node;

    public SearchEntry(SearchEntry parent, BoundNode node) {
        this.parent = parent;
        this.node = node;
    }
}
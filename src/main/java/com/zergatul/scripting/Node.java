package com.zergatul.scripting;

public abstract class Node implements Locatable {

    private final TextRange range;

    protected Node(TextRange range) {
        this.range = range;
    }

    public TextRange getRange() {
        return this.range;
    }

    public void print(String prefix) {

    }

    @Override
    public boolean equals(Object obj) {
        throw new RuntimeException("Not implemented");
    }
}
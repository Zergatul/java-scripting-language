package com.zergatul.scripting.parser.nodes;

public abstract class Node {

    public void print(String prefix) {

    }

    @Override
    public boolean equals(Object obj) {
        throw new RuntimeException("Not implemented");
    }
}
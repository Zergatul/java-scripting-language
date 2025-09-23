package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.lexer.Token;

import java.util.ArrayList;
import java.util.List;

public final class SeparatedList<T extends ParserNode> {

    private static final SeparatedList<?> EMPTY = new SeparatedList<>();

    private final List<T> nodes;
    private final List<Token> commas;

    public SeparatedList() {
        this.nodes = new ArrayList<>();
        this.commas = new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    public static <T extends ParserNode> SeparatedList<T> of() {
        return (SeparatedList<T>) EMPTY;
    }

    public void add(T node) {
        nodes.add(node);
    }

    public void add(Token comma) {
        commas.add(comma);
    }

    public List<T> getNodes() {
        return nodes;
    }

    public List<Token> getCommas() {
        return commas;
    }

    public T getNodeAt(int i) {
        return nodes.get(i);
    }

    public Token getCommaAfter(int i) {
        return commas.get(i);
    }

    public int size() {
        return nodes.size();
    }
}
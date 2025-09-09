package com.zergatul.scripting.parser.nodes;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.Locatable;
import com.zergatul.scripting.lexer.Token;

import java.util.ArrayList;
import java.util.Arrays;
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

    @SuppressWarnings("unchecked")
    public static <T extends ParserNode> SeparatedList<T> of(Class<T> clazz, Locatable... params) {
        SeparatedList<T> list = new SeparatedList<>();
        for (int i = 0; i < params.length; i++) {
            Locatable item = params[i];
            if (i % 2 == 0) {
                if (clazz.isInstance(item) && item instanceof ParserNode node) {
                    list.add((T) node);
                } else {
                    throw new InternalException();
                }
            } else {
                if (item instanceof Token token) {
                    list.add(token);
                } else {
                    throw new InternalException();
                }
            }
        }
        return list;
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

    public List<Locatable> getChildNodes() {
        Locatable[] children = new Locatable[nodes.size() + commas.size()];
        for (int i = 0; i < nodes.size(); i++) {
            children[2 * i] = nodes.get(i);
        }
        for (int i = 0; i < commas.size(); i++) {
            children[2 * i + 1] = commas.get(i);
        }
        return Arrays.asList(children);
    }
}
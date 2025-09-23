package com.zergatul.scripting.binding.nodes;

import com.zergatul.scripting.InternalException;
import com.zergatul.scripting.lexer.Token;
import com.zergatul.scripting.parser.nodes.ParserNode;
import com.zergatul.scripting.parser.nodes.SeparatedList;

import java.util.ArrayList;
import java.util.List;

public final class BoundSeparatedList<T extends BoundNode> {

    private static final BoundSeparatedList<?> EMPTY = new BoundSeparatedList<>();

    private final List<T> nodes;
    private final List<Token> commas;

    public BoundSeparatedList() {
        this.nodes = new ArrayList<>();
        this.commas = new ArrayList<>();
    }

    @SuppressWarnings("unchecked")
    public static <T extends BoundNode> BoundSeparatedList<T> of() {
        return (BoundSeparatedList<T>) EMPTY;
    }

    public static <T extends BoundNode> BoundSeparatedList<T> of(List<T> nodes) {
        BoundSeparatedList<T> list = new BoundSeparatedList<T>();
        nodes.forEach(list::add);
        return list;
    }

    public static <T1 extends BoundNode, T2 extends ParserNode> BoundSeparatedList<T1> from(SeparatedList<T2> other, List<T1> replacement) {
        if (other.size() != replacement.size()) {
            throw new InternalException();
        }

        BoundSeparatedList<T1> list = new BoundSeparatedList<T1>();
        other.getCommas().forEach(list::add);
        replacement.forEach(list::add);
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
}
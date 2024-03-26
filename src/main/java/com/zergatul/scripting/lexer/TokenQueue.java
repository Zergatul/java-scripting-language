package com.zergatul.scripting.lexer;

import java.util.Iterator;
import java.util.List;

public class TokenQueue implements Iterable<Token> {

    private final List<Token> list;
    private int position;

    public TokenQueue(List<Token> list) {
        this.list = list;
        this.position = 0;
    }

    public Token next() {
        if (position < list.size()) {
            return list.get(position++);
        } else {
            return EndOfFileToken.instance;
        }
    }

    public Token peek(int n) {
        if (position + n - 1 < list.size()) {
            return list.get(position + n - 1);
        } else {
            return EndOfFileToken.instance;
        }
    }

    public int size() {
        return list.size() - position;
    }

    public int position() {
        return position;
    }

    public void rollback(int position) {
        this.position = position;
    }

    @Override
    public Iterator<Token> iterator() {
        return new TokenIterator();
    }

    private class TokenIterator implements Iterator<Token> {

        private int pos;

        public TokenIterator() {
            pos = position;
        }

        @Override
        public boolean hasNext() {
            return pos < list.size();
        }

        @Override
        public Token next() {
            return list.get(pos++);
        }
    }
}
package com.zergatul.scripting.utility;

import org.jspecify.annotations.NonNull;

import java.util.*;

public final class ImmutableList<T> implements List<T> {

    private final T[] items;

    public ImmutableList(T[] items) {
        this.items = items;
    }

    @Override
    public int size() {
        return items.length;
    }

    @Override
    public boolean isEmpty() {
        return items.length == 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof List) {
            List<?> other = (List<?>) obj;
            if (other.size() != items.length) {
                return false;
            }
            for (int i = 0; i < items.length; i++) {
                if (!Objects.equals(items[i], other.get(i))) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean contains(Object o) {
        for (T item : items) {
            if (Objects.equals(item, o)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public @NonNull Iterator<T> iterator() {
        return new ArrayIterator<>(items);
    }

    @Override
    public Object @NonNull [] toArray() {
        return Arrays.copyOf(items, items.length);
    }

    @Override
    public <T1> T1 @NonNull [] toArray(T1 @NonNull [] a) {
        if (a.length < items.length) {
            a = (T1[]) Arrays.copyOf(items, items.length, a.getClass());
        }
        System.arraycopy(items, 0, a, 0, items.length);
        return a;
    }

    @Override
    public boolean add(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, @NonNull Collection<? extends T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public T get(int index) {
        return items[index];
    }

    @Override
    public T set(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, T element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public T remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        for (int i = 0; i < items.length; i++) {
            if (Objects.equals(o, items[i])) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        for (int i = items.length - 1; i >= 0; i--) {
            if (Objects.equals(o, items[i])) {
                return i;
            }
        }

        return -1;
    }

    @Override
    public @NonNull ListIterator<T> listIterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NonNull ListIterator<T> listIterator(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NonNull List<T> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    private static class ArrayIterator<T> implements Iterator<T> {

        private final T[] array;
        private int index;

        public ArrayIterator(T[] array) {
            this.array = array;
            this.index = 0;
        }

        @Override
        public boolean hasNext() {
            return index < array.length;
        }

        @Override
        public T next() {
            return array[index++];
        }
    }
}
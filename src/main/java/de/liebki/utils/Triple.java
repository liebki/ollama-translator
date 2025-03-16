package de.liebki.utils;

public class Triple<U, T, V> {
    private final U first;
    private final T second;
    private final V third;

    public Triple(U first, T second, V third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public U getOne() {
        return first;
    }

    public T getTwo() {
        return second;
    }

    public V getThree() {
        return third;
    }
} 
package com.eerussianguy.blazemap.util;

public interface IConfigAdapter<T> {
    T get();

    void set(T value);
}

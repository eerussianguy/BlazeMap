package com.eerussianguy.blazemap.engine.threading;

@FunctionalInterface
public interface IThreadQueue {
    void submit(Runnable r);
}


package com.eerussianguy.blazemap.engine.async;

@FunctionalInterface
public interface IThreadQueue
{
    void submit(Runnable r);
}


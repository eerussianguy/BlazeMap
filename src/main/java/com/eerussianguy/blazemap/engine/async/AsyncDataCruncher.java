package com.eerussianguy.blazemap.engine.async;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.eerussianguy.blazemap.BlazeMap;

public final class AsyncDataCruncher {
    private final Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();
    private final Thread thread = new Thread(this::loop);
    private volatile boolean running = true;
    private final Object mutex = new Object();

    public AsyncDataCruncher(String name) {
        BlazeMap.LOGGER.info("Starting {} AsyncDataCruncher Thread", name);
        thread.setName(name + " AsyncDataCruncher");
        thread.setDaemon(true);
        thread.start();
        BlazeMap.LOGGER.info("Started {} AsyncDataCruncher Thread", name);
    }

    public void assertIsOnDataCruncherThread() {
        if(Thread.currentThread() != thread) {
            throw new IllegalStateException("Operation can only be performed in the AsyncDataCruncher thread");
        }
    }

    public IThreadAsserter getThreadAsserter() {
        return this::assertIsOnDataCruncherThread;
    }

    public void stop() {
        running = false;
    }

    public void submit(Runnable r) {
        tasks.add(r);
        synchronized(mutex) {
            mutex.notifyAll();
        }
    }

    private void loop() {
        while(running) {
            this.work();
            try {
                synchronized(mutex) {
                    mutex.wait();
                }
            }
            catch(InterruptedException ex) {
                // We can't tolerate interrupts on the worker threads as that messes up with java.nio
                // The runtime exception helps us realize if it is happening
                throw new RuntimeException(ex);
            }
        }
    }

    private void work() {
        while(!tasks.isEmpty()) {
            Runnable task = tasks.poll();
            try {task.run();}
            catch(Throwable t) {t.printStackTrace();}
        }
    }

    @FunctionalInterface
    public interface IThreadAsserter {
        void assertCurrentThread();
    }
}

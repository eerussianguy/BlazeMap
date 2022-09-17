package com.eerussianguy.blazemap.engine.async;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.eerussianguy.blazemap.BlazeMap;

public final class AsyncDataCruncher {
    private final Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();
    private volatile boolean running = true;
    private final Object mutex = new Object();
    private final LinkedList<Thread> threads = new LinkedList();

    public AsyncDataCruncher(String name) {
        int cores = Runtime.getRuntime().availableProcessors();
        BlazeMap.LOGGER.info("Starting {} {} AsyncDataCruncher Threads", cores, name);
        for(int i = 0; i < cores; i++) {
            Thread thread = new Thread(this::loop);
            thread.setName(name + " AsyncDataCruncher #" + i);
            thread.setDaemon(true);
            thread.start();
            threads.add(thread);
            BlazeMap.LOGGER.info("Started {}", thread.getName());
        }
        BlazeMap.LOGGER.info("Started {} {} AsyncDataCruncher Threads", cores, name);
    }

    public void assertIsOnDataCruncherThread() {
        if(!threads.contains(Thread.currentThread())) {
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
            if(task == null) continue;
            try {task.run();}
            catch(Throwable t) {t.printStackTrace();}
        }
    }

    @FunctionalInterface
    public interface IThreadAsserter {
        void assertCurrentThread();
    }
}

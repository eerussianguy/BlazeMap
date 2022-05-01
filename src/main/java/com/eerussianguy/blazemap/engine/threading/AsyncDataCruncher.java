package com.eerussianguy.blazemap.engine.threading;

import com.eerussianguy.blazemap.BlazeMap;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class AsyncDataCruncher {
    private final Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();
    private final Thread thread = new Thread(this::loop);
    private volatile boolean running = true;

    public AsyncDataCruncher(String name){
        BlazeMap.LOGGER.info("Starting {} AsyncDataCruncher Thread", name);
        thread.setName(name+" AsyncDataCruncher");
        thread.setDaemon(true);
        thread.start();
        BlazeMap.LOGGER.info("Started {} AsyncDataCruncher Thread", name);
    }

    public void assertIsOnDataCruncherThread(){
        if(Thread.currentThread() != thread){
            throw new IllegalStateException("Operation can only be performed in the AsyncDataCruncher thread");
        }
    }

    public IThreadAsserter getThreadAsserter(){
        return this::assertIsOnDataCruncherThread;
    }

    public void stop(){
        running = false;
    }

    public void submit(Runnable r){
        tasks.add(r);
        thread.interrupt();
    }

    @SuppressWarnings("BusyWait") // the thread is dedicated to this loop
    private void loop(){
        while(running){
            this.work();
            try{ Thread.sleep(30_000L); }
            catch(InterruptedException ignored){}
        }
    }

    private void work(){
        while(!tasks.isEmpty()){
            Runnable task = tasks.poll();
            try{ task.run(); }
            catch(Throwable t){ t.printStackTrace(); }
        }
    }

    @FunctionalInterface
    public interface IThreadAsserter{
        void assertCurrentThread();
    }
}

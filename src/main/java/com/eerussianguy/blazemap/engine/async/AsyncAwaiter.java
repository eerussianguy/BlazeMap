package com.eerussianguy.blazemap.engine.async;

public class AsyncAwaiter {
    private final Object mutex = new Object();
    private int jobs;
    private int done;

    public AsyncAwaiter() {}

    public AsyncAwaiter(int jobs) {
        setJobs(jobs);
    }

    public void setJobs(int jobs) {
        if(this.jobs > 0)
            throw new IllegalStateException("Job count already set");
        if(jobs <= 0)
            throw new IllegalArgumentException("Job count must be positive");
        this.jobs = jobs;
    }

    public void done() {
        synchronized(mutex) {
            done++;
            if(done >= jobs) {
                mutex.notifyAll();
            }
        }
    }

    public void await() {
        synchronized(mutex) {
            while(done < jobs) {
                try {mutex.wait();}
                catch(InterruptedException e) {e.printStackTrace();}
            }
        }
    }
}

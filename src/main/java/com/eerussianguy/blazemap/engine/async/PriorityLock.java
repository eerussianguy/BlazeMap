package com.eerussianguy.blazemap.engine.async;

public class PriorityLock {
    private boolean priorityWaiting;
    private final Object mutex = new Object();
    private Thread owner = null;
    private int level = 0;

    public void lock() {
        synchronized(mutex) {
            if(owner == Thread.currentThread()) {
                level++;
                return;
            }

            while(owner != null || priorityWaiting) {
                try {
                    mutex.wait();
                }
                catch(InterruptedException ignored) {}
            }
            owner = Thread.currentThread();
            level = 1;
        }
    }

    public void lockPriority() {
        synchronized(mutex) {
            if(owner == Thread.currentThread()) {
                level++;
                return;
            }

            priorityWaiting = true;
            try {
                while(owner != null) {
                    try {
                        mutex.wait();
                    }
                    catch(InterruptedException ignored) {}
                }
                owner = Thread.currentThread();
                level = 1;
            }
            finally {
                priorityWaiting = false;
            }
        }
    }

    public void unlock() {
        synchronized(mutex) {
            if(owner != Thread.currentThread()) {
                throw new IllegalMonitorStateException("Attempted to unlock PriorityLock owned by a different thread!");
            }
            level--;
            if(level == 0) {
                owner = null;
                mutex.notifyAll();
            }
        }
    }
}

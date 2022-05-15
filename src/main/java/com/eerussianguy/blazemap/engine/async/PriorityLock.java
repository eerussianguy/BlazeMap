package com.eerussianguy.blazemap.engine.async;

public class PriorityLock
{
    private boolean locked;
    private boolean priorityWaiting;
    private final Object mutex = new Object();

    public void lock()
    {
        synchronized (mutex)
        {
            while (locked || priorityWaiting)
            {
                try
                {
                    mutex.wait();
                }
                catch (InterruptedException ignored) {}
            }
            locked = true;
        }
    }

    public void lockPriority()
    {
        synchronized (mutex)
        {
            priorityWaiting = true;
            try
            {
                while (locked)
                {
                    try
                    {
                        mutex.wait();
                    }
                    catch (InterruptedException ignored) {}
                }
                locked = true;
            }
            finally
            {
                priorityWaiting = false;
            }
        }
    }

    public void unlock()
    {
        synchronized (mutex)
        {
            locked = false;
            mutex.notifyAll();
        }
    }
}

package com.eerussianguy.blazemap.engine.async;

import java.util.ArrayList;
import java.util.List;

import com.eerussianguy.blazemap.BlazeMap;

public class DebouncingThread {
    private final Thread thread;
    private final List<DebouncingDomain<?>> domains;

    public DebouncingThread(String name) {
        this.domains = new ArrayList<>();
        this.thread = new Thread(this::work, name + " Debouncer Thread");
        thread.setDaemon(true);
        thread.start();
        BlazeMap.LOGGER.info("Starting {} Debouncer Thread", name);
    }

    void add(DebouncingDomain<?> domain) {
        synchronized(domains) {
            if(!domains.contains(domain)) {
                domains.add(domain);
                domain.setThread(thread);
                thread.interrupt();
            }
        }
    }

    public void remove(DebouncingDomain<?> domain) {
        synchronized(domains) {
            domains.remove(domain);
        }
    }

    private void work() {
        while(true) {
            long next = Long.MAX_VALUE;
            synchronized(domains) {
                for(DebouncingDomain<?> domain : domains) {
                    long d = domain.pop();
                    if(d < next) next = d;
                }
            }
            long wait = System.currentTimeMillis() - next;
            if(wait > 0) try {
                Thread.sleep(wait);
            }
            catch(InterruptedException ignored) {}
        }
    }
}

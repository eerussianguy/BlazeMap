package com.eerussianguy.blazemap.engine.async;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class AsyncChain<I, O> {
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();

    public static class Root {
        private final IThreadQueue gameThreadQueue;
        private final IThreadQueue dataThreadQueue;

        public Root(AsyncDataCruncher asyncDataCruncher, IThreadQueue gameThreadQueue) {
            this.dataThreadQueue = asyncDataCruncher::submit;
            this.gameThreadQueue = gameThreadQueue;
        }

        public <O> AsyncChain<Void, O> startOnGameThread(Function<Void, O> fn) {
            return new AsyncChain<>(null, fn, gameThreadQueue, this);
        }

        public <O> AsyncChain<Void, O> startOnDataThread(Function<Void, O> fn) {
            return new AsyncChain<>(null, fn, dataThreadQueue, this);
        }

        public AsyncChain<Void, Void> startWithDelay(int ms) {
            return new AsyncChain<>(null, null, null, this) {
                @Override
                protected void execute(Void input) {
                    if(ms == 0) {
                        next.execute(null);
                    }
                    else {
                        SCHEDULER.schedule(() -> next.execute(null), ms, TimeUnit.MILLISECONDS);
                    }

                }
            };
        }

        public void runOnGameThread(Runnable r) {
            gameThreadQueue.submit(r);
        }

        public void runOnDataThread(Runnable r) {
            dataThreadQueue.submit(r);
        }
    }

    private final Root initiator;
    private final AsyncChain<?, ?> root;
    private final Function<I, O> fn;
    private final IThreadQueue threadQueue;
    protected AsyncChain<O, ?> next;
    private boolean closed = false;

    private AsyncChain(AsyncChain<?, ?> parent, Function<I, O> fn, IThreadQueue threadQueue, Root initiator) {
        if(parent == null) this.root = null;
        else if(parent.root == null) this.root = parent;
        else this.root = parent.root;
        this.fn = fn;
        this.threadQueue = threadQueue;
        this.initiator = initiator;
    }

    public <N> AsyncChain<O, N> thenOnGameThread(Function<O, N> fn) {
        return thenOnThread(fn, initiator.gameThreadQueue);
    }

    public <N> AsyncChain<O, N> thenOnDataThread(Function<O, N> fn) {
        return thenOnThread(fn, initiator.dataThreadQueue);
    }

    private <N> AsyncChain<O, N> thenOnThread(Function<O, N> fn, IThreadQueue threadQueue) {
        if(closed) throw new IllegalStateException("AsyncChain is already closed");
        closed = true;
        AsyncChain<O, N> next = new AsyncChain<>(this, fn, threadQueue, initiator);
        this.next = next;
        return next;
    }

    private AsyncChain<O, O> thenDelay(int ms) {
        if(closed) throw new IllegalStateException("AsyncChain is already closed");
        closed = true;
        AsyncChain<O, O> next = new AsyncChain<>(this, null, null, initiator) {
            @Override
            protected void execute(O input) {
                if(ms == 0) {
                    next.execute(input);
                }
                else {
                    SCHEDULER.schedule(() -> next.execute(input), ms, TimeUnit.MILLISECONDS);
                }
            }
        };
        this.next = next;
        return next;
    }

    protected void execute(I input) {
        threadQueue.submit(() -> {
            if(next != null) {
                next.execute(fn.apply(input));
            }
            else {
                fn.apply(input);
            }
        });
    }

    public void start() {
        if(root == null) {
            this.execute(null);
        }
        else {
            root.execute(null);
        }
    }

    private static class Pointer<T> {
        public T value;

        public Pointer() {}

        public Pointer(T value) {
            this.value = value;
        }
    }
}


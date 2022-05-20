package com.eerussianguy.blazemap.engine.async;

import java.util.function.Function;

public final class AsyncChain<I, O> {

    public static class Root {
        private final AsyncDataCruncher asyncDataCruncher;
        private final IThreadQueue gameThreadQueue;
        private final IThreadQueue dataThreadQueue;

        public Root(AsyncDataCruncher asyncDataCruncher, IThreadQueue gameThreadQueue) {
            this.asyncDataCruncher = asyncDataCruncher;
            this.dataThreadQueue = asyncDataCruncher::submit;
            this.gameThreadQueue = gameThreadQueue;
        }

        public <O> AsyncChain<Void, O> startOnGameThread(Function<Void, O> fn) {
            return new AsyncChain<>(null, fn, gameThreadQueue, this);
        }

        public <O> AsyncChain<Void, O> startOnDataThread(Function<Void, O> fn) {
            return new AsyncChain<>(null, fn, dataThreadQueue, this);
        }

        public void runOnGameThread(Runnable r) {
            gameThreadQueue.submit(r);
        }

        public void runOnDataThread(Runnable r) {
            dataThreadQueue.submit(r);
        }

        @SuppressWarnings("BusyWait") // this is blocking on purpose
        public void runOnGameThreadBlocking(Runnable task) {
            asyncDataCruncher.assertIsOnDataCruncherThread();
            Thread thread = Thread.currentThread();
            Pointer<Boolean> control = new Pointer<>(Boolean.FALSE);
            Pointer<Throwable> error = new Pointer<>();
            gameThreadQueue.submit(() -> {
                try {
                    task.run();
                }
                catch(Throwable t) {
                    error.value = t;
                }
                control.value = Boolean.TRUE;
                thread.interrupt();
            });
            while(control.value != Boolean.TRUE) {
                try {Thread.sleep(50);}
                catch(InterruptedException ignored) {}
            }
            if(error.value != null)
                throw new RuntimeException("Error executing task on game thread: " + error.value.getMessage(), error.value);
        }

        public <T> T getOnGameThreadBlocking(Function<Void, T> fn) {
            Pointer<T> pointer = new Pointer<>();
            runOnGameThreadBlocking(() -> pointer.value = fn.apply(null));
            return pointer.value;
        }
    }

    private final Root initiator;
    private final AsyncChain<?, ?> root;
    private final Function<I, O> fn;
    private final IThreadQueue threadQueue;
    private AsyncChain<O, ?> next;
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

    private void execute(I input) {
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


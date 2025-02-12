package com.badlogic.gdx.jnigen.runtime.gc;

import com.badlogic.gdx.jnigen.runtime.CHandler;
import com.badlogic.gdx.jnigen.runtime.pointer.Pointing;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.ref.ReferenceQueue;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class GCHandler {
    private static final boolean NO_GC_FREE = System.getProperty("com.badlogic.jnigen.gc_disabled", "false").equals("true");
    private static final boolean ENABLE_GC_LOG = System.getProperty("com.badlogic.jnigen.gc_log", "false").equals("true");
    protected static final ReferenceQueue<Object> REFERENCE_QUEUE = new ReferenceQueue<>();
    private static final Set<PointingPhantomReference> referenceHolder = Collections.synchronizedSet(new HashSet<PointingPhantomReference>());
    private static final Map<Long, AtomicInteger> countMap = new HashMap<>();

    private static final Thread RELEASER = new Thread() {
        @Override
        public void run() {
            while (true) {
                try {
                    PointingPhantomReference releasedStructRef = (PointingPhantomReference)REFERENCE_QUEUE.remove();
                    if (!referenceHolder.remove(releasedStructRef)) {
                        System.err.println("Reference holder did not contained released StructRef.");
                    }

                    synchronized (countMap) {
                        AtomicInteger counter = countMap.get(releasedStructRef.getPointer());
                        int count = counter.decrementAndGet();
                        if (count <= 0) {
                            if (ENABLE_GC_LOG)
                                System.out.println("Freeing Pointer: " + releasedStructRef.getPointer());
                            countMap.remove(releasedStructRef.getPointer());
                            CHandler.free(releasedStructRef.getPointer());
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    static {
        RELEASER.setDaemon(true);
        RELEASER.setName("jnigen release thread");
        RELEASER.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                e.printStackTrace(); // Better logging :/
                System.exit(1);
            }
        });
        RELEASER.start();
    }

    public static void enqueuePointer(Object pointing, long pointer) {
        if (NO_GC_FREE)
            return;
        if (ENABLE_GC_LOG)
            System.out.println("Enqueuing Pointer: " + pointer);

        synchronized (countMap) {
            AtomicInteger counter = countMap.get(pointer);
            if (counter == null) {
                counter = new AtomicInteger(0);
                countMap.put(pointer, counter);
            }
            counter.incrementAndGet();
        }

        PointingPhantomReference structPhantomReference = new PointingPhantomReference(pointing, pointer);
        referenceHolder.add(structPhantomReference);
    }

    public static int nativeObjectCount() {
        return referenceHolder.size();
    }

    public static boolean isEnqueued(long pointer) {
        synchronized (countMap) {
            return countMap.containsKey(pointer);
        }
    }
}

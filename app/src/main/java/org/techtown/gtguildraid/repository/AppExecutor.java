package org.techtown.gtguildraid.repository;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppExecutor {
    private static final Object LOCK = new Object();
    private static AppExecutor sInstance;
    private final Executor diskIO;

    public AppExecutor(Executor diskIO, Executor mainThread,
                       Executor networkIO) {
        this.diskIO = diskIO;
    }

    public static AppExecutor getInstance() {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new AppExecutor(Executors.newSingleThreadExecutor(),
                        Executors.newFixedThreadPool(3), new MainThreadExecutor());
            }
        }
        return sInstance;
    }

    public Executor diskIO() {
        return diskIO;
    }

    private static class MainThreadExecutor implements Executor {
        @Override
        public void execute(@NonNull Runnable runnable) {
        }
    }
}
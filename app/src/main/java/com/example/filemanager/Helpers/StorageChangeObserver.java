package com.example.filemanager.Helpers;

import android.os.FileObserver;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;

public class StorageChangeObserver extends FileObserver {
    private static final long DEBOUNCE_DELAY_MS = 500;
    private final Handler handler;
    private final Runnable callback;

    public StorageChangeObserver(String path, Handler handler, Runnable callback) {
        super(path, FileObserver.CREATE | FileObserver.DELETE | FileObserver.MODIFY | FileObserver.MOVED_TO | FileObserver.MOVED_FROM);
        this.handler = handler;
        this.callback = callback;
    }
    @Override
    public void onEvent(int i, @Nullable String path) {
        if (path != null) {
           // Log.d("StorageObserver", "File change detected: " + path);
            handler.removeCallbacks(callback);
            handler.postDelayed(callback, DEBOUNCE_DELAY_MS);
        }

    }
}

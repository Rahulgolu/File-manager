package com.example.filemanager.utils;

import android.content.Context;
import android.net.Uri;
import androidx.core.content.FileProvider;
import java.io.File;

public class FileUtils {

    public static boolean renameFile(File file, String newName) {
        File newFile = new File(file.getParent(), newName);
        return file.renameTo(newFile);
    }

    public static boolean deleteFile(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteFile(child);
                }
            }
        }
        return file.delete();
    }

    public static Uri getFileUri(Context context, File file) {
        return FileProvider.getUriForFile(
                context.getApplicationContext(),
                context.getApplicationContext().getPackageName() + ".provider",
                file
        );
    }
}

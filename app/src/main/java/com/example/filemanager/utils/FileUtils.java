package com.example.filemanager.utils;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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

    public static boolean moveFile(File sourceFile, File targetDir) {
        if (!targetDir.isDirectory()) {
            return false;
        }
        File newFile = new File(targetDir, sourceFile.getName());
        return sourceFile.renameTo(newFile);
    }

    public static void copyFile(File sourceFile, File targetDir) {
        if (!targetDir.isDirectory()){
            return;
        }

        File targetFile = new File(targetDir,sourceFile.getName());
        try (FileInputStream inStream = new FileInputStream(sourceFile);
             FileOutputStream outStream = new FileOutputStream(targetFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inStream.read(buffer)) > 0) {
                outStream.write(buffer, 0, length);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

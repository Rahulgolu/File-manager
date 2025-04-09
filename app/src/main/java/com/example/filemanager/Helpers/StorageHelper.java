package com.example.filemanager.Helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StorageHelper {

    private static final String PREF_NAME = "CategorySizeCache";
    private static final String TIMESTAMP_KEY = "LastUpdateTimestamp";

    public static long getCachedCategorySize(Context context, String category) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getLong(category, -1); // Returns -1 if no cached value
    }

    public static void updateCategorySizeCache(Context context, String category, long size) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(category, size);
        editor.putLong(TIMESTAMP_KEY, System.currentTimeMillis()); // Store last update time
        editor.apply();
    }

    public static boolean shouldRecomputeSizes(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        long lastUpdateTime = prefs.getLong(TIMESTAMP_KEY, 0);
        long currentTime = System.currentTimeMillis();
        // Recompute if 24 hours have passed
        return (currentTime - lastUpdateTime) > 24 * 60 * 60 * 1000;
    }

   /* public static long computeCategorySize(String category) {
        File storageDir = Environment.getExternalStorageDirectory();
        return getFilesSizeRecursive(storageDir, category);
    }

    private static long getFilesSizeRecursive(File dir, String category) {
        long totalSize = 0;
        if (dir != null && dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        totalSize += getFilesSizeRecursive(file, category);
                    } else if (matchesCategory(file, category)) {
                        totalSize += file.length();
                    }
                }
            }
        }
        return totalSize;
    }*/

    private static boolean matchesCategory(File file, String category) {
        String name = file.getName().toLowerCase();
        switch (category) {
            case "Images":
                return name.endsWith(".jpg") || name.endsWith(".png") || name.endsWith(".gif");
            case "Audio":
                return name.endsWith(".mp3") || name.endsWith(".wav");
            case "Videos":
                return name.endsWith(".mp4") || name.endsWith(".mkv");
            case "Apps":
                return name.endsWith(".apk");
            case "Document":
                return name.endsWith(".pdf") || name.endsWith(".docx");
            case "Download":
                return file.getAbsolutePath().contains(Environment.DIRECTORY_DOWNLOADS);
            default:
                return false;
        }
    }

    public static String formatSize(long size) {
        if (size < 1024) return size + " B";
        // determine unit convert (kB, Mb,)
        int exp = (int) (Math.log(size) / Math.log(1024));
        // base on expo value pick correct unit and format
        String units = "KMGTPE".charAt(exp - 1) + "B";
        return String.format("%.1f %s", size / Math.pow(1024, exp), units);
    }

    public static List<File> getFilesByCategory(Context context,String category) {

        List<File> fileList = new ArrayList<>();
        Uri collection;
        String[] projection = {MediaStore.Files.FileColumns.DATA};
        String selection = null;
        String selectionArgs[] = null;

        switch (category){
            case "Images":
                collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                break;
            case "Videos":
                collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                break;
            case "Audio":
                collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                break;
            case "Document":
                collection = MediaStore.Files.getContentUri("external");
                selection = MediaStore.Files.FileColumns.MIME_TYPE + " LIKE 'application/pdf' OR " +
                        MediaStore.Files.FileColumns.MIME_TYPE + " LIKE 'application/vnd.openxmlformats-officedocument.wordprocessingml.document'";
                break;
            case "Download":
                collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
                break;
            case "Apps":
                collection = MediaStore.Files.getContentUri("external");
                selection = MediaStore.Files.FileColumns.MIME_TYPE + " LIKE 'application/vnd.android.package-archive'";
                break;
            default:
                collection = MediaStore.Files.getContentUri("external");

        }

        try {
            Cursor cursor = context.getContentResolver().
                    query(collection,projection,selection,selectionArgs,null);
            if (cursor !=  null){
                int colunmIndex = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA);
                while (cursor.moveToNext()){
                    String filePath = cursor.getString(colunmIndex);
                    File file = new File(filePath);
                    if (file.exists()){
                        fileList.add(file);
                    }
                }
            }
        }catch (Exception e){

        }

        return fileList;

        /*File storageDir = Environment.getExternalStorageDirectory();
        List<File> categoryFiles = new ArrayList<>();
        getFilesRecursive(storageDir, category, categoryFiles);
        return categoryFiles;*/
    }

    public static long computeCategorySize(Context context, String category) {
        Uri collectionUri;
        String[] projection = { MediaStore.MediaColumns.SIZE };
        String selection = null;
        String[] selectionArgs = null;

        switch (category.toLowerCase()) {
            case "images":
                collectionUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                break;
            case "videos":
                collectionUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                break;
            case "audio":
                collectionUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                break;
            case "documents":
                collectionUri = MediaStore.Files.getContentUri("external");
                selection = MediaStore.Files.FileColumns.MIME_TYPE + " IN (?, ?, ?, ?)";
                selectionArgs = new String[] {
                        "application/pdf", "application/msword",
                        "application/vnd.ms-excel", "text/plain"
                };
                break;
            default:
                return 0;
        }

        long totalSize = 0;
        try (Cursor cursor = context.getContentResolver().query(
                collectionUri,
                projection,
                selection,
                selectionArgs,
                null
        )) {
            if (cursor != null) {
                int sizeIndex = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE);
                while (cursor.moveToNext()) {
                    totalSize += cursor.getLong(sizeIndex);
                }
            }
        }
        return totalSize;
    }

    /*private static void getFilesRecursive(File dir, String category, List<File> categoryFiles) {
        if (dir != null && dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        getFilesRecursive(file, category, categoryFiles);
                    } else if (matchesCategory(file, category)) {
                        categoryFiles.add(file);
                    }
                }
            }
        }
    }*/

}

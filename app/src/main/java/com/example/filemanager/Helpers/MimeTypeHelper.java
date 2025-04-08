package com.example.filemanager.Helpers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.filemanager.R;
import com.example.filemanager.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MimeTypeHelper {

    public static String getMimeType(File file) {
        String extension = getFileExtension(file);
        if (extension != null) {
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            if (mimeType != null) {
                return mimeType;
            }
        }

        return URLConnection.guessContentTypeFromName(file.getName());
    }

    public static String getFileExtension(File file) {
        String name = file.getName();
        int lastIndex = name.lastIndexOf(".");
        if (lastIndex > 0) {
            return name.substring(lastIndex + 1).toLowerCase();
        }
        return null;
    }

    public static boolean isImage(File file) {
        String mimeType = getMimeType(file);
        return mimeType != null && mimeType.startsWith("image/");
    }

    public static boolean isVideo(File file) {
        String mimeType = getMimeType(file);
        return mimeType != null && mimeType.startsWith("video/");
    }

    public static boolean isAudio(File file) {
        String mimeType = getMimeType(file);
        return mimeType != null && mimeType.startsWith("audio/");
    }

    public static boolean isPdf(File file) {
        String mimeType = getMimeType(file);
        return "application/pdf".equals(mimeType);
    }

    public static boolean isApk(File file) {
        String mimeType = getMimeType(file);
        return "application/vnd.android.package-archive".equals(mimeType);
    }

    public static void openFile(Context context, File file) {

        try {
            Uri fileUri = FileUtils.getFileUri(context,file);
            String mimeType = MimeTypeHelper.getMimeType(file);

            if (mimeType == null) {
                Toast.makeText(context, "Unsupported file type", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(fileUri, mimeType);
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Cannot open file", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    public static void loadVideoThumbnailAsync(File file, ImageView imageView){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            Bitmap videoThumbnail = getVideoThumbnail(file);
            handler.post(() -> {
                if (videoThumbnail != null) {
                    imageView.setImageBitmap(videoThumbnail);
                }
                else {
                    imageView.setImageResource(R.drawable.ic_vedio);
                }
            });
        });

    }

    public static Bitmap getVideoThumbnail(File file) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(file.getAbsolutePath());
            return retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC); // 1st sec frame
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                retriever.release();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

}

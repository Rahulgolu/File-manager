package com.example.filemanager.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.example.filemanager.FileExplorerActivity;
import com.example.filemanager.Helpers.MimeTypeHelper;
import com.example.filemanager.R;
import com.example.filemanager.utils.FileUtils;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileAdapter extends AbstractItem <FileAdapter.ViewHolder> {
    private final File files;
    private final Context context;
    private boolean isSearchMode = false;

    public FileAdapter(File files, Context context) {
        this.files = files;
        this.context = context;
    }

    public void setSearchMode(boolean searchMode) {
        this.isSearchMode = searchMode;
    }

    public File getFile() {
        return files;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    public int getType() {
        return R.id.llfile;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_file;
    }

    public class ViewHolder extends FastAdapter.ViewHolder<FileAdapter> {
        private TextView fileNameTextView, fileDateTextView;
        private ImageView imageView,moreIcon;
        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.fileIcon);
            fileNameTextView = itemView.findViewById(R.id.fileName);
            fileDateTextView = itemView.findViewById(R.id.fileDate);
            moreIcon = itemView.findViewById(R.id.moreicon);
        }

        @Override
        public void bindView(FileAdapter item, List<?> list) {
            File file = item.getFile();
            String mimeType = MimeTypeHelper.getMimeType(file);

            fileNameTextView.setText(file.getName());
            String formattedDate = new SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(file.lastModified());
            fileDateTextView.setText(formattedDate);

            if (file.isDirectory()) {
                imageView.setImageResource(R.drawable.folder);
              //  setFolderPreview(file);
            } else {
                setFileIcon(file, mimeType);
            }

            // Change background based on selection state
            if (item.isSelected()) {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.grey)); // Fixed
            } else {
                itemView.setBackgroundColor(Color.TRANSPARENT);
            }


            if (moreIcon != null) {
                if (item.isSearchMode) {
                    moreIcon.setVisibility(View.GONE);
                } else {
                    moreIcon.setVisibility(View.VISIBLE);
                }
            }
            Log.d("FileAdapter", "moreIcon visibility: " + moreIcon.getVisibility() + " | isSearchMode: " + item.isSearchMode);

        }

        /*private void setFolderPreview(File file) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (MimeTypeHelper.isImage(f)) {
                        Uri fileUri = FileUtils.getFileUri(context,f);
                        Glide.with(context).load(fileUri).into(imageView);
                        break;
                    }
                }
            }
        }*/

        private void setFileIcon(File file, String mimeType) {
            if (MimeTypeHelper.isPdf(file)) {
                imageView.setImageResource(R.drawable.ic_picture_as_pdf);
            } else if (MimeTypeHelper.isAudio(file)) {
                imageView.setImageResource(R.drawable.ic_audio);
                loadAudioAlbumArtAsync(file, imageView);
            } else if (MimeTypeHelper.isVideo(file)) {
                MimeTypeHelper.loadVideoThumbnailAsync(file, imageView);
            } else if (MimeTypeHelper.isImage(file)) {
                Uri fileUri = FileUtils.getFileUri(context,file);
                Glide.with(context).load(fileUri).into(imageView);
            } else if (MimeTypeHelper.isApk(file)) {
                imageView.setImageResource(R.mipmap.ic_launcher);
            } else {
                imageView.setImageResource(R.drawable.folder);
            }
        }


        private void loadAudioAlbumArtAsync(File file, ImageView imageView) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(() -> {
                Bitmap albumArt = getAudioAlbumArt(file);
                handler.post(() -> {
                    if (albumArt != null) {
                        imageView.setImageBitmap(albumArt);
                    }
                });
            });
        }

        private Bitmap getAudioAlbumArt(File file) {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                retriever.setDataSource(file.getAbsolutePath());
                byte[] art = retriever.getEmbeddedPicture();
                if (art != null) {
                    return BitmapFactory.decodeByteArray(art, 0, art.length);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    retriever.release();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        }

        @Override
        public void unbindView(FileAdapter item) {
            fileNameTextView.setText(null);
            fileDateTextView.setText(null);
            imageView.setImageDrawable(null);

        }

    }

}

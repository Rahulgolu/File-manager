package com.example.filemanager.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.filemanager.Helpers.MimeTypeHelper;
import com.example.filemanager.R;
import com.example.filemanager.utils.FileUtils;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecentAdapter extends AbstractItem<RecentAdapter.ViewHolder> {
    private FileAdapter file;
    private final Context context;

    public RecentAdapter(FileAdapter file, Context context) {
        this.file = file;
        this.context = context;
    }

    public File getFile() {
        return file.getFile();
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_recent;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(@NonNull View view) {
        return new ViewHolder(view);
    }

    @Override
    public int getType() {
        return R.id.recentll;
    }

    class ViewHolder extends FastAdapter.ViewHolder<RecentAdapter> {
        private ImageView recentImage;
        private TextView txt;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recentImage = itemView.findViewById(R.id.recentImg);
            txt = itemView.findViewById(R.id.txt);
        }

        @Override
        public void bindView(@NonNull RecentAdapter item, @NonNull List<?> list) {

            File file = item.getFile();
            txt.setText(file.getName());

            if (file.isFile()) {
                Uri fileUri = FileUtils.getFileUri(context, file);
                if (MimeTypeHelper.isImage(file)) {
                    Glide.with(context)
                            .load(fileUri).into(recentImage);
                } else if (MimeTypeHelper.isVideo(file)) {
                    MimeTypeHelper.loadVideoThumbnailAsync(file, recentImage);
                }
            }
        }

        @Override
        public void unbindView(@NonNull RecentAdapter item) {
            recentImage.setImageDrawable(null);
            txt.setText(null);

        }
    }
}

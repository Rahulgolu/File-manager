package com.example.filemanager;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filemanager.Adapter.FileAdapter;
import com.example.filemanager.Helpers.MimeTypeHelper;
import com.example.filemanager.Helpers.StorageHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryFilesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FastAdapter<FileAdapter> fastAdapter;
    private ItemAdapter<FileAdapter> itemAdapter;
    private List<FileAdapter> fileItems = new ArrayList<>();
    private String category;
    private MaterialToolbar toolbar;
    private TextView toolbarTitle;
    private ExecutorService executorService;
    private Handler mainHandler;
    private ImageView backArrow;
    private ProgressBar progressBar;
    private ContentObserver mediaobserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_category_files);

        recyclerView = findViewById(R.id.recyclerView);
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressBar);
        toolbarTitle = findViewById(R.id.toolbarTitle);
        backArrow = findViewById(R.id.backButton);
        setSupportActionBar(toolbar);
        viewCompact();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        itemAdapter = new ItemAdapter<>();
        fastAdapter = FastAdapter.with(itemAdapter);
        recyclerView.setAdapter(fastAdapter);

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        category = getIntent().getStringExtra("category");


        if (category != null) {
            toolbarTitle.setText(category);
            loadFilesAsync(category);
        }
        // it will observe if any changes in files
        mediaobserver(category);

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        fastAdapter.setOnClickListener((v, adapter, item, position) -> {

            File file=item.getFile();
            MimeTypeHelper.openFile(this,file);
            return true;
        });

    }

    private void mediaobserver(String category) {
        Uri uri = null;
        switch (category){
            case "Images":
                uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                break;
            case "Videos":
                uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                break;
            case "Audio":
                uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                break;
            case "Document":
                uri = MediaStore.Files.getContentUri("external");
                break;
            case "Download":
                uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
                break;
            case "Apps":
                uri = MediaStore.Files.getContentUri("external");
                break;
            default:
                uri = MediaStore.Files.getContentUri("external");
        }
        mediaobserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                loadFilesAsync(category);
            }
        };
        getContentResolver().registerContentObserver(uri, true, mediaobserver);
    }

    private void loadFilesAsync(String category) {
        progressBar.setVisibility(View.VISIBLE);
        if (!fileItems.isEmpty()) {
            itemAdapter.set(fileItems);
            fastAdapter.notifyDataSetChanged();
        }

        executorService.execute(() -> {

            List<FileAdapter> tempFileItems = new ArrayList<>();
            List<File> files = StorageHelper.getFilesByCategory(this,category);
            for (File file : files) {
                tempFileItems.add(new FileAdapter(file, this));
            }

            mainHandler.post(() -> {
                progressBar.setVisibility(View.GONE);
                if (!tempFileItems.equals(fileItems)) {
                    fileItems.clear();
                    fileItems.addAll(tempFileItems);
                    itemAdapter.set(fileItems);
                    fastAdapter.notifyDataSetChanged();

                }
            });
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }

        if (mediaobserver != null){
            getContentResolver().unregisterContentObserver(mediaobserver);
        }
    }

    public void viewCompact(){
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.recyclerView), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
            return insets;
        });
    }
}
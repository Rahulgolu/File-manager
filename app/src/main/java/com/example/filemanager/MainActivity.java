package com.example.filemanager;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.filemanager.Adapter.CategoryAdapter;
import com.example.filemanager.Adapter.FileAdapter;
import com.example.filemanager.Adapter.RecentAdapter;
import com.example.filemanager.Adapter.SearchAdapter;
import com.example.filemanager.Helpers.MimeTypeHelper;
import com.example.filemanager.Helpers.StorageChangeObserver;
import com.example.filemanager.Helpers.StorageHelper;
import com.example.filemanager.databinding.ActivityMainBinding;
import com.example.filemanager.utils.Utils;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private FastAdapter<CategoryAdapter> fastAdapter;
    private ItemAdapter<CategoryAdapter> itemAdapter;
    private String[] categoryNames = {"Images", "Audio", "Videos", "Apps", "Document", "Download"};
    private List<CategoryAdapter> categoryList;
    private ExecutorService executorService;
    private Handler mainHandler;
    private StorageChangeObserver storageObserver;
    int[] categoryIcons = {
            R.drawable.ic_image, R.drawable.ic_audio, R.drawable.ic_vedio,
            R.drawable.ic_apps, R.drawable.ic_documents, R.drawable.ic_download
    };

    private ItemAdapter<FileAdapter> itemfileAdapter;
    private SearchAdapter searchAdapter;
    private FastAdapter<FileAdapter> fastAdapters;
    private File rootDirectory;
    private FastAdapter<RecentAdapter> recentFastAdapter;
    private ItemAdapter<RecentAdapter> recentItemAdapter;
    private List<RecentAdapter> recentList;
    private String rootFile;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        progressBar = findViewById(R.id.progressBar);

        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        itemAdapter = new ItemAdapter<>();
        fastAdapter = FastAdapter.with(itemAdapter);
        categoryList = new ArrayList<>();

        rootFile = Environment.getExternalStorageDirectory().getAbsolutePath();

        recentItemAdapter = new ItemAdapter<>();
        recentFastAdapter = FastAdapter.with(recentItemAdapter);
        recentList = new ArrayList<>();
        binding.recRecent.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        binding.recRecent.setAdapter(recentFastAdapter);
        loadRecentFiles(rootFile);

        itemfileAdapter = new ItemAdapter<>();
        searchAdapter = new SearchAdapter(itemfileAdapter,this);
        fastAdapters = FastAdapter.with(itemfileAdapter);

        binding.searchResult.setLayoutManager(new LinearLayoutManager(this));
        binding.searchResult.setAdapter(fastAdapters);

        rootDirectory = Environment.getExternalStorageDirectory();
        setupSearch();
        loadCategorySizesAsync();
        itemAdapter.add(categoryList);
        binding.rv.setLayoutManager(new GridLayoutManager(this,2));
        binding.rv.setAdapter(fastAdapter);
        setInternalStorageView();
        storageObserver = new StorageChangeObserver(
                Environment.getExternalStorageDirectory().getPath(),
                mainHandler,
                this::loadCategorySizesAsync
        );
        storageObserver.startWatching();

        binding.llInternalStorage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openInternalStorage();
            }
        });

        binding.llExternalStorage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this,"Coming Soon",Toast.LENGTH_SHORT).show();
            }
        });

        fastAdapter.setOnClickListener((v, adapter, item, position) -> {
            openCategoryFiles(item.getCategoryName());
            return true;
        });

        clickListener();

    }

    private void clickListener() {
        recentFastAdapter.setOnClickListener((v, adapter, item, position) -> {
            File file = item.getFile();
            MimeTypeHelper.openFile(this,file);
            return true;
        });

        fastAdapters.setOnClickListener((v, adapter, item, position)->{
            File file = item.getFile();
            MimeTypeHelper.openFile(this,file);
            return true;
        });

    }

    private void loadRecentFiles(String path) {
        progressBar.setVisibility(View.VISIBLE);
        rootFile = path;
        File directory = new File(rootFile);
        if (!directory.exists() && !directory.isDirectory()) {
         //   Log.e("RecentFiles", "Directory does not exist " + rootFile);
            progressBar.setVisibility(View.GONE);
            return;
        }
        executorService.execute(() ->{
            List<File> imageFiles = new ArrayList<>();
            searchForImages(directory, imageFiles);

            Collections.sort(imageFiles,(f1,f2)-> {
                return Long.compare(f2.lastModified(), f1.lastModified());
            });
            List<File> lastTwen =imageFiles.subList(0,Math.min(imageFiles.size(),15));
            List<RecentAdapter> tempRecentList = new ArrayList<>();
            for (File file : lastTwen) {
                FileAdapter fileAdapter = new FileAdapter(file, this);
                tempRecentList.add(new RecentAdapter(fileAdapter, this));
            }

            mainHandler.post(() ->{
                recentList.clear();
                recentList.addAll(tempRecentList);
                recentItemAdapter.clear();
                recentItemAdapter.add(recentList);
                recentFastAdapter.notifyAdapterDataSetChanged();
                progressBar.setVisibility(View.GONE);
            });

        });


        }

    private void searchForImages(File dir, List<File> mediaFiles) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    if (MimeTypeHelper.isImage(file) || MimeTypeHelper.isVideo(file)) {
                        mediaFiles.add(file);
                    }
                }else if (file.isDirectory()) {
                    searchForImages(file, mediaFiles);
                }
            }
        }
    }

    public void setupSearch() {
        binding.searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String query = charSequence.toString().toLowerCase(Locale.getDefault());
                if (query.isEmpty()) {
                    binding.searchResult.setVisibility(View.GONE);
                    itemfileAdapter.clear();
                } else {
                    binding.searchResult.setVisibility(View.VISIBLE);
                    searchAdapter.filter(rootDirectory, query);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }


    private void openCategoryFiles(String category) {
        Intent intent = new Intent(this, CategoryFilesActivity.class);
        intent.putExtra("category", category);
        Log.d("MainActivity", "Passing category: " + category);
        startActivity(intent);
    }

    private void loadCategorySizesAsync() {
        executorService.execute(() -> {
            List<CategoryAdapter> tempList = new ArrayList<>();
            boolean shouldRecompute = StorageHelper.shouldRecomputeSizes(this);

            for (int i = 0; i < categoryNames.length; i++) {
                long categorySizeBytes = StorageHelper.getCachedCategorySize(this, categoryNames[i]);

                // If no cache or outdated, compute and update cache
                if (categorySizeBytes == -1 || shouldRecompute) {
                    categorySizeBytes = StorageHelper.computeCategorySize(this,categoryNames[i]);
                    StorageHelper.updateCategorySizeCache(this, categoryNames[i], categorySizeBytes);
                }
                String formattedSize = StorageHelper.formatSize(categorySizeBytes);
                tempList.add(new CategoryAdapter(categoryNames[i], categoryIcons[i], formattedSize));
            }

            mainHandler.post(() -> {
                categoryList.clear();
                categoryList.addAll(tempList);
                itemAdapter.set(categoryList);
            });
        });
    }


    private void openInternalStorage() {
        Intent intent = new Intent(this, FileExplorerActivity.class);
        String path = Environment.getExternalStorageDirectory().getPath();
      //  String path = getExternalFilesDir(null).getAbsolutePath();
        intent.putExtra("path", path);
        startActivity(intent);
    }

    private void setInternalStorageView() {
        getTotalInternalMemorySize();
        getAvailableInternalMemorySize();

        String formattedSize = "Size: " + Utils.formatSize(Utils.TOTAL_AVAILABLE_INTERNAL_SIZE) +
                " / " + Utils.formatSize(Utils.TOTAL_INTERNAL_SIZE);

        binding.tvInternalStorageSize.setText(formattedSize);
    }

    public void getTotalInternalMemorySize() {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = 0;
        long totalBlocks = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = stat.getBlockSizeLong();
            totalBlocks = stat.getBlockCountLong();
        }
        Utils.TOTAL_INTERNAL_SIZE = totalBlocks * blockSize;

    }

    public void getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = 0;
        long availableBlocks = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {

            blockSize = stat.getBlockSizeLong();
            availableBlocks = stat.getAvailableBlocksLong();
        }

        Utils.TOTAL_AVAILABLE_INTERNAL_SIZE = availableBlocks * blockSize;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        if (storageObserver != null) {
            storageObserver.stopWatching();
        }
    }

    @Override
    public void onBackPressed() {
        if (binding.searchView.isShowing()) {
            binding.searchView.hide();
        }else {
            super.onBackPressed();
        }
    }
}
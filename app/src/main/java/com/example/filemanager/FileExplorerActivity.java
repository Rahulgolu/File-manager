package com.example.filemanager;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filemanager.Adapter.FileAdapter;
import com.example.filemanager.Helpers.MimeTypeHelper;
import com.example.filemanager.utils.FileUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import com.mikepenz.fastadapter.select.SelectExtension;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class FileExplorerActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ItemAdapter<FileAdapter> itemAdapter;
    private FastAdapter<FileAdapter> fastAdapter;
    MaterialToolbar toolbar,selectionToolbar;
    private TextView selectionToolbarTitle;
    private ImageView closeSelection,backArrow;
    private String currentPath;
    private FloatingActionButton fab;
    private LinearLayout breadcrumbLayout;
    private SelectExtension<FileAdapter> selectExtension;
    private boolean isSelectionMode = false;
    private FrameLayout frameLayout;
    private List<FileAdapter> fileAdapterList;
    private static final String PREFS_NAME = "SortPreferences";
    private static final String KEY_SORT_OPTION = "selected_sort";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_file_explorer);

        toolbar = findViewById(R.id.toolbar);
        selectionToolbar = findViewById(R.id.selectionToolbar);
        selectionToolbarTitle = findViewById(R.id.selectionToolbarTitle);
        closeSelection = findViewById(R.id.closeSelection);
        setSupportActionBar(toolbar);
        recyclerView = findViewById(R.id.recyclerView);
        breadcrumbLayout = findViewById(R.id.breadcrumbLayout);
        fab = findViewById(R.id.fab);
        frameLayout = findViewById(R.id.emptyframe);
        backArrow = findViewById(R.id.backButton);
        setupInsetsHandling();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemAdapter = new ItemAdapter<>();
        fastAdapter = FastAdapter.with(itemAdapter);
        fastAdapter.setHasStableIds(true);
        selectExtension = new SelectExtension<>(fastAdapter);
        if (selectExtension != null) {
            selectExtension.setSelectable(true);
            selectExtension.setMultiSelect(true);
            selectExtension.setSelectOnLongClick(true);
        }
        recyclerView.setAdapter(fastAdapter);

        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        currentPath = getIntent().getStringExtra("path");

        if (currentPath == null || currentPath.isEmpty()) {
            currentPath = getExternalFilesDir(null).getAbsolutePath();
        }


        loadFiles(currentPath);
      //  applySorting(getSavedSortOption());
        updateBreadcrumbs(currentPath);


        setupItemClickListeners();
        setupSelectionToolbar();

        fab.setOnClickListener(v -> showCreateFolderDialog());

        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    private void loadFiles(String path) {
        currentPath = path;
        File directory = new File(currentPath);
        itemAdapter.clear();
        if (directory .exists() && directory.isDirectory()){
            File[] files = directory.listFiles();
            if (files != null && files.length > 0) {
                frameLayout.setVisibility(View.GONE);

                for (File file : files) {
                    itemAdapter.add(new FileAdapter(file,this));
                }
                applySorting(getSavedSortOption());

            }else {
                frameLayout.setVisibility(View.VISIBLE);
            }
        }
        else {
            Log.e("FileExplorer", "Directory not found: " + path);
            frameLayout.setVisibility(View.VISIBLE);
        }
        fastAdapter.notifyDataSetChanged();
    }

    private void showCreateFolderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create New");

        // Create Layout for Dialog
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(20, 20, 20, 20);

        // Input Field for Name
        final EditText input = new EditText(this);
        input.setHint("Enter name");
        layout.addView(input);

        // Add to Dialog
        builder.setView(layout);

        // Buttons
        builder.setPositiveButton("Create Folder", (dialog, which) -> {
            String folderName = input.getText().toString().trim();
            if (!folderName.isEmpty()) {
                createNewFolder(folderName);
            } else {
                Toast.makeText(this, "Folder name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        /*builder.setNegativeButton("Create File", (dialog, which) -> {
            String fileName = input.getText().toString().trim();
            if (!fileName.isEmpty()) {
                createNewFile(fileName);
            } else {
                Toast.makeText(this, "File name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });*/

        builder.setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void createNewFolder(String folderName) {
        File newFolder = new File(currentPath, folderName);

        if (!newFolder.exists()) {
            if (newFolder.mkdir()) {
                Toast.makeText(this, "Folder createds " + folderName, Toast.LENGTH_SHORT).show();
                refreshFileList();
            } else {
                Toast.makeText(this, "Failed to create folder", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Folder already exists", Toast.LENGTH_SHORT).show();
        }
    }

    private void createNewFile(String fileName) {
        File newFile = new File(currentPath, fileName);

        if (!newFile.exists()) {
            try {
                if (newFile.createNewFile()) {
                    Toast.makeText(this, "File created: " + fileName, Toast.LENGTH_SHORT).show();
                    refreshFileList();
                } else {
                    Toast.makeText(this, "Failed to create file", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "File already exists", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupSelectionToolbar() {
        closeSelection.setOnClickListener(v -> exitSelectionMode());
        selectionToolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            Set<FileAdapter> selectedItems = selectExtension.getSelectedItems();

            if (selectedItems.isEmpty()) {
                Toast.makeText(this, "No files selected", Toast.LENGTH_SHORT).show();
                return false;
            }

            if (itemId == R.id.action_delete) {
                handleFileOperation(FileOperation.DELETE, selectedItems);
                return true;
            } else if (itemId == R.id.action_move) {
                handleFileOperation(FileOperation.MOVE, selectedItems);
                return true;
            } else if (itemId == R.id.action_copy) {
                handleFileOperation(FileOperation.COPY, selectedItems);
                return true;
            } else if (itemId == R.id.action_rename) {
                handleFileOperation(FileOperation.RENAME, selectedItems);
                return true;
            }

            return false;
        });
    }

    // es method mai working krna bacha hua hai
    private void handleFileOperation(FileOperation operation, Set<FileAdapter> selectedSet) {
        List<FileAdapter> selectedItems = new ArrayList<>(selectedSet);

        switch (operation) {
            case DELETE:
                for (FileAdapter item : selectedItems) {
                    if (FileUtils.deleteFile(item.getFile())) {
                        Toast.makeText(this, "Deleted: " + item.getFile().getName(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to delete: " + item.getFile().getName(), Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case MOVE:
                showMoveDialog(selectedItems);
                break;
            case COPY:
                File destination = new File("/path/to/destination");
                if (!destination.exists())
                    destination.mkdirs();
                for (FileAdapter itms : selectedItems ){
                    FileUtils.copyFile(itms.getFile(),destination);
                }

                break;

            case RENAME:
                if (selectedItems.size() != 1) {
                    Toast.makeText(this, "Select only one file to rename", Toast.LENGTH_SHORT).show();
                    return;
                }
                showRenameDialog(selectedItems.get(0).getFile());
                break;
        }

        refreshFileList();
        exitSelectionMode();
    }

    private void showMoveDialog(List<FileAdapter> selectedFiles) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Move to");
        File directory = new File(currentPath);
        File[] directories = directory.listFiles(File::isDirectory);
        if (directories == null || directories.length == 0) {
            Toast.makeText(this, "No folders available", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] folderNames = new String[directories.length];
        for (int i = 0; i < directories.length; i++) {
            folderNames[i] = directories[i].getName();
        }

        builder.setItems(folderNames, (DialogInterface dialog, int i) -> {
            File targetDir = directories[i];
            for ( FileAdapter file : selectedFiles){
                moveFile(file.getFile(),targetDir);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void moveFile(File sourceFile, File targetDir) {
        if (!targetDir.isDirectory()) {
            Toast.makeText(this, "Invalid destination", Toast.LENGTH_SHORT).show();
            return;
        }

        File newFile = new File(targetDir, sourceFile.getName());

        if (sourceFile.renameTo(newFile)) {
            Toast.makeText(this, "Moved successfully", Toast.LENGTH_SHORT).show();
            refreshFileList();
        } else {
            Toast.makeText(this, "Failed to move", Toast.LENGTH_SHORT).show();
        }
    }

    private void showRenameDialog(File file) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename File");

        final EditText input = new EditText(this);
        input.setText(file.getName());
        builder.setView(input);

        builder.setPositiveButton("Rename", (DialogInterface dialog, int i) -> {
            String newName = input.getText().toString().trim();

            if (!newName.isEmpty() && FileUtils.renameFile(file, newName)) {
                Toast.makeText(this, "Renamed to: " + newName, Toast.LENGTH_SHORT).show();
                refreshFileList();
            } else {
                Toast.makeText(this, "Rename failed", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void refreshFileList() {
        loadFiles(currentPath);
        updateBreadcrumbs(currentPath);
        fastAdapter.notifyDataSetChanged();
    }

    private void setupItemClickListeners() {
        // longClick listner
        fastAdapter.setOnLongClickListener((v, adapter, item, position) -> {
            enterSelectionMode();
            selectExtension.toggleSelection(position);
            fastAdapter.notifyAdapterItemChanged(position);
            updateSelectionTitle();
            return true;
        });

        fastAdapter.setOnClickListener((v, adapter, item, position) -> {
            if (isSelectionMode) {
                selectExtension.toggleSelection(position);
                fastAdapter.notifyAdapterItemChanged(position);
                updateSelectionTitle();
                return true;
            } else {
                File file = item.getFile();
                if (file.isDirectory()){
                    loadFiles(file.getAbsolutePath()); // open folder
                    updateBreadcrumbs(file.getAbsolutePath());
                }else {
                     MimeTypeHelper.openFile(this,file); // open file
                }
            }
            return false;
        });
    }

    /*private void openFile(File file) {

        try {
            Uri fileUri = FileUtils.getFileUri(this,file);
            String mimeType = MimeTypeHelper.getMimeType(file);

            if (mimeType == null) {
                Toast.makeText(this, "Unsupported file type", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(fileUri, mimeType);

            this.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open file", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }*/

    private void enterSelectionMode() {
        if (!isSelectionMode) {
            isSelectionMode = true;
            toolbar.setVisibility(View.GONE);
            selectionToolbar.setVisibility(View.VISIBLE);
        }
    }

    private void exitSelectionMode() {
        isSelectionMode = false;
        selectExtension.deselect();
        fastAdapter.notifyAdapterDataSetChanged();
        toolbar.setVisibility(View.VISIBLE);
        selectionToolbar.setVisibility(View.GONE);
    }

    private void updateSelectionTitle() {
        int selectedCount = selectExtension.getSelectedItems().size();
        selectionToolbarTitle.setText(selectedCount + " selected");

        if (selectedCount == 0) {
            exitSelectionMode();
        }
    }

    private void updateBreadcrumbs(String path) {
        breadcrumbLayout.removeAllViews();
        File currentFile = new File(path);
        List<File> pathSegments = new ArrayList<>();


        while (currentFile != null) {
            pathSegments.add(0, currentFile);
            currentFile = currentFile.getParentFile();
        }

        // Remove "storage > emulated > 0" to start from "Internal Storage"
        if (pathSegments.size() > 3) {
            pathSegments = pathSegments.subList(3, pathSegments.size());
        }


        if (!pathSegments.isEmpty()) {
            String internalStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            TextView internalStorageBreadcrumb = createBreadcrumb("Internal Storage", internalStoragePath, false);
            breadcrumbLayout.addView(internalStorageBreadcrumb);

            if (pathSegments.size() > 0) {
                breadcrumbLayout.addView(createSeparator());
            }
        }


        for (int i = 0; i < pathSegments.size(); i++) {
            File segment = pathSegments.get(i);
            if (segment.getName().equals("0")) {
                continue;
            }

            boolean isCurrentFile = (i == pathSegments.size() - 1);
            TextView breadcrumb = createBreadcrumb(segment.getName(), segment.getAbsolutePath(), isCurrentFile);
            breadcrumbLayout.addView(breadcrumb);


            if (i < pathSegments.size() - 1) {
                breadcrumbLayout.addView(createSeparator());
            }
        }
    }


    private TextView createBreadcrumb(String name, String path, boolean isBold) {
        TextView breadcrumb = new TextView(this);
        breadcrumb.setText(name);
        breadcrumb.setPadding(8, 8, 8, 8);
        breadcrumb.setClickable(true);
        breadcrumb.setFocusable(true);
        breadcrumb.setBackgroundResource(android.R.drawable.list_selector_background);
        breadcrumb.setTextColor(getThemeTextColor());
        breadcrumb.setTypeface(null, isBold ? Typeface.BOLD : Typeface.NORMAL);

        breadcrumb.setOnClickListener(v -> {
            loadFiles(path);
            updateBreadcrumbs(path);
        });

        return breadcrumb;
    }


    private TextView createSeparator() {
        TextView separator = new TextView(this);
        separator.setText(" > ");
        separator.setTextColor(getThemeTextColor());
        separator.setPadding(4, 8, 4, 8);
        return separator;
    }


    private int getThemeTextColor() {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getTheme();

        if (theme.resolveAttribute(com.google.android.material.R.attr.colorOnBackground, typedValue, true)) {
            return typedValue.data;
        }
        if (theme.resolveAttribute(android.R.attr.textColorPrimary, typedValue, true)) {
            return typedValue.data;
        }
        return Color.WHITE;
    }



    private void setupInsetsHandling() {
        ViewCompat.setOnApplyWindowInsetsListener(fab, (v, insets) -> {
            int bottomInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;
            fab.setTranslationY(-bottomInset);
            return insets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(recyclerView, (v, insets) -> {
            int navigationBarHeight = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            recyclerView.setPadding(recyclerView.getPaddingLeft(), recyclerView.getPaddingTop(), recyclerView.getPaddingRight(), recyclerView.getPaddingBottom() + navigationBarHeight);
            return insets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(selectionToolbar, (v, insets) -> {
            int topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(0, topInset, 0, 0);
            return insets;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.sortBy) {
            openButtonSheetFrag();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openButtonSheetFrag() {
        SortBottomSheetFragment buttonSheetFragment = new SortBottomSheetFragment();
        buttonSheetFragment.show(getSupportFragmentManager(), buttonSheetFragment.getTag());
      //  buttonSheetFragment.setCancelable(true);
    }

    private String getSavedSortOption() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(KEY_SORT_OPTION, "Newest date first");
    }

    public void applySorting(String sortOption) {
        fileAdapterList = new ArrayList<>(itemAdapter.getAdapterItems());
        if (fileAdapterList == null) return;

        Collections.sort(fileAdapterList, (FileAdapter f1 , FileAdapter f2) -> {
            File file1 = f1.getFile();
            File file2 = f2.getFile();
            if (file1.isDirectory() && !file2.isDirectory()){
                return -1;
            }
            if (!file1.isDirectory() && file2.isDirectory()){
                return 1;
            }

        switch (sortOption) {
            case "Newest date first":
                return Long.compare(file2.lastModified(), file1.lastModified());

            case "Oldest date first":
                return Long.compare(file1.lastModified(), file2.lastModified());

            case "Largest first":
                return Long.compare(file2.length(), file1.length());

            case "Smallest first":
                return Long.compare(file1.length(), file2.length());

            case "Name A -> Z":
                return file1.getName().compareToIgnoreCase(file2.getName());

            case "Name Z -> A":
                return file2.getName().compareToIgnoreCase(file1.getName());

                   /* String fileName1 = f1.getFile().getName().toLowerCase();
                    String fileName2 = f2.getFile().getName().toLowerCase();
                    return fileName2.compareTo(fileName1);*/

        }

            return 0;
        });

        updateAdapter(fileAdapterList);
    }


    private void updateAdapter(List<FileAdapter> sortedList) {
        itemAdapter.set(sortedList);
        fastAdapter.notifyDataSetChanged();
    }


    @Override
    public void onBackPressed() {
        String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (currentPath.equals(rootPath)){
            super.onBackPressed();
        }else {
            File parent = new File(currentPath).getParentFile();
            if (parent != null) {
                loadFiles(parent.getAbsolutePath());
                updateBreadcrumbs(parent.getAbsolutePath());
            }else {
                super.onBackPressed();
            }
        }

    }

}
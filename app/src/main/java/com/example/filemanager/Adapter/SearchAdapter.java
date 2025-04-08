package com.example.filemanager.Adapter;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.mikepenz.fastadapter.adapters.ItemAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchAdapter extends ItemAdapter<FileAdapter> {
    private final ItemAdapter<FileAdapter> itemfileAdapter;
    private final Context context;

    public SearchAdapter(ItemAdapter<FileAdapter> itemfileAdapter, Context context) {
        this.itemfileAdapter = itemfileAdapter;
        this.context = context;
    }


    public void filter(File file,String query) {

        ExecutorService excutor = Executors.newSingleThreadExecutor();
        excutor.execute(new Runnable() {
            @Override
            public void run() {
                List<FileAdapter> filteredList = new ArrayList<>();
                String queryLower = query.toLowerCase();
                SearchAdapter.this.searchFiles(file, queryLower, filteredList);

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        itemfileAdapter.setNewList(filteredList, false);
                    }
                });
            }
        });
        excutor.shutdown();
    }

    private void searchFiles(File rootDir, String queryLower, List<FileAdapter> filteredList) {

        Stack<File> stack = new Stack<>();
        stack.push(rootDir);

        while (!stack.isEmpty()) {
            File currentDir = stack.pop();
            File[] files = currentDir.listFiles();

            if (files != null) {
                for (File file : files) {
                    String fileName = file.getName().toLowerCase();
                    if (file.isDirectory()) {
                        stack.push(file);
                    } else if (fileName.startsWith(queryLower)) {
                        FileAdapter fileAdapter = new FileAdapter(file, context);
                        fileAdapter.setSearchMode(true);
                        filteredList.add(fileAdapter);
                    }

                }
            }
        }

       /* File files[] = file.listFiles();
        if (files != null) {
            for (File file1 : files){
                String s =file1.getName().toLowerCase();
                if (s.startsWith(queryLower)){
                    FileAdapter fileAdapter = new FileAdapter(file1, context);
                    fileAdapter.setSearchMode(true);
                    filteredList.add(fileAdapter);
                }
                if (file1.isDirectory()){
                    searchFiles(file1,queryLower,filteredList);
                }
            }
        }*/
    }

}

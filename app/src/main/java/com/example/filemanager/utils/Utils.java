package com.example.filemanager.utils;

import java.util.Locale;

public class Utils {

    public static long TOTAL_INTERNAL_SIZE = 0;
    public static long TOTAL_AVAILABLE_INTERNAL_SIZE = 0;

    public static String formatSize(long size) {
        if (size <= 0) return "0.00 GB";

        // Convert to base-10 GB (1GB = 1,000,000,000 bytes)
        float sizeInGB = size / (1000f * 1000f * 1000f);

        return String.format(Locale.getDefault(), "%.2f GB", sizeInGB);
    }
}

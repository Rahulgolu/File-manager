package com.example.filemanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SortBottomSheetFragment extends BottomSheetDialogFragment {

    private RadioGroup radioGroup;
    private static final String PREFS_NAME = "SortPreferences";
    private static final String KEY_SORT_OPTION = "selected_sort";

    public SortBottomSheetFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view= inflater.inflate(R.layout.fragment_sort_bottom_sheet, container, false);
        radioGroup = view.findViewById(R.id.radioGroupSort);

      //  SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
     //   String selectedSortOption = prefs.getString(KEY_SORT_OPTION, "Newest date first");

        String selectedSortOption = getSavedSortOption();
        if (selectedSortOption != null) {
            selectRadioButton(selectedSortOption);
        }

        radioGroup.setOnCheckedChangeListener((RadioGroup group, int chekedId) ->{
            String sortOption = getSortOptionFromId(chekedId);

            saveSortOption(sortOption);
            notifySortingChange();
            dismiss();
        });

        return view;
    }

    private String getSortOptionFromId(int chekedId) {
        if (chekedId == R.id.newDateFirst)
            return "Newest date first";
        if (chekedId == R.id.oldDateFirst)
            return "Oldest date first";
        if (chekedId == R.id.largestFirst)
            return "Largest first";
        if (chekedId == R.id.smallestFirst)
            return "Smallest first";
        if (chekedId == R.id.nameAccending)
            return "Name A -> Z";
        if (chekedId == R.id.nameDecending)
            return "Name Z -> A";

        return null;
    }

    private void selectRadioButton(String selectedSortOption) {
        int radioId;
        switch (selectedSortOption) {
            case "Oldest date first":
                radioId = R.id.oldDateFirst;
                break;
            case "Largest first":
                radioId = R.id.largestFirst;
                break;
            case "Smallest first":
                radioId = R.id.smallestFirst;
                break;
            case "Name A -> Z":
                radioId = R.id.nameAccending;
                break;
            case "Name Z -> A":
                radioId = R.id.nameDecending;
                break;
            default:
                radioId = R.id.newDateFirst;
        }
        radioGroup.check(radioId);
    }

    private void saveSortOption(String option) {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_SORT_OPTION, option).apply();
    }

    private String getSavedSortOption() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_SORT_OPTION, "Newest date first");
    }

    private void notifySortingChange() {
        if (getActivity() instanceof FileExplorerActivity) {
            ((FileExplorerActivity) getActivity()).applySorting(getSavedSortOption());
        }
    }
}
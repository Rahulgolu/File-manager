package com.example.filemanager.Adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.filemanager.R;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.items.AbstractItem;

import java.util.List;

public class CategoryAdapter extends AbstractItem<CategoryAdapter.ViewHolder> {
    private final String categoryName;
    private final int categoryIcon;
    private final String categorySize;


    public CategoryAdapter(String categoryName, int categoryIcon , String categorySize) {
        this.categoryName = categoryName;
        this.categoryIcon = categoryIcon;
        this.categorySize = categorySize;
    }

    public String getCategoryName() {
        return categoryName;
    }

    @NonNull
    @Override
    public ViewHolder getViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    public int getType() {
        return R.id.llCategory;
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_category;
    }

    public static class ViewHolder extends FastAdapter.ViewHolder<CategoryAdapter> {
        private final TextView categoryTextView,categorySizeTextView;
        private final ImageView categoryImageView;

        public ViewHolder(View itemView) {
            super(itemView);
            categoryTextView = itemView.findViewById(R.id.tvCategoryName);
            categoryImageView = itemView.findViewById(R.id.ivCategoryIcon);
            categorySizeTextView = itemView.findViewById(R.id.tvCategorySize);

        }

        @Override
        public void bindView(CategoryAdapter item, List<?> list) {

            categoryTextView.setText(item.categoryName);
            categoryImageView.setImageResource(item.categoryIcon);
            categorySizeTextView.setText("Size: " + item.categorySize);
        }

        @Override
        public void unbindView(CategoryAdapter item) {
            categoryTextView.setText(null);
            categoryImageView.setImageResource(0);
            categorySizeTextView.setText(null);

        }
    }
}

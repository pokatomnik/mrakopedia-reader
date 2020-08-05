package com.example.mrakopediareader.categorieslist;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mrakopediareader.R;
import com.example.mrakopediareader.api.dto.Category;

import java.util.ArrayList;
import java.util.function.Consumer;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.ViewHolder> {
    private ArrayList<Category> mDataset;

    private Consumer<Category> onClick;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout layout;

        private Category category;

        public ViewHolder(LinearLayout layout, Consumer<Category> onClick) {
            super(layout);
            this.layout = layout;
            this.layout.setOnClickListener((view) -> {
                onClick.accept(this.category);
            });
        }

        public void setCategory(Category category) {
            this.category = category;
            final TextView textView = this.layout.findViewById(R.id.categoryTitle);
            textView.setText(this.category.getTitle());
        }
    }

    public CategoriesAdapter(ArrayList<Category> myDataset, Consumer<Category> onClick) {
        mDataset = myDataset;
        this.onClick = onClick;
    }

    @Override
    public CategoriesAdapter.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.categories_view, parent, false);
        return new CategoriesAdapter.ViewHolder(layout, this.onClick);
    }

    @Override
    public void onBindViewHolder(CategoriesAdapter.ViewHolder holder, int position) {
        final Category category = mDataset.get(position);
        holder.setCategory(category);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}

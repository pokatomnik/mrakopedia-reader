package com.example.mrakopediareader.pageslist;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mrakopediareader.R;
import com.example.mrakopediareader.api.Page;

import java.util.ArrayList;
import java.util.function.Consumer;

class PageResultsAdapter extends RecyclerView.Adapter<PageResultsAdapter.ViewHolder> {
    private ArrayList<Page> mDataset;

    private Consumer<Page> onClick;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout layout;

        private Page page;

        public ViewHolder(LinearLayout layout, Consumer<Page> onClick) {
            super(layout);
            this.layout = layout;
            this.layout.setOnClickListener((view) -> {
                onClick.accept(this.page);
            });
        }

        public void setPage(Page page) {
            this.page = page;
            final TextView textView = this.layout.findViewById(R.id.pageTitle);
            textView.setText(this.page.getTitle());
        }
    }

    public PageResultsAdapter(ArrayList<Page> myDataset, Consumer<Page> onClick) {
        mDataset = myDataset;
        this.onClick = onClick;
    }

    @Override
    public PageResultsAdapter.ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_result_view, parent, false);
        return new ViewHolder(layout, this.onClick);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Page page = mDataset.get(position);
        holder.setPage(page);
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}

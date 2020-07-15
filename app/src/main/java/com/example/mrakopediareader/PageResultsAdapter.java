package com.example.mrakopediareader;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mrakopediareader.api.Page;

import java.util.ArrayList;

class PageResultsAdapter extends RecyclerView.Adapter<PageResultsAdapter.ViewHolder> {
    private ArrayList<Page> mDataset;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout layout;
        public ViewHolder(LinearLayout v) {
            super(v);
            layout = v;
        }
    }

    public PageResultsAdapter(ArrayList<Page> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public PageResultsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LinearLayout layout = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_result_view, parent, false);
        ViewHolder vh = new ViewHolder(layout);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final TextView textView = holder.layout.findViewById(R.id.pageTitle);
        textView.setText(mDataset.get(position).getTitle());

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}

package com.example.mrakopediareader;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mrakopediareader.api.Page;

import java.util.ArrayList;

class PageResultsAdapter extends RecyclerView.Adapter<PageResultsAdapter.ViewHolder> {
    private ArrayList<Page> mDataset;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        public ViewHolder(TextView v) {
            super(v);
            textView = v;
        }
    }

    public PageResultsAdapter(ArrayList<Page> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public PageResultsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        TextView v = (TextView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_result_view, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textView.setText(mDataset.get(position).getTitle());

    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}

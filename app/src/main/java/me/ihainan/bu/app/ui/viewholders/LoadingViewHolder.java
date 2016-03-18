package me.ihainan.bu.app.ui.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;

import me.ihainan.bu.app.R;

/**
 * Loading more view holder
 */
public class LoadingViewHolder extends RecyclerView.ViewHolder {
    public ProgressBar progressBar;

    public LoadingViewHolder(View itemView) {
        super(itemView);
        progressBar = (ProgressBar) itemView.findViewById(R.id.bottom_progress_bar);
    }
}
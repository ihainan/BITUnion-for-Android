package me.ihainan.bu.app.ui.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.ihainan.bu.app.R;

public class SelfieViewHolder extends RecyclerView.ViewHolder {
    public TextView authorName;
    public TextView action;
    public TextView title;
    public ImageView background;
    public ImageView avatar;
    public LinearLayout rootLayout;

    public SelfieViewHolder(View view) {
        super(view);
        rootLayout = (LinearLayout) view.findViewById(R.id.root_layout);
        background = (ImageView) view.findViewById(R.id.post_item_background);
        authorName = (TextView) view.findViewById(R.id.thread_item_author);
        title = (TextView) view.findViewById(R.id.thread_item_title);
        action = (TextView) view.findViewById(R.id.thread_item_action);
        avatar = (ImageView) view.findViewById(R.id.thread_item_avatar);
    }
}
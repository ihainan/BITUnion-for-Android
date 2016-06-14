package me.ihainan.bu.app.ui.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.ihainan.bu.app.R;

public class DefaultViewHolder extends RecyclerView.ViewHolder {
    public final TextView authorName;
    public final TextView forumName;
    public final TextView action;
    public final TextView title;
    public final ImageView avatar;
    public final TextView replyCount;
    public final TextView date;
    public final TextView isNewOrHot;
    public final TextView placeHolderIn;
    public final LinearLayout rootLayout;

    public DefaultViewHolder(View view) {
        super(view);
        rootLayout = (LinearLayout) view.findViewById(R.id.root_layout);
        authorName = (TextView) view.findViewById(R.id.thread_item_author);
        forumName = (TextView) view.findViewById(R.id.thread_item_forum);
        title = (TextView) view.findViewById(R.id.thread_item_title);
        avatar = (ImageView) view.findViewById(R.id.thread_item_avatar);
        replyCount = (TextView) view.findViewById(R.id.thread_item_reply);
        action = (TextView) view.findViewById(R.id.thread_item_action);
        date = (TextView) view.findViewById(R.id.thread_item_date);
        isNewOrHot = (TextView) view.findViewById(R.id.thread_item_new_or_hot);
        placeHolderIn = (TextView) view.findViewById(R.id.thread_item_in);
    }
}
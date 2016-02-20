package bit.ihainan.me.bitunionforandroid.ui.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import bit.ihainan.me.bitunionforandroid.R;

public class DefaultViewHolder extends RecyclerView.ViewHolder {
    public TextView authorName;
    public TextView forumName;
    public TextView action;
    public TextView title;
    public ImageView avatar;
    public TextView replyCount;
    public TextView date;
    public TextView isNewOrHot;
    public TextView placeHolderIn;

    public DefaultViewHolder(View view) {
        super(view);
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
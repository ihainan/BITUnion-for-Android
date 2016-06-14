package me.ihainan.bu.app.ui.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import me.ihainan.bu.app.R;

/**
 * 通知 Notification
 */
public class NotificationViewHolder extends RecyclerView.ViewHolder {
    public final ImageView avatar;
    public final TextView username;
    public final TextView content;
    public final TextView date;
    public final RelativeLayout rootLayout;

    public NotificationViewHolder(View itemView) {
        super(itemView);
        rootLayout = (RelativeLayout) itemView.findViewById(R.id.root_layout);
        username = (TextView) itemView.findViewById(R.id.username);
        avatar = (ImageView) itemView.findViewById(R.id.avatar);
        content = (TextView) itemView.findViewById(R.id.content);
        date = (TextView) itemView.findViewById(R.id.date);
    }
}

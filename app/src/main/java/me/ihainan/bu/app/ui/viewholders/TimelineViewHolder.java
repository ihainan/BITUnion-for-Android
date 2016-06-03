package me.ihainan.bu.app.ui.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.ihainan.bu.app.R;

/**
 * 时间轴事件 ViewHolder
 */
public class TimelineViewHolder extends RecyclerView.ViewHolder {
    public ImageView avatar;
    public TextView username, title, action, content, date;
    public LinearLayout rootLayout;

    public TimelineViewHolder(View itemView) {
        super(itemView);

        rootLayout = (LinearLayout) itemView.findViewById(R.id.root_layout);
        avatar = (ImageView) itemView.findViewById(R.id.avatar);
        title = (TextView) itemView.findViewById(R.id.title);
        username = (TextView) itemView.findViewById(R.id.user_name);
        action = (TextView) itemView.findViewById(R.id.action);
        content = (TextView) itemView.findViewById(R.id.content);
        date = (TextView) itemView.findViewById(R.id.date);
    }
}

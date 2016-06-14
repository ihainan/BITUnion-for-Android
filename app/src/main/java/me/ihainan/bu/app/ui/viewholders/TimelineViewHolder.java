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
public class TimelineViewHolder {
    /**
     * 时间轴事件 - 帖子相关
     */
    public static class TimelinePostViewHolder extends RecyclerView.ViewHolder {
        public final ImageView avatar;
        public final TextView username;
        public final TextView title;
        public final TextView action;
        public final TextView content;
        public final TextView date;
        public final LinearLayout rootLayout;

        public TimelinePostViewHolder(View itemView) {
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

    /**
     * 时间轴事件 - 用户相关
     */
    public static class TimelineUserViewHolder extends RecyclerView.ViewHolder {
        public final ImageView avatar;
        public final TextView username;
        public final TextView following;
        public final TextView action;
        public final TextView date;
        public final LinearLayout rootLayout;

        public TimelineUserViewHolder(View itemView) {
            super(itemView);

            rootLayout = (LinearLayout) itemView.findViewById(R.id.root_layout);
            avatar = (ImageView) itemView.findViewById(R.id.avatar);
            username = (TextView) itemView.findViewById(R.id.user_name);
            action = (TextView) itemView.findViewById(R.id.action);
            date = (TextView) itemView.findViewById(R.id.date);
            following = (TextView) itemView.findViewById(R.id.following);
        }
    }
}

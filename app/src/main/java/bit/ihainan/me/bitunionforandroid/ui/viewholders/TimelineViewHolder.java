package bit.ihainan.me.bitunionforandroid.ui.viewholders;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import bit.ihainan.me.bitunionforandroid.R;

/**
 * 时间轴事件 ViewHolder
 */
public class TimelineViewHolder extends RecyclerView.ViewHolder {
    public ImageView avatar;
    public CardView rootCardView;
    public TextView username, title, action, content, date;

    public TimelineViewHolder(View itemView) {
        super(itemView);

        avatar = (ImageView) itemView.findViewById(R.id.avatar);
        rootCardView = (CardView) itemView.findViewById(R.id.root_card_view);
        title = (TextView) itemView.findViewById(R.id.title);
        username = (TextView) itemView.findViewById(R.id.user_name);
        action = (TextView) itemView.findViewById(R.id.action);
        content = (TextView) itemView.findViewById(R.id.content);
        date = (TextView) itemView.findViewById(R.id.date);
    }
}

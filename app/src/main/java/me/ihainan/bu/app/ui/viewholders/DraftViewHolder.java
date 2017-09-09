package me.ihainan.bu.app.ui.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import me.ihainan.bu.app.R;

/**
 * 通知 Notification
 */
public class DraftViewHolder extends RecyclerView.ViewHolder {
    public final ImageView attachment;
    public final TextView title;
    public final TextView content;
    public final TextView date;
    public final RelativeLayout rootLayout;

    public DraftViewHolder(View itemView) {
        super(itemView);
        rootLayout = itemView.findViewById(R.id.root_layout);
        title = itemView.findViewById(R.id.draft_title);
        attachment = itemView.findViewById(R.id.draft_attach_file);
        content = itemView.findViewById(R.id.draft_content);
        date = itemView.findViewById(R.id.draft_date);
    }
}

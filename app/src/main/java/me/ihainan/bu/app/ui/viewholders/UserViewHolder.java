package me.ihainan.bu.app.ui.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import me.ihainan.bu.app.R;

public class UserViewHolder extends RecyclerView.ViewHolder {
    public ImageView avatar;
    public TextView username;
    public LinearLayout rootLayout;

    public UserViewHolder(View itemView) {
        super(itemView);

        avatar = (ImageView) itemView.findViewById(R.id.avatar);
        username = (TextView) itemView.findViewById(R.id.username);
        rootLayout = (LinearLayout) itemView.findViewById(R.id.root_layout);
    }
}

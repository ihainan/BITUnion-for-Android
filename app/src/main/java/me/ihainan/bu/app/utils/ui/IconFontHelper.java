package me.ihainan.bu.app.utils.ui;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.view.MenuItemCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import me.ihainan.bu.app.R;

/**
 * Icon Font 工具类
 */
public class IconFontHelper {
    private static final int LAYOUT = R.layout.menu_font_icon_layout;
    private static final String DEFAULT_FONT_TTF = "iconfont/iconfont.ttf";

    public static RelativeLayout setupMenuIcon(Context context, MenuItem menuItem, String iconText, View.OnClickListener onClickListener) {
        MenuItemCompat.setActionView(menuItem, LAYOUT);
        RelativeLayout itemLayout = (RelativeLayout) MenuItemCompat.getActionView(menuItem);

        // TextView
        TextView tvIcon = (TextView) itemLayout.findViewById(R.id.tv_icon);
        tvIcon.setText(iconText);
        Typeface iconFont = Typeface.createFromAsset(context.getAssets(), DEFAULT_FONT_TTF);
        tvIcon.setTypeface(iconFont);

        // Click
        itemLayout.setOnClickListener(onClickListener);
        itemLayout.findViewById(R.id.tv_icon).setOnClickListener(onClickListener);
        itemLayout.findViewById(R.id.badge_layout).setOnClickListener(onClickListener);

        // Badge
        itemLayout.findViewById(R.id.badge_notification).setVisibility(View.GONE);

        return itemLayout;
    }

    public static TextView setupMenuIconWithBadge(Context context, MenuItem menuItem, String iconText, View.OnClickListener onClickListener) {
        RelativeLayout itemLayout = setupMenuIcon(context, menuItem, iconText, onClickListener);

        // Badge
        TextView tvBadgeCount = (TextView) itemLayout.findViewById(R.id.badge_notification);
        itemLayout.findViewById(R.id.badge_notification).setVisibility(View.INVISIBLE);
        return tvBadgeCount;
    }
}

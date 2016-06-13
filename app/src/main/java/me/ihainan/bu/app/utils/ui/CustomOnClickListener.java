package me.ihainan.bu.app.utils.ui;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

/**
 * 点击事件
 */
public abstract class CustomOnClickListener implements View.OnClickListener {
    int clickTimes = 0;
    boolean hasClickTwoTimes = false;

    @Override
    public void onClick(View v) {
        clickTimes++;
        Handler handler = new Handler();
        Runnable r = new Runnable() {

            @Override
            public void run() {
                clickTimes = 0;
                if (!hasClickTwoTimes) {
                    singleClick();
                }

                hasClickTwoTimes = false;
            }
        };

        if (clickTimes == 1) {
            // Single click
            handler.postDelayed(r, 250);
        } else if (clickTimes == 2) {
            // Double click
            clickTimes = 0;
            hasClickTwoTimes = true;
            doubleClick();
        }
    }

    public abstract void singleClick();

    public abstract void doubleClick();

    public static View.OnClickListener doubleClickToListTop(final Context context, final RecyclerView recyclerView) {
        return new CustomOnClickListener() {
            @Override
            public void singleClick() {
                Toast.makeText(context, "双击标题栏返回顶部", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void doubleClick() {
                if (context == null || recyclerView == null) return;
                recyclerView.getLayoutManager().smoothScrollToPosition(recyclerView, null, 0);
                Toast.makeText(context, "返回顶部", Toast.LENGTH_SHORT).show();
            }
        };
    }
}

package me.ihainan.bu.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ExpandableListView;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.adapters.SuperParentAdapter;
import me.ihainan.bu.app.ui.assist.SwipeActivity;
import me.ihainan.bu.app.utils.BUApplication;

public class ForumListActivity extends SwipeActivity {
    private final static String TAG = ForumListActivity.class.getSimpleName();
    public final static int REQUEST_THREAD_LIST = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum_list);

        // Swipe to back
        setSwipeAnyWhere(false);

        // Load forum group list
        BUApplication.makeForumGroupList(this);

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null && toolbar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            toolbar.setTitle(R.string.action_forum);
        }

        // ListView
        ExpandableListView mExpandableListView = (ExpandableListView) findViewById(R.id.forum_system_admin_lv);
        SuperParentAdapter mAdapter = new SuperParentAdapter(this, BUApplication.forumListGroupList);
        if (mExpandableListView != null) {
            mExpandableListView.setAdapter(mAdapter);
            mExpandableListView.setDivider(null);
            mExpandableListView.expandGroup(0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_THREAD_LIST && resultCode == RESULT_OK) {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    }
}

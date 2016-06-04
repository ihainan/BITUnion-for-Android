package me.ihainan.bu.app.ui;

import android.os.Bundle;
import android.app.Activity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ExpandableListView;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.adapters.SuperParentAdapter;
import me.ihainan.bu.app.ui.assist.SwipeActivity;
import me.ihainan.bu.app.utils.BUApplication;

public class ForumListActivity extends SwipeActivity {
    private final static String TAG = ForumListActivity.class.getSimpleName();

    // UI references
    private ExpandableListView mExpandableListView;
    private Toolbar mToolbar;

    // Data
    private SuperParentAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum_list);

        // Swipe to back
        setSwipeAnyWhere(false);

        // Load forum group list
        BUApplication.makeForumGroupList(this);

        // Toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mToolbar.setTitle(R.string.action_forum);

        // ListView
        mExpandableListView = (ExpandableListView) findViewById(R.id.forum_system_admin_lv);
        mAdapter = new SuperParentAdapter(this, BUApplication.forumListGroupList);
        mExpandableListView.setAdapter(mAdapter);
        mExpandableListView.setDivider(null);
        mExpandableListView.expandGroup(0);
    }


}

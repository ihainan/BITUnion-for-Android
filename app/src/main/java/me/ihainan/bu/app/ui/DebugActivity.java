package me.ihainan.bu.app.ui;

import android.os.Bundle;
import android.app.Activity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.ui.assist.SwipeActivity;

public class DebugActivity extends SwipeActivity {
    private final static String TAG = DebugActivity.class.getSimpleName();
    public final static String ORIGIN_HTML = TAG + "_ORIGIN_HTML";
    public final static String NEW_HTML = TAG + "_NEW_HTML";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        // Toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mToolbar.setTitle(getString(R.string.title_activity_debug));

        if (getIntent().getExtras() != null) {
            String originalHtml = getIntent().getExtras().getString(ORIGIN_HTML);
            String newHtml = getIntent().getExtras().getString(NEW_HTML);
            ((EditText) findViewById(R.id.et_original_text)).setText(originalHtml);
            ((EditText) findViewById(R.id.et_new_text)).setText(newHtml);
        }
    }
}

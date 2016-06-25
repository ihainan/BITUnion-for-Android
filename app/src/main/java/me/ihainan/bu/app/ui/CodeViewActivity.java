package me.ihainan.bu.app.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.widget.Toast;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.ui.assist.SwipeActivity;
import me.ihainan.bu.app.utils.CommonUtils;
import thereisnospon.codeview.CodeView;
import thereisnospon.codeview.CodeViewTheme;

public class CodeViewActivity extends SwipeActivity {
    // Tags
    private final static String TAG = CodeViewActivity.class.getSimpleName();
    public final static String CODE_CONTENT_TAG = TAG + "_CODE_CONTENT_TAG";

    // UI
    private CodeView mCodeView;

    // Data
    private String mCodeContent;
    private final Context mContext = CodeViewActivity.this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_view);

        // Get Extra
        getExtra();

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }
        setTitle("代码片段");

        // Code
        mCodeView = (CodeView) findViewById(R.id.code_view);

        if (mCodeView != null) {
            mCodeView.setTheme(CodeViewTheme.ATELIER_ESTUARY_LIGHT).fillColor();
            mCodeView.showCode(mCodeContent);
        }
    }

    public void getExtra() {
        Intent intent = getIntent();
        Bundle args = intent.getExtras();
        if (args != null) {
            mCodeContent = args.getString(CODE_CONTENT_TAG);
        }

        if (mCodeContent == null) {
            mCodeContent = "@Override\n" +
                    "protected void onCreate(Bundle savedInstanceState) {\n" +
                    "    super.onCreate(savedInstanceState);\n" +
                    "    setContentView(R.layout.activity_code_view);\n" +
                    "\n" +
                    "    // Get Extra\n" +
                    "    getExtra();\n" +
                    "\n" +
                    "    // Toolbar\n" +
                    "    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);\n" +
                    "    setSupportActionBar(toolbar);\n" +
                    "    if (getSupportActionBar() != null) {\n" +
                    "        getSupportActionBar().setDisplayHomeAsUpEnabled(true);\n" +
                    "    }\n" +
                    "    if (toolbar != null) {\n" +
                    "        toolbar.setNavigationOnClickListener(new View.OnClickListener() {\n" +
                    "            @Override\n" +
                    "            public void onClick(View v) {\n" +
                    "                finish();\n" +
                    "            }\n" +
                    "        });\n" +
                    "    }\n" +
                    "    setTitle(\"代码片段\");\n" +
                    "\n" +
                    "    // Code\n" +
                    "    mCodeView = (CodeView) findViewById(R.id.code_view);\n" +
                    "    if (mCodeView != null) {\n" +
                    "        mCodeView.setTheme(CodeViewTheme.ANDROIDSTUDIO).fillColor();\n" +
                    "        mCodeView.showCode(mCodeContent);\n" +
                    "    }\n" +
                    "}";
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.code_view_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_copy_code:
                // 复制代码
                CommonUtils.copyToClipboard(mContext, "复制代码", mCodeContent);
                Toast.makeText(mContext, "复制代码成功", Toast.LENGTH_SHORT).show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
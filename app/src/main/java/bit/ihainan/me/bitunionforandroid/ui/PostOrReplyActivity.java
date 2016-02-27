package bit.ihainan.me.bitunionforandroid.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.utils.EditTextUndoRedo;
import bit.ihainan.me.bitunionforandroid.utils.Global;

public class PostOrReplyActivity extends AppCompatActivity {

    // UI References
    private EditText mMessage;
    private ImageView boldAction, quoteAction, undoAction, redoAction, testAction;

    private EditTextUndoRedo editTextUndoRedo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_or_reply);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        setTitle("发布新帖");

        mMessage = (EditText) findViewById(R.id.new_post_message);
        mMessage.setLineSpacing(10, 1.3f);
        mMessage.setText("本科我宿舍的一个是国防生，本科毕业就去了保定徐水的38军某防空团了，到现在差不多有7年多的时间\n\n到现在差不多有7年多的时间\n本科我宿舍的一个是国防生，本科毕业就去了保定徐水的38军某防空团了，到现在差不多有7年多的时间\n到现在差不多有7年多的时间" + "本科我宿舍的一个是国防生，本科毕业就去了保定徐水的38军某防空团了，到现在差不多有7年多的时间\n\n到现在差不多有7年多的时间\n本科我宿舍的一个是国防生，本科毕业就去了保定徐水的38军某防空团了，到现在差不多有7年多的时间\n到现在差不多有7年多的时间" + "本科我宿舍的一个是国防生，本科毕业就去了保定徐水的38军某防空团了，到现在差不多有7年多的时间\n\n到现在差不多有7年多的时间\n本科我宿舍的一个是国防生，本科毕业就去了保定徐水的38军某防空团了，到现在差不多有7年多的时间\n到现在差不多有7年多的时间");
        editTextUndoRedo = new EditTextUndoRedo(mMessage);

        // Buttons
        boldAction = (ImageView) findViewById(R.id.bold_action);
        undoAction = (ImageView) findViewById(R.id.undo_action);
        redoAction = (ImageView) findViewById(R.id.redo_action);
        quoteAction = (ImageView) findViewById(R.id.quote_action);
        testAction = (ImageView) findViewById(R.id.test_action);
        setUpActions();
    }

    private void setUpActions() {
        quoteAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTag("quote", false);
            }
        });


        boldAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTag("b", false);
            }
        });

        undoAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // mMessage.
                editTextUndoRedo.undo();
            }
        });

        redoAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextUndoRedo.redo();
            }
        });

        testAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view = getCurrentFocus();
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(view.getWindowToken(), 0);
                Intent intent = new Intent(PostOrReplyActivity.this, PreviewActivity.class);
                intent.putExtra(PreviewActivity.MESSAGE_CONTENT, mMessage.getText().toString());
                startActivity(intent);
            }
        });
    }

    private void addTag(String tag, boolean isWrapLine) {
        int startSelection = mMessage.getSelectionStart();
        int endSelection = mMessage.getSelectionEnd();
        String wrapLine = isWrapLine ? "\n" : "";
        String before = wrapLine + "[" + tag + "]";
        String after = "[/" + tag + "]" + wrapLine;
        String selectedStr = mMessage.getText().subSequence(startSelection, endSelection).toString();
        String replaceStr = before + selectedStr + after;
        mMessage.getText().replace(Math.min(startSelection, endSelection), Math.max(startSelection, endSelection),
                replaceStr, 0, replaceStr.length());

        if (startSelection == endSelection) mMessage.setSelection(startSelection + before.length());
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .showSoftInput(mMessage, InputMethodManager.SHOW_FORCED);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_post_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.send:
                View view = getCurrentFocus();
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(view.getWindowToken(), 0);
                Intent intent = new Intent(PostOrReplyActivity.this, PreviewActivity.class);
                intent.putExtra(PreviewActivity.MESSAGE_CONTENT, mMessage.getText().toString());
                startActivity(intent);
                break;
        }

        return true;
    }

}

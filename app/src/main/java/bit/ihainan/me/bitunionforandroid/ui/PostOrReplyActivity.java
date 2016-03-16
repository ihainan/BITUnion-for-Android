package bit.ihainan.me.bitunionforandroid.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.ui.fragment.EmoticonFragment;
import bit.ihainan.me.bitunionforandroid.utils.ui.EditTextUndoRedo;

public class PostOrReplyActivity extends AppCompatActivity {
    // TAGS
    public final static String TAG = PostOrReplyActivity.class.getSimpleName();
    public final static int CHOOSE_PHOTO_TAG = 0;
    public final static int CHOOSE_FILE_TAG = 1;

    // UI References
    private DrawerLayout mDrawer;
    private EditText mMessage;
    private ImageView mBoldAction, mQuoteAction, mUndoAction, mRedoAction, mTestAction, mEmojiAction;
    private ImageView mItalic, mLink, mAt, mImage, mAttachment;
    private EmoticonFragment mEmoticonFragment;
    private EditTextUndoRedo editTextUndoRedo;
    private CardView mAttachmentLayout;
    private ImageView mFileTypeImage, mDelButton;
    private TextView mAttachmentName, mAttachmentSize;

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

        mMessage = (EditText) findViewById(R.id.message);
        mMessage.setLineSpacing(10, 1.3f);
        // mMessage.setText(":bz_71:本科我宿舍的一个是国防生，本科毕业就去了保定徐水的38军某防空团了，到现在差不多有7年多的时间\n\n到现在差不多有7年多的时间\n本科我宿舍的一个是国防生，本科毕业就去了保定徐水的38军某防空团了，到现在差不多有7年多的时间\n到现在差不多有7年多的时间" + "本科我宿舍的一个是国防生，本科毕业就去了保定徐水的38军某防空团了，到现在差不多有7年多的时间\n\n到现在差不多有7年多的时间\n本科我宿舍的一个是国防生，本科毕业就去了保定徐水的38军某防空团了，到现在差不多有7年多的时间\n到现在差不多有7年多的时间" + "本科我宿舍的一个是国防生，本科毕业就去了保定徐水的38军某防空团了，到现在差不多有7年多的时间\n\n到现在差不多有7年多的时间\n本科我宿舍的一个是国防生，本科毕业就去了保定徐水的38军某防空团了，到现在差不多有7年多的时间\n到现在差不多有7年多的时间");
        mMessage.setText("普通文本: 这个家教是我之前做的，平均每个月能收入4k。现在因为我个人原因，帮忙学生找一位新的认真负责的家教。我做的时候是周末50一小时，平时100元一次(陪写作业，一般每次2-3小时之间)这个价格请自行和家长商量，我这个仅供参考。" +
                "\n\n加粗：[b]加粗[/b]" +
                "\n\n链接：[url=http://www.baidu.com]百度[/url]" +
                "\n\n表情：:nugget::sweating::wink::tongue:"
                + "\n\n引用：[quote]我是一个引用[/quote]");
        editTextUndoRedo = new EditTextUndoRedo(mMessage);


        // Fragments
        mDrawer = (DrawerLayout) findViewById(R.id.post_drawer);
        mEmoticonFragment = new EmoticonFragment();
        getFragmentManager().beginTransaction().replace(R.id.post_emoticons, mEmoticonFragment).commit();
        mEmoticonFragment.setEmoticonListener(new EmoticonFragment.EmoticonListener() {
            @Override
            public void onEmoticonSelected(String name) {
                mMessage.getText().insert(mMessage.getSelectionStart(), name);
                mDrawer.closeDrawer(Gravity.RIGHT);
            }
        });


        // Attachment Layout
        mAttachmentLayout = (CardView) findViewById(R.id.attachment_layout);
        mFileTypeImage = (ImageView) findViewById(R.id.attachment_icon);
        mDelButton = (ImageView) findViewById(R.id.attachment_remove);
        mAttachmentName = (TextView) findViewById(R.id.attachment_name);
        mAttachmentSize = (TextView) findViewById(R.id.attachment_size);
        setupAttachmentLayout(false, null, null, null);

        // Buttons
        mBoldAction = (ImageView) findViewById(R.id.bold_action);
        mUndoAction = (ImageView) findViewById(R.id.undo_action);
        mRedoAction = (ImageView) findViewById(R.id.redo_action);
        mQuoteAction = (ImageView) findViewById(R.id.quote_action);
        mTestAction = (ImageView) findViewById(R.id.test_action);
        mEmojiAction = (ImageView) findViewById(R.id.emoji_action);
        mItalic = (ImageView) findViewById(R.id.italic_action);
        mImage = (ImageView) findViewById(R.id.img_action);
        mAt = (ImageView) findViewById(R.id.at_action);
        mAttachment = (ImageView) findViewById(R.id.attachment_action);
        setUpActions();
    }

    private void setupAttachmentLayout(Boolean visibility, Boolean isImage, String fileName, Long fileSize) {
        if (!visibility) mAttachmentLayout.setVisibility(View.GONE);
        else {
            mAttachmentLayout.setVisibility(View.VISIBLE);
            if (isImage) mFileTypeImage.setImageResource(R.drawable.ic_photo_file);
            else mFileTypeImage.setImageResource(R.drawable.ic_documentation_file);

            mAttachmentName.setText(fileName);
            mAttachmentSize.setText((fileSize / 1024) + " KB");
            mDelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setupAttachmentLayout(false, null, null, null);
                }
            });
        }
    }

    private void setUpActions() {
        // registerForContextMenu(mAttachment);

        mQuoteAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTag("quote", false);
            }
        });

        mBoldAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTag("b", false);
            }
        });

        mUndoAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // mMessage.
                editTextUndoRedo.undo();
            }
        });

        mRedoAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextUndoRedo.redo();
            }
        });

        mTestAction.setOnClickListener(new View.OnClickListener() {
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

        mEmojiAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDrawer.isDrawerOpen(Gravity.RIGHT)) {
                    mDrawer.closeDrawer(Gravity.RIGHT);
                } else {
                    mDrawer.openDrawer(Gravity.RIGHT);
                }
            }
        });

        mItalic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTag("i", false);
            }
        });

        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, CHOOSE_PHOTO_TAG);
            }
        });

        mAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, CHOOSE_FILE_TAG);
            }
        });
    }

    // 菜单
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.equals(mAttachment)) {
            menu.add(0, 1, Menu.NONE, "添加图片");
            menu.add(0, 2, Menu.NONE, "添加文件");
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getGroupId() == 0) {
            // 附件
            switch (item.getItemId()) {
                case 1:
                    break;
                case 2:
                    break;
            }
        }

        return true;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();

            // Get the Uri of the selected file
            String uriString = uri.toString();
            File myFile = new File(uriString);
            String displayName = null;

            if (uriString.startsWith("content://")) {
                Cursor cursor = null;
                try {
                    cursor = getContentResolver().query(uri, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                } finally {
                    cursor.close();
                }
            } else if (uriString.startsWith("file://")) {
                displayName = myFile.getName();
            }

            // Size
            Cursor cursor = getContentResolver().query(uri,
                    null, null, null, null);
            cursor.moveToFirst();
            long size = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
            cursor.close();

            setupAttachmentLayout(true, requestCode == CHOOSE_PHOTO_TAG, displayName, size);
        }
    }
}

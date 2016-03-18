package me.ihainan.bu.app.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.ui.assist.SwipeActivity;
import me.ihainan.bu.app.ui.fragment.EmoticonFragment;
import me.ihainan.bu.app.utils.ACache;
import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.Global;
import me.ihainan.bu.app.utils.ui.EditTextUndoRedo;

public class NewPostActivity extends SwipeActivity {
    // TAGS
    public final static String TAG = NewPostActivity.class.getSimpleName();
    public final static int REQUEST_CHOOSE_PHOTO_TAG = 0;
    public final static int REQUEST_CHOOSE_FILE_TAG = 1;
    public final static int REQUEST_PREVIEW_TAG = 2;

    public final static String NEW_POST_ACTION_TAG = "NEW_POST_ACTION"; // 可选 thread / post
    public final static String NEW_POST_SUBJECT_TAG = "NEW_POST_SUBJECT_TAG"; // 主题
    public final static String ACTION_POST = "newreply";
    public final static String ACTION_THREAD = "newthread";
    public final static String NEW_POST_ATTACHMENT_TAG = "NEW_POST_ATTACHMENT_TAG"; // 附件

    public final static String NEW_POST_QUOTE_TAG = "NEW_POST_QUOTE"; // 引用内容
    public final static String NEW_POST_TID_TAG = "NEW_POST_TID"; // 回复帖子 ID
    public final static String NEW_POST_FID_TAG = "NEW_POST_FID"; // 回复论坛组 ID

    public final static String NEW_POST_FLOOR_TAG = "NEW_POST_FLOOR_TAG"; // 回复论坛组 ID

    // EditText draft
    public final static String DRAFT_POST_SUBJECT = "DRAFT_POST_SUBJECT"; // 草稿 - 回帖 - 主题
    public final static String DRAFT_POST_CONTENT = "DRAFT_POST_CONTENT"; // 草稿 - 回帖- 正文
    public final static String DRAFT_POST_ATTACHMENT = "DRAFT_POST_ATTACHMENT"; // 草稿 - 回帖- 附件

    public final static String DRAFT_THREAD_SUBJECT = "DRAFT_THREAD_SUBJECT"; // 草稿 - 主题 - 主题
    public final static String DRAFT_THREAD_CONTENT = "DRAFT_THREAD_CONTENT"; // 草稿 - 主题 - 正文
    public final static String DRAFT_THREAD_ATTACHMENT = "DRAFT_THREAD_ATTACHMENT"; // 草稿 - 主题 - 附件

    // UI References
    private DrawerLayout mDrawer;
    private HorizontalScrollView buttonPanel;
    private EditText mMessage, mSubject;
    private ImageView mBoldAction, mQuoteAction, mUndoAction, mRedoAction, mTestAction, mEmojiAction;
    private ImageView mItalic, mLink, mAt, mImage, mAttachment;
    private EmoticonFragment mEmoticonFragment;
    private EditTextUndoRedo editTextUndoRedo;
    private CardView mAttachmentLayout;
    private ImageView mFileTypeImage, mDelButton;
    private TextView mAttachmentName, mAttachmentSize;

    // Data
    private String mAction, mQuote;
    private Long mFid, mTid, mFloor;
    private byte[] mAttachmentByteArray;

    private void loadCachedData() {
        if (mAction.equals(ACTION_POST)) {
            // 主题
            mSubject.setVisibility(View.GONE);

            // 内容
            String cachedContent = Global.getCache(this).getAsStringWithNewLine(DRAFT_POST_CONTENT + "_" + mTid);
            mMessage.setHint("回帖内容");
            if (cachedContent != null) mMessage.append(cachedContent);
            else mMessage.setText("");

            // 引用
            if (mQuote != null) {
                mMessage.append("\n" + mQuote);
            }
        } else {
            // 主题
            mSubject.setVisibility(View.VISIBLE);
            String cachedSubject = Global.getCache(this).getAsStringWithNewLine(DRAFT_THREAD_SUBJECT + "_" + mFid);
            mSubject.setHint("帖子主题");
            if (cachedSubject != null) mSubject.append(cachedSubject);
            else mSubject.setText("");

            // 内容
            String cachedContent = Global.getCache(this).getAsStringWithNewLine(DRAFT_THREAD_CONTENT + "_" + mFid);
            mMessage.setHint("主题内容");
            if (cachedContent != null) mMessage.append(cachedContent);
            else mMessage.setText("");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_or_reply);

        // Get Extra Data
        getExtra();

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        if (ACTION_POST.equals(mAction)) setTitle("发布新帖");
        else setTitle("发布主题");

        // UI Ref
        mMessage = (EditText) findViewById(R.id.message);
        mSubject = (EditText) findViewById(R.id.subject);
        mMessage.setLineSpacing(10, 1.3f);
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
        buttonPanel = (HorizontalScrollView) findViewById(R.id.buttonPanel);

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

        // Actions
        setUpActions();

        // 从缓存中提取数据
        loadCachedData();

        // Swipe
        setSwipeAnyWhere(false);
    }

    /**
     * 获取 Bundle 数据
     */
    private void getExtra() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            mAction = bundle.getString(NEW_POST_ACTION_TAG);
            if (mAction == null || "".equals(mAction)) finish();
            if (mAction.equals(ACTION_POST)) setTitle("回复帖子");
            else setTitle("发布主题");
            if (ACTION_POST.equals(mAction)) mTid = bundle.getLong(NEW_POST_TID_TAG);
            if (ACTION_THREAD.equals(mAction)) mFid = bundle.getLong(NEW_POST_FID_TAG);
            mQuote = bundle.getString(NEW_POST_QUOTE_TAG);
            mFloor = bundle.getLong(NEW_POST_FLOOR_TAG, 1);
        }
    }

    private void setUpActions() {
        if (mAction.equals(ACTION_THREAD) && "".equals(mSubject.getText())) mSubject.requestFocus();
        else mMessage.requestFocus();

        mMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String content = s.toString();
                if (mAction.equals(ACTION_POST)) {
                    Global.getCache(NewPostActivity.this).put(DRAFT_POST_CONTENT + "_" + mTid, content, ACache.TIME_DAY * 2);
                } else {
                    Global.getCache(NewPostActivity.this).put(DRAFT_THREAD_CONTENT + "_" + mFid, content, ACache.TIME_DAY * 2);
                }
            }
        });

        mSubject.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String subject = s.toString();
                if (mAction.equals(ACTION_POST)) {
                    Global.getCache(NewPostActivity.this).put(DRAFT_POST_SUBJECT + "_" + mTid, subject, ACache.TIME_DAY * 2);
                } else {
                    Global.getCache(NewPostActivity.this).put(DRAFT_THREAD_SUBJECT + "_" + mFid, subject, ACache.TIME_DAY * 2);
                }
            }
        });

        mAt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTag("@", false);
            }
        });

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
                startActivityForResult(photoPickerIntent, REQUEST_CHOOSE_PHOTO_TAG);
            }
        });

        mAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, REQUEST_CHOOSE_FILE_TAG);
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
                // 进入预览界面
                View view = getCurrentFocus();
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(view.getWindowToken(), 0);

                // TODO: 长度、内容限制
                if (ACTION_THREAD.equals(mAction) && ("".equals(mSubject.getText().toString()))) {
                    Snackbar.make(buttonPanel, R.string.error_subject_required, Snackbar.LENGTH_LONG).show();
                    mSubject.requestFocus();
                } else if (mMessage.getText().toString().length() < 5) {
                    Snackbar.make(buttonPanel, R.string.error_message_length_short, Snackbar.LENGTH_LONG).show();
                    mMessage.requestFocus();
                } else {
                    Intent intent = new Intent(NewPostActivity.this, PreviewActivity.class);
                    intent.putExtra(PreviewActivity.MESSAGE_CONTENT, mMessage.getText().toString());
                    intent.putExtra(NEW_POST_ACTION_TAG, mAction);
                    intent.putExtra(NEW_POST_ACTION_TAG, mAction);
                    intent.putExtra(NEW_POST_SUBJECT_TAG, mSubject.getText().toString());
                    if (mTid != null) intent.putExtra(NEW_POST_TID_TAG, mTid);
                    if (mFid != null) intent.putExtra(NEW_POST_FID_TAG, mFid);
                    intent.putExtra(NEW_POST_FLOOR_TAG, mFloor);
                    intent.putExtra(NEW_POST_ATTACHMENT_TAG, mAttachmentByteArray);

                    startActivityForResult(intent, REQUEST_PREVIEW_TAG);
                }
        }

        return true;
    }

    private void fillAttachmentView(Uri uri, int requestCode) {
        try {
            if (uri != null) {
                // Get byte array
                getByteArray(uri);

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

                setupAttachmentLayout(true, requestCode == REQUEST_CHOOSE_PHOTO_TAG, displayName, size);
            } else {
                setupAttachmentLayout(false, null, null, null);
            }
        } catch (IOException e) {
            String message = getString(R.string.error_insert_attachment);
            Log.e(TAG, message, e);
            CommonUtils.debugToast(this, message);
            Snackbar.make(mMessage, message, Snackbar.LENGTH_LONG).show();
        }
    }

    private void getByteArray(Uri uri) throws IOException {
        InputStream iStream = getContentResolver().openInputStream(uri);
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = iStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        iStream.close();
        byteBuffer.close();
        mAttachmentByteArray = byteBuffer.toByteArray();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == REQUEST_CHOOSE_PHOTO_TAG || requestCode == REQUEST_CHOOSE_FILE_TAG)
                && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            fillAttachmentView(uri, requestCode);
        } else if (requestCode == REQUEST_PREVIEW_TAG) {
            if (resultCode == RESULT_OK) {
                if (mAction.equals(ACTION_POST)) {
                    Global.getCache(NewPostActivity.this).remove(DRAFT_POST_CONTENT + "_" + mTid);
                    Global.getCache(NewPostActivity.this).remove(DRAFT_POST_SUBJECT + "_" + mTid);
                } else {
                    Global.getCache(NewPostActivity.this).remove(DRAFT_THREAD_CONTENT + "_" + mFid);
                    Global.getCache(NewPostActivity.this).remove(DRAFT_THREAD_SUBJECT + "_" + mFid);
                }

                Intent resultData = new Intent();
                setResult(Activity.RESULT_OK, resultData);
                finish();
            }
        }
    }
}
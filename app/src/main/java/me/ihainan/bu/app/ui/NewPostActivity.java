package me.ihainan.bu.app.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.ui.fragment.EmoticonFragment;
import me.ihainan.bu.app.utils.BUApplication;
import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.ui.EditTextUndoRedo;

public class NewPostActivity extends AppCompatActivity {
    private final static String TAG = NewPostActivity.class.getSimpleName();

    // 权限 Tags
    private final static int PERMISSIONS_REQUEST_READ_IMAGE = 1;
    private final static int PERMISSIONS_REQUEST_READ_FILE = 2;

    // 请求 Tags
    private final static int REQUEST_CHOOSE_PHOTO_TAG = 0;   // 添加图片附件请求
    private final static int REQUEST_CHOOSE_FILE_TAG = 1;    // 添加文件附件请求
    private final static int REQUEST_PREVIEW_TAG = 2;    // 查看发帖预览请求

    // 来源 Intent Tags
    public final static String ACTION_TAG = "ACTION"; // 预期操作（发主题 / 回帖），对应 ACTION_THREAD / ACTION_POST
    public final static String ACTION_NEW_POST = "ACTION_NEW_POST";    // 发布新回复操作
    public final static String ACTION_NEW_THREAD = "ACTION_NEW_THREAD"; // 发布新主题操作

    public final static String NEW_POST_TID_TAG = "NEW_POST_TID"; // 回复帖子 ID
    public final static String NEW_POST_QUOTE_CONTENT_TAG = "NEW_POST_QUOTE_CONTENT"; // 引用内容
    public final static String NEW_POST_MAX_FLOOR_TAG = "NEW_POST_MAX_FLOOR"; // 回复帖子当前最高楼层，用于预览显示
    public final static String NEW_THREAD_FID_TAG = "NEW_THREAD_FID"; // 回复板块 ID

    // 提交给 Preview Activity
    public final static String CONTENT_SUBJECT_TAG = "CONTENT_SUBJECT_TAG";    // 标题
    public final static String CONTENT_MESSAGE_TAG = "CONTENT_MESSAGE_TAG";    // 内容
    public final static String CONTENT_ATTACHMENT_URI = "CONTENT_ATTACHMENT_URI";   // 附件 Uri


    // UI References
    private DrawerLayout mDrawer;
    private HorizontalScrollView mButtonPanel;
    private EditText mETMessage, mETSubject;
    private ImageView mIVUndoAction, mIVRedoAction, mIVEmotionAction, mIVAttachment, mIvBold, mIvItalic, mIvQuote;
    private EditTextUndoRedo mETUndoRedo;

    // Data
    private String mAction, mQuoteContent;
    private Long mFid, mTid, mFloor;
    private Uri mAttachmentUri;   // 附件 URI
    private String mRealImageName;  // 真实附件名
    private final static int MAX_COMPRESSED_IMAGE_SIZE = 900;   // 压缩之后的最大图片大小，单位为 KB

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_better_post);
        getExtra();

        // Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitEditorAction();
            }
        });
        if (ACTION_NEW_POST.equals(mAction)) setTitle(getString(R.string.title_new_post));
        else setTitle(getString(R.string.title_new_thread));

        // Editor UI
        mETSubject = (EditText) findViewById(R.id.subject);
        if (ACTION_NEW_POST.equals(mAction)) mETSubject.setVisibility(View.GONE);
        else mETSubject.setVisibility(View.VISIBLE);

        mETMessage = (EditText) findViewById(R.id.message);
        mETMessage.setLineSpacing(10, 1.3f);
        mETUndoRedo = new EditTextUndoRedo(mETMessage);

        if (ACTION_NEW_THREAD.equals(mAction))
            mETSubject.requestFocus();
        else mETMessage.requestFocus();

        if (mQuoteContent != null) {
            String message = mETMessage.getText().toString();
            if ("".equals(message.trim()) || message.endsWith("\n\n"))
                mETMessage.append(mQuoteContent);
            else
                mETMessage.append("\n\n" + mQuoteContent);
        }

        // Fragments
        mDrawer = (DrawerLayout) findViewById(R.id.post_drawer);
        EmoticonFragment mEmoticonFragment = new EmoticonFragment();
        getFragmentManager().beginTransaction().replace(R.id.post_emoticons, mEmoticonFragment).commit();
        mEmoticonFragment.setEmoticonListener(new EmoticonFragment.EmoticonListener() {
            @Override
            public void onEmoticonSelected(String name) {
                mETMessage.getText().insert(mETMessage.getSelectionStart(), name);
                mETMessage.requestFocus();
                mDrawer.closeDrawer(Gravity.RIGHT);
            }
        });

        // Buttons
        mButtonPanel = (HorizontalScrollView) findViewById(R.id.buttonPanel);
        mIVUndoAction = (ImageView) findViewById(R.id.undo_action);
        mIVRedoAction = (ImageView) findViewById(R.id.redo_action);
        mIVEmotionAction = (ImageView) findViewById(R.id.emotion_action);
        mIVAttachment = (ImageView) findViewById(R.id.attachment_action);
        mIvBold = (ImageView) findViewById(R.id.bold_action);
        mIvItalic = (ImageView) findViewById(R.id.italic_action);
        mIvQuote = (ImageView) findViewById(R.id.quote_action);
        if (BUApplication.enableAdvancedEditor) {
            mIvBold.setVisibility(View.VISIBLE);
            mIvItalic.setVisibility(View.VISIBLE);
            mIvQuote.setVisibility(View.VISIBLE);
        } else {
            mIvBold.setVisibility(View.GONE);
            mIvItalic.setVisibility(View.GONE);
            mIvQuote.setVisibility(View.GONE);
        }

        // 初始化 UI 和按钮
        setUpButtonActions();
    }

    private void addTag(String tag, boolean isWrapLine) {
        int startSelection = mETMessage.getSelectionStart();
        int endSelection = mETMessage.getSelectionEnd();
        String wrapLine = isWrapLine ? "\n" : "";
        String before = wrapLine + "[" + tag + "]";
        String after = "[/" + tag + "]" + wrapLine;
        String selectedStr = mETMessage.getText().subSequence(startSelection, endSelection).toString();
        String replaceStr = before + selectedStr + after;
        mETMessage.getText().replace(Math.min(startSelection, endSelection), Math.max(startSelection, endSelection),
                replaceStr, 0, replaceStr.length());

        if (startSelection == endSelection)
            mETMessage.setSelection(startSelection + before.length());
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .showSoftInput(mETMessage, InputMethodManager.SHOW_FORCED);
    }

    /**
     * 获取 Bundle 数据
     */
    private void getExtra() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            mAction = bundle.getString(ACTION_TAG);

            if (ACTION_NEW_POST.equals(mAction)) mTid = bundle.getLong(NEW_POST_TID_TAG);
            if (ACTION_NEW_THREAD.equals(mAction)) mFid = bundle.getLong(NEW_THREAD_FID_TAG);

            mQuoteContent = bundle.getString(NEW_POST_QUOTE_CONTENT_TAG);
            mFloor = bundle.getLong(NEW_POST_MAX_FLOOR_TAG, 1);
        }
    }

    /**
     * 设置按钮点击事件
     */
    private void setUpButtonActions() {
        // 撤销操作
        mIVUndoAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mETUndoRedo.undo();
            }
        });

        // 重做操作
        mIVRedoAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mETUndoRedo.redo();
            }
        });

        // 添加表情
        mIVEmotionAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDrawer.isDrawerOpen(Gravity.RIGHT)) {
                    mDrawer.closeDrawer(Gravity.RIGHT);
                } else {
                    mDrawer.openDrawer(Gravity.RIGHT);
                }
            }
        });

        // 添加附件
        mIVAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAttachmentDialog(ACTION_ADD_ATTACHMENT);
            }
        });

        // 引用
        mIvQuote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTag("quote", false);
            }
        });

        // 加粗
        mIvBold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTag("b", false);
            }
        });

        // 斜体
        mIvItalic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addTag("i", false);
            }
        });
    }

    private final static int ACTION_ADD_ATTACHMENT = 1;
    private final static int ACTION_MOD_ATTACHMENT = 2;

    private void showAttachmentDialog(int actionType) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.content_editor_attachment_menu, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.show();

        LinearLayout addAttachmentLayout = (LinearLayout) dialogView.findViewById(R.id.layout_add_attachment);
        LinearLayout modAttachmentLayout = (LinearLayout) dialogView.findViewById(R.id.layout_mod_attachment);

        if (actionType == ACTION_ADD_ATTACHMENT) {
            addAttachmentLayout.setVisibility(View.VISIBLE);
            dialogView.findViewById(R.id.layout_add_image).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    if (ContextCompat.checkSelfPermission(NewPostActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(NewPostActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                PERMISSIONS_REQUEST_READ_IMAGE);
                    } else {
                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                        photoPickerIntent.setType("image/*");
                        startActivityForResult(photoPickerIntent, REQUEST_CHOOSE_PHOTO_TAG);
                    }
                }
            });

            dialogView.findViewById(R.id.layout_add_file).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    if (ContextCompat.checkSelfPermission(NewPostActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(NewPostActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                PERMISSIONS_REQUEST_READ_FILE);
                    } else {
                        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        intent.setType("*/*");
                        startActivityForResult(intent, REQUEST_CHOOSE_FILE_TAG);
                    }
                }
            });
        } else if (ACTION_MOD_ATTACHMENT == actionType) {
            modAttachmentLayout.setVisibility(View.VISIBLE);
            TextView tvViewAttachment = (TextView) dialogView.findViewById(R.id.tv_view_attachment);
            TextView tvDelAttachment = (TextView) dialogView.findViewById(R.id.tv_remove_attachment);
            TextView tvFilename = (TextView) dialogView.findViewById(R.id.tv_file_name);

            tvFilename.setText(mRealImageName);

            // 布局
            ContentResolver cR = getContentResolver();
            final String type = cR.getType(mAttachmentUri);
            if (type.startsWith("image")) {
                tvViewAttachment.setText(getString(R.string.action_view_image_attachment));
                tvDelAttachment.setText(getString(R.string.action_remove_image_attachment));
            } else {
                tvViewAttachment.setText(getString(R.string.action_view_file_attachment));
                tvDelAttachment.setText(getString(R.string.action_remove_file_attachment));
            }

            // 查看附件
            dialogView.findViewById(R.id.layout_view_attachment).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    Intent intent = new Intent(Intent.ACTION_VIEW, mAttachmentUri);
                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        String message = getString(R.string.error_open_attachment);
                        Log.e(TAG, message, e);
                        CommonUtils.debugToast(NewPostActivity.this, message);
                        Snackbar.make(mButtonPanel, message, Snackbar.LENGTH_LONG).show();
                    }
                }
            });

            // 删除附件
            dialogView.findViewById(R.id.layout_remove_attachment).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                    mRealImageName = null;
                    mIVAttachment.setImageResource(R.drawable.ic_attachment_black_24dp);
                    mAttachmentUri = null;
                    mIVAttachment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showAttachmentDialog(ACTION_ADD_ATTACHMENT);
                        }
                    });
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_IMAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, REQUEST_CHOOSE_PHOTO_TAG);
                } else {
                    Toast.makeText(this, getString(R.string.error_insert_attachment), Toast.LENGTH_LONG).show();
                }
                break;
            case PERMISSIONS_REQUEST_READ_FILE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");
                    startActivityForResult(intent, REQUEST_CHOOSE_FILE_TAG);
                } else {
                    Toast.makeText(this, getString(R.string.error_insert_attachment), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    private final static String[] SUPPORT_FILE_TYPE = {"txt", "gif", "jpg", "png", "rar", "zip", "swf", "nfo", "gz", "gz2", "tgz", "bz", "rpm", "deb", "7z", "torrent"};

    private void doAfterAddingAttachmentNew() {
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(mAttachmentUri, null, null, null, null);

            final ProgressDialog dialog = ProgressDialog.show(this, "",
                    getString(R.string.processing_attachment), true);
            dialog.show();
            setFinishOnTouchOutside(false);

            // 文件名
            if (cursor != null && cursor.moveToFirst()) {
                if (mRealImageName == null)
                    mRealImageName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            }

            // 文件大小
            cursor.moveToFirst();
            long size = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));

            // 文件类型
            ContentResolver cR = getContentResolver();
            final String type = cR.getType(mAttachmentUri);

            boolean isSupport = false;
            for (String format : SUPPORT_FILE_TYPE) {
                if (mAttachmentUri.toString().endsWith(format)) {
                    isSupport = true;
                    break;
                }
            }

            if (!type.startsWith("image") && !isSupport) {
                if (dialog != null) dialog.dismiss();
                setFinishOnTouchOutside(true);

                mAttachmentUri = null;
                String message = getString(R.string.error_insert_attachment) + ": 不支持的文件格式";
                Log.w(TAG, message);
                CommonUtils.debugToast(this, message);
                Snackbar.make(mButtonPanel, message, Snackbar.LENGTH_LONG).show();

                return;
            }

            if (size > 1000000) {
                if (type.startsWith("image")) {
                    // final long oriSize = size;
                    // Remove cache directory
                    CommonUtils.deleteTmpDir(this);
                    mETMessage.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mAttachmentUri = CommonUtils.compressImage(NewPostActivity.this, mAttachmentUri, MAX_COMPRESSED_IMAGE_SIZE);
                            // mAttachmentUri = CommonUtils.compressImageNew(NewPostActivity.this, mAttachmentUri, oriSize, MAX_COMPRESSED_IMAGE_SIZE * 1000);
                            doAfterProcessingAttachment(dialog);
                        }
                    }, 1000);
                } else {
                    if (dialog != null) dialog.dismiss();
                    setFinishOnTouchOutside(true);

                    mAttachmentUri = null;
                    String message = getString(R.string.error_insert_attachment) + ": 文件大小超过 1M";
                    Log.w(TAG, message);
                    CommonUtils.debugToast(this, message);
                    Snackbar.make(mButtonPanel, message, Snackbar.LENGTH_LONG).show();
                }
            } else doAfterProcessingAttachment(dialog);
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void doAfterProcessingAttachment(ProgressDialog dialog) {
        if (dialog != null) dialog.dismiss();
        setFinishOnTouchOutside(true);

        // 改变 Icon 颜色
        Drawable newIcon = getResources().getDrawable(R.drawable.ic_attachment_black_24dp);
        newIcon.mutate().setColorFilter(getResources().getColor(R.color.primary), PorterDuff.Mode.SRC_IN);
        mIVAttachment.setImageDrawable(newIcon);

        // 监听
        mIVAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAttachmentDialog(ACTION_MOD_ATTACHMENT);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == REQUEST_CHOOSE_PHOTO_TAG || requestCode == REQUEST_CHOOSE_FILE_TAG)
                && resultCode == RESULT_OK && data != null) {
            mAttachmentUri = data.getData();
            if (mAttachmentUri == null) {
                String message = getString(R.string.error_insert_attachment) + ": 获取图片失败";
                Log.w(TAG, message);
                CommonUtils.debugToast(this, message);
                Snackbar.make(mButtonPanel, message, Snackbar.LENGTH_LONG).show();
            } else doAfterAddingAttachmentNew();
        } else if (requestCode == REQUEST_PREVIEW_TAG) {
            if (resultCode == RESULT_OK) {
                Intent resultData = new Intent();
                setResult(Activity.RESULT_OK, resultData);
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        exitEditorAction();
    }

    /**
     * 退出编辑器前先进行确认
     */
    private void exitEditorAction() {
        if (mAttachmentUri != null || !"".equals(mETSubject.getText().toString())
                || !"".equals(mETMessage.getText().toString())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(NewPostActivity.this);
            builder.setTitle(getString(R.string.title_warning))

                    .setMessage(getString(R.string.message_exit_editor))
                    .setPositiveButton(getString(R.string.button_yes), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            CommonUtils.deleteTmpDir(NewPostActivity.this);
                            finish();
                        }
                    }).setNegativeButton(getString(R.string.button_no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        } else {
            CommonUtils.deleteTmpDir(NewPostActivity.this);
            finish();
        }
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

                if (ACTION_NEW_THREAD.equals(mAction) && ("".equals(mETSubject.getText().toString()))) {
                    Snackbar.make(mButtonPanel, R.string.error_subject_required, Snackbar.LENGTH_LONG).show();
                    mETSubject.requestFocus();
                } else if (mETMessage.getText().toString().length() < 3) {
                    Snackbar.make(mButtonPanel, R.string.error_message_length_short, Snackbar.LENGTH_LONG).show();
                    mETMessage.requestFocus();
                } else {
                    Intent intent = new Intent(NewPostActivity.this, PreviewActivity.class);
                    intent.putExtra(ACTION_TAG, mAction);
                    intent.putExtra(CONTENT_SUBJECT_TAG, mETSubject.getText().toString());
                    intent.putExtra(CONTENT_MESSAGE_TAG, mETMessage.getText().toString());
                    if (mTid != null) intent.putExtra(NEW_POST_TID_TAG, mTid);
                    if (mFid != null) intent.putExtra(NEW_THREAD_FID_TAG, mFid);
                    intent.putExtra(NEW_POST_MAX_FLOOR_TAG, mFloor);
                    intent.putExtra(CONTENT_ATTACHMENT_URI, mAttachmentUri == null ? null : mAttachmentUri.toString());

                    startActivityForResult(intent, REQUEST_PREVIEW_TAG);
                }
        }

        return true;
    }
}
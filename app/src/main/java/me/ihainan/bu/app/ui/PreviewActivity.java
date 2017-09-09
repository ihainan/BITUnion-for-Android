package me.ihainan.bu.app.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.models.Member;
import me.ihainan.bu.app.ui.assist.CustomSpan;
import me.ihainan.bu.app.ui.assist.SwipeActivity;
import me.ihainan.bu.app.utils.BUApplication;
import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.network.BUApi;
import me.ihainan.bu.app.utils.ui.HtmlUtil;
import me.ihainan.bu.app.utils.ui.PicassoImageGetter;

import static me.ihainan.bu.app.ui.NewPostActivity.POST_RESULT_TAG;

public class PreviewActivity extends SwipeActivity {
    // TAG
    private final static String TAG = PreviewActivity.class.getSimpleName();

    private CardView mCardView;

    // Data
    private String mSubject, mMessageContent, mMessageHtmlContent, mAction, mActionStr;
    private Long mFloor, mFid, mTid;
    private Uri mUri;
    private byte[] mAttachmentByteArray;
    private String mFilename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        getExtra();
        setSwipeAnyWhere(false);

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
        setTitle("预览");

        // 卡片
        fillCardView();
    }

    private void getExtra() {
        Bundle bundle = getIntent().getExtras();

        // 提取数据
        mMessageContent = bundle.getString(NewPostActivity.CONTENT_MESSAGE_TAG);
        if (mMessageContent != null) {
            if (BUApplication.enableDisplayDeviceInfo)
                mMessageContent += "\n\n\n[url=http://out.bitunion.org/thread-10614850-1-1.html][b]发自 " + CommonUtils.getDeviceName() + " @BU for Android[/b][/url]";
            mMessageHtmlContent = HtmlUtil.formatHtml(HtmlUtil.ubbToHtml(mMessageContent));
        }
        mFloor = bundle.getLong(NewPostActivity.NEW_POST_MAX_FLOOR_TAG);
        mAction = bundle.getString(NewPostActivity.ACTION_TAG);
        mSubject = bundle.getString(NewPostActivity.CONTENT_SUBJECT_TAG, "").trim();
        mTid = bundle.getLong(NewPostActivity.NEW_POST_TID_TAG);
        mFid = bundle.getLong(NewPostActivity.NEW_THREAD_FID_TAG);
        mActionStr = "发表" + (mAction.endsWith(NewPostActivity.ACTION_NEW_POST) ? "回复" : "主题");

        // 提取和显示附件
        if (bundle.getString(NewPostActivity.CONTENT_ATTACHMENT_URI, null) != null) {
            mUri = Uri.parse(bundle.getString(NewPostActivity.CONTENT_ATTACHMENT_URI, null));
            mAttachmentByteArray = getByteArray(mUri);
        }
    }

    private void fillCardView() {
        // CardView
        mCardView = (CardView) findViewById(R.id.card_view);

        // Author
        TextView author = (TextView) findViewById(R.id.thread_author_name);
        if (author != null) {
            author.setText(CommonUtils.decode(BUApplication.username));
        }

        // Author
        final ImageView avatar = (ImageView) findViewById(R.id.thread_author_avatar);
        CommonUtils.setUserAvatarClickListener(this,
                avatar, -1,
                BUApplication.username);
        CommonUtils.getAndCacheUserInfo(this, BUApplication.username,
                new CommonUtils.UserInfoAndFillAvatarCallback() {
                    @Override
                    public void doSomethingIfHasCached(Member member) {
                        String avatarURL = CommonUtils.getRealImageURL(member.avatar);
                        CommonUtils.setAvatarImageView(PreviewActivity.this, avatar,
                                avatarURL, R.drawable.default_avatar);
                    }
                });


        // Message
        TextView mMessageView = (TextView) findViewById(R.id.thread_message);
        if (mMessageView != null) {
            mMessageView.setMovementMethod(new CustomSpan.LinkTouchMovementMethod());
            mMessageView.setLineSpacing(BUApplication.lineSpacingExtra, BUApplication.lineSpacingMultiplier);
            SpannableString spannableString = new SpannableString(
                    Html.fromHtml(
                            CommonUtils.addSpaces(mMessageHtmlContent),
                            new PicassoImageGetter(this, mMessageView),
                            null));
            CustomSpan.setUpAllSpans(this, spannableString);
            mMessageView.setText(spannableString);
        }

        // Floor
        TextView mFloorView = (TextView) findViewById(R.id.post_floor);
        if (mFloorView != null) {
            mFloorView.setText("#" + mFloor);
        }

        // Date
        TextView mPostDateView = (TextView) findViewById(R.id.post_date);
        if (mPostDateView != null) {
            mPostDateView.setText("Recently");
        }

        // Submit
        Button mSubmitBtn = (Button) findViewById(R.id.submit);
        if (mSubmitBtn != null) {
            mSubmitBtn.setOnClickListener(submitListener);
            if (NewPostActivity.ACTION_NEW_POST.equals(mAction)) {
                mSubmitBtn.setText("发表回复");
            } else {
                mSubmitBtn.setText("发布主题");
            }
        }

        // Subject
        TextView mSubjectView = (TextView) findViewById(R.id.thread_subject);
        if (mSubjectView != null) {
            if ("".equals(mSubject)) {
                mSubjectView.setVisibility(View.GONE);
            } else {
                mSubjectView.setVisibility(View.VISIBLE);
                mSubjectView.setText(mSubject);
            }
        }

        // Device name
        TextView deviceName = (TextView) findViewById(R.id.device_name);
        if (deviceName != null) {
            if (BUApplication.enableDisplayDeviceInfo) {
                deviceName.setText(CommonUtils.getDeviceName());
            } else {
                deviceName.setText("");
            }
        }

        // Attachment
        if (mUri != null) {
            showAttachmentView();
        }
    }

    private void showAttachmentView() {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.thread_attachment_layout);
        LayoutInflater inflater = LayoutInflater.from(this);
        View itemView = inflater.inflate(R.layout.item_thread_detail_attachment, null, false);
        itemView.setVisibility(View.VISIBLE);
        linearLayout.setVisibility(View.VISIBLE);

        // UI references
        TextView attachmentName = (TextView) itemView.findViewById(R.id.thread_attachment_name);
        RelativeLayout attachmentImageLayout = (RelativeLayout) itemView.findViewById(R.id.thread_attachment_image_layout);
        final ImageView attachmentImage = (ImageView) itemView.findViewById(R.id.thread_attachment_image);
        itemView.findViewById(R.id.load_image_text).setVisibility(View.GONE);

        // 附件名
        Cursor cursor = null;
        try {
            // 文件名
            cursor = getContentResolver().query(mUri, null, null, null, null);
            mFilename = "Unknown file name";
            if (cursor != null && cursor.moveToFirst()) {
                mFilename = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                // mFilename = CommonUtils.truncateString(mFilename, 20);
            }

            // 文件大小
            if (cursor != null) {
                cursor.moveToFirst();
                long fileSize = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));

                attachmentName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(mUri);
                        startActivity(i);
                    }
                });

                attachmentName.setText(CommonUtils.truncateString(mFilename, 20) + "（" + CommonUtils.readableFileSize(fileSize) + "）");

                // 附件类型
                ContentResolver cR = getContentResolver();
                String fileType = cR.getType(mUri);

                // 显示图片
                if (fileType != null) {
                    if (fileType.startsWith("image")) {
                        attachmentImageLayout.setVisibility(View.VISIBLE);

                        final Point displaySize = CommonUtils.getDisplaySize(getWindowManager().getDefaultDisplay());
                        // final int size = (int) Math.ceil(Math.sqrt(displaySize.x * displaySize.y));
                        Picasso.with(PreviewActivity.this)
                                .load(mUri)
                                .resize(0, displaySize.y)
                                .into(attachmentImage);
                    } else {
                        attachmentImageLayout.setVisibility(View.GONE);
                    }
                }
            }
        } finally {
            if (cursor != null) cursor.close();
        }

        linearLayout.addView(itemView);
    }

    private final View.OnClickListener submitListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AlertDialog.Builder builder = new AlertDialog.Builder(PreviewActivity.this);
            builder.setTitle("提醒")
                    .setMessage("确认" + mActionStr + "?")
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            postNew();
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();
        }
    };

    private ProgressDialog dialog;

    private void postNew() {
        try {
            dialog = ProgressDialog.show(PreviewActivity.this, "",
                    "正在" + mActionStr, true);
            if (NewPostActivity.ACTION_NEW_POST.equals(mAction)) {
                BUApi.postNewPost(this, mTid, mMessageContent, mFilename, mAttachmentByteArray, listener, errorListener);
            } else {
                BUApi.postNewThread(this, mFid, mSubject, mMessageContent, mFilename, mAttachmentByteArray, listener, errorListener);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 成功拉取数据事件监听器
    private final Response.Listener<NetworkResponse> listener = new Response.Listener<NetworkResponse>() {
        @Override
        public void onResponse(NetworkResponse response) {
            if (isFinishing()) return;
            if (dialog != null) {
                dialog.dismiss();
            }
            if (response.statusCode == 200) {
                String jsonStr = new String(response.data).trim();
                try {
                    JSONObject jsonObject = new JSONObject(jsonStr);
                    if ("success".equals(jsonObject.getString("result"))) {
                        String message = mActionStr + "成功";
                        String debugMessage = message + " - " + jsonStr;
                        CommonUtils.debugToast(PreviewActivity.this, debugMessage);
                        Log.d(TAG, debugMessage);

                        // Finish
                        Intent resultData = new Intent();
                        resultData.putExtra(POST_RESULT_TAG, R.string.activity_success);
                        setResult(Activity.RESULT_OK, resultData);
                        finish();
                    } else {
                        String message = getString(R.string.error_unknown_msg) + ": " + jsonObject.getString("msg");
                        String debugMessage = message + " - " + response;
                        Log.w(TAG, debugMessage);
                        CommonUtils.debugToast(PreviewActivity.this, debugMessage);
                        Intent resultData = new Intent();
                        // resultData.putExtra(POST_RESULT_TAG, R.string.activity_failure);
                        setResult(Activity.RESULT_OK, resultData);
                        finish();
                    }
                } catch (JSONException e) {
                    String message = getString(R.string.error_parse_json);
                    String debugMessage = message + " - " + jsonStr;
                    Log.e(TAG, debugMessage, e);
                    CommonUtils.debugToast(PreviewActivity.this, debugMessage);
                    Intent resultData = new Intent();
                    // resultData.putExtra(POST_RESULT_TAG, R.string.activity_failure);
                    setResult(Activity.RESULT_OK, resultData);
                    finish();
                }
            } else {
                String message = mActionStr + "失败，请重试";
                String debugMessage = message + " - " + response.statusCode;
                Log.w(TAG, debugMessage);
                CommonUtils.debugToast(PreviewActivity.this, debugMessage);
                Intent resultData = new Intent();
                // resultData.putExtra(POST_RESULT_TAG, R.string.activity_failure);
                setResult(Activity.RESULT_OK, resultData);
                finish();
            }
        }
    };

    // 拉取数据失败事件监听器
    private final Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            if (isFinishing()) return;
            if (dialog != null) {
                dialog.dismiss();
            }
            String message = getString(R.string.error_network);
            String debugMessage = "PreviewActivity >> " + message;
            CommonUtils.debugToast(PreviewActivity.this, debugMessage);
            Log.e(TAG, debugMessage, error);
            Intent resultData = new Intent();
            // resultData.putExtra(POST_RESULT_TAG, R.string.activity_failure);
            setResult(Activity.RESULT_OK, resultData);
            finish();
        }
    };

    /**
     * 从 URI 中获取文件的 Byte 数组
     *
     * @param uri URI
     */
    private byte[] getByteArray(Uri uri) {
        InputStream iStream = null;
        ByteArrayOutputStream byteBuffer = null;
        try {
            iStream = getContentResolver().openInputStream(uri);
            byteBuffer = new ByteArrayOutputStream();

            int bufferSize = 1024, len;
            byte[] buffer = new byte[bufferSize];
            assert iStream != null;
            while ((len = iStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
        } catch (Exception e) {
            String message = "提取附件数据失败";
            Log.e(TAG, message, e);
            Snackbar.make(mCardView, message, Snackbar.LENGTH_LONG).show();
            return null;
        } finally {
            try {
                assert iStream != null;
                iStream.close();
                assert byteBuffer != null;
                byteBuffer.close();
            } catch (IOException e) {
                String message = "提取附件数据失败";
                Log.e(TAG, message, e);
                Snackbar.make(mCardView, message, Snackbar.LENGTH_LONG).show();
            }
        }

        return byteBuffer.toByteArray();
    }

    private void showSnackbar(String message) {
        Snackbar.make(mCardView, message, Snackbar.LENGTH_LONG)
                .setAction("Retry", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        postNew();
                    }
                }).show();
    }
}
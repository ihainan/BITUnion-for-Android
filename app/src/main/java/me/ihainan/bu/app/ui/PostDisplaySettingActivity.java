package me.ihainan.bu.app.ui;

import android.content.DialogInterface;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.models.Member;
import me.ihainan.bu.app.ui.assist.SwipeActivity;
import me.ihainan.bu.app.utils.BUApplication;
import me.ihainan.bu.app.utils.CommonUtils;

public class PostDisplaySettingActivity extends SwipeActivity {
    // TAG
    private final static String TAG = PostDisplaySettingActivity.class.getSimpleName();

    // UI References
    private TextView mMessageView;
    private TextView mSubject;
    private ImageView mAvatar;
    private SeekBar mFontSize, mLSExtra, mLSMul, mTitleFontSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_post_display_setting);
        } else {
            setContentView(R.layout.activity_post_display_setting_landscape);
        }

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
        setTitle("显示设置");

        // UI
        fillUI();
    }

    private void fillUI() {
        mMessageView = (TextView) findViewById(R.id.thread_message);
        TextView mAuthor = (TextView) findViewById(R.id.thread_author_name);
        TextView mPostDateView = (TextView) findViewById(R.id.post_date);
        mSubject = (TextView) findViewById(R.id.thread_subject);
        TextView mDevice = (TextView) findViewById(R.id.device_name);
        mAvatar = (ImageView) findViewById(R.id.thread_author_avatar);

        CommonUtils.getAndCacheUserInfo(this, BUApplication.username,
                new CommonUtils.UserInfoAndFillAvatarCallback() {
                    @Override
                    public void doSomethingIfHasCached(Member member) {
                        String avatarURL = CommonUtils.getRealImageURL(member.avatar);
                        CommonUtils.setAvatarImageView(PostDisplaySettingActivity.this, mAvatar,
                                avatarURL, R.drawable.default_avatar);
                    }
                });
        mAuthor.setText(CommonUtils.decode(BUApplication.username));
        mPostDateView.setText("Recently");
        mDevice.setText(CommonUtils.getDeviceName());

        // SeekBar
        mTitleFontSize = (SeekBar) findViewById(R.id.sb_title_font_size);
        mFontSize = (SeekBar) findViewById(R.id.sb_font_size);
        mLSExtra = (SeekBar) findViewById(R.id.sb_line_spacing_extra);
        mLSMul = (SeekBar) findViewById(R.id.sb_line_spacing_mul);

        mTitleFontSize.setProgress(BUApplication.getCacheTitleFontSize(this) - 15);
        mFontSize.setProgress(BUApplication.getCacheFontSize(this) - 12);
        mLSExtra.setProgress(BUApplication.getCacheLineSpacingExtra(this));
        mLSMul.setProgress((int) (BUApplication.getCacheLineSpacingMultiplier(this) * 10) - 10);

        mSubject.setTextSize(TypedValue.COMPLEX_UNIT_SP, BUApplication.titleFontSize);
        mMessageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, BUApplication.fontSize);
        mMessageView.setLineSpacing(BUApplication.lineSpacingExtra, BUApplication.lineSpacingMultiplier);

        mTitleFontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int size = 15 + progress;
                BUApplication.titleFontSize = size;
                BUApplication.setCacheTitleFontSize(PostDisplaySettingActivity.this);
                mSubject.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mFontSize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int size = 12 + progress;
                BUApplication.fontSize = size;
                BUApplication.setCacheFontSize(PostDisplaySettingActivity.this);
                mMessageView.setTextSize(TypedValue.COMPLEX_UNIT_SP, size);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mLSExtra.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float lineSpacingMultiplier = (mLSMul.getProgress() + 10) * 1.0f / 10;
                BUApplication.lineSpacingExtra = progress;
                BUApplication.setCacheLineSpacingExtra(PostDisplaySettingActivity.this);
                mMessageView.setLineSpacing(progress, lineSpacingMultiplier);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mLSMul.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // 1.0 - 2.0, 10 - 20, 0 - 10
                int lineSpacingExtra = mLSExtra.getProgress();
                float lineSpacingMultiplier = (progress + 10) * 1.0f / 10;
                BUApplication.lineSpacingMultiplier = lineSpacingMultiplier;
                BUApplication.setCacheLineSpacingMultiplier(PostDisplaySettingActivity.this);
                mMessageView.setLineSpacing(lineSpacingExtra, lineSpacingMultiplier);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        Button resetBtn = (Button) findViewById(R.id.reset);
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(PostDisplaySettingActivity.this, R.style.AlertDialogCustom));
                builder.setTitle(getString(R.string.title_warning)).setMessage(getString(R.string.warning_reset_display_setting))
                        .setPositiveButton(getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setNegativeButton(getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        BUApplication.titleFontSize = BUApplication.DEFAULT_TITLE_FONT_SIZE;
                        BUApplication.fontSize = BUApplication.DEFAULT_FONT_SIZE;
                        BUApplication.lineSpacingExtra = BUApplication.DEFAULT_LINE_SPACING_EXTRA;
                        BUApplication.lineSpacingMultiplier = BUApplication.DEFAULT_LINE_SPACING_MULTIPLIER;
                        BUApplication.setCacheTitleFontSize(PostDisplaySettingActivity.this);
                        BUApplication.setCacheFontSize(PostDisplaySettingActivity.this);
                        BUApplication.setCacheLineSpacingExtra(PostDisplaySettingActivity.this);
                        BUApplication.setCacheLineSpacingMultiplier(PostDisplaySettingActivity.this);
                        mTitleFontSize.setProgress(BUApplication.getCacheTitleFontSize(PostDisplaySettingActivity.this) - 15);
                        mFontSize.setProgress(BUApplication.getCacheFontSize(PostDisplaySettingActivity.this) - 12);
                        mLSExtra.setProgress(BUApplication.getCacheLineSpacingExtra(PostDisplaySettingActivity.this));
                        mLSMul.setProgress((int) (BUApplication.getCacheLineSpacingMultiplier(PostDisplaySettingActivity.this) * 10) - 10);
                    }
                }).show();
            }
        });
    }
}

package me.ihainan.bu.app.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lb.material_preferences_library.PreferenceActivity;

import java.util.Arrays;

import me.ihainan.bu.app.BuildConfig;
import me.ihainan.bu.app.R;
import me.ihainan.bu.app.utils.BUApplication;
import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.network.BUApi;

public class SettingsActivity extends PreferenceActivity {
    public final static String TAG = SettingsActivity.class.getSimpleName();

    @Override
    protected int getPreferencesXmlId() {
        return R.xml.pref_all;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preference);

        swipeLayout = new SwipeLayout(this);

        // Toolbar
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setTitle(R.string.action_settings);

        // Load
        loadDefaultValue();
    }

    private SwitchPreference prefEnableSilentMode;
    private Preference prefHomePageClick;
    private Preference prefEnableNotifyType;

    private void loadDefaultValue() {
        BUApplication.readConfig(this);

        /* 网络相关 */
        SwitchPreference prefNetworkType = (SwitchPreference) findPreference("pref_out_school");
        prefNetworkType.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean checked = (Boolean) newValue;
                BUApplication.networkType = checked ? BUApplication.NETWORK_TYPE.OUT_SCHOOL : BUApplication.NETWORK_TYPE.IN_SCHOOL;
                BUApi.currentEndPoint = checked ? BUApi.OUT_SCHOOL_ENDPOINT : BUApi.IN_SCHOOL_ENDPOINT;
                BUApplication.setCacheNetworkType(SettingsActivity.this);
                return true;
            }
        });
        prefNetworkType.setChecked(BUApplication.networkType != BUApplication.NETWORK_TYPE.IN_SCHOOL);

        SwitchPreference prefSaveDataMode = (SwitchPreference) findPreference("pref_save_data");
        prefSaveDataMode.setChecked(BUApplication.saveDataMode);
        prefSaveDataMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                BUApplication.saveDataMode = (Boolean) newValue;
                BUApplication.setCacheSaveDataMode(SettingsActivity.this);
                return true;
            }
        });

        /* 开发相关 */
        SwitchPreference prefDebugMode = (SwitchPreference) findPreference("pref_enable_dev_mode");
        prefDebugMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                BUApplication.debugMode = (Boolean) newValue;
                BUApplication.setCacheDebugMode(SettingsActivity.this);
                return true;
            }
        });

        prefDebugMode.setChecked(BUApplication.debugMode);

        SwitchPreference prefUploadData = (SwitchPreference) findPreference("pref_upload_data");
        prefUploadData.setChecked(BUApplication.uploadData);
        prefUploadData.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                BUApplication.uploadData = (Boolean) newValue;
                BUApplication.setUploadData(SettingsActivity.this);
                return true;
            }
        });

        /* 关于相关 */
        Preference prefFeedback = findPreference("pref_feedback");
        prefFeedback.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                /*
                Intent feedbackIntent = new Intent(Intent.ACTION_SEND);
                feedbackIntent.setType("message/rfc822");
                feedbackIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"ihainan72@gmail.com"});
                feedbackIntent.putExtra(Intent.EXTRA_SUBJECT, "联盟安卓客户端意见反馈");
                feedbackIntent.putExtra(Intent.EXTRA_TEXT, "\n---\n当前版本：" + BuildConfig.VERSION_NAME);
                startActivity(Intent.createChooser(feedbackIntent, "发送邮件...")); */
                Intent intent = new Intent(SettingsActivity.this, FeedbackActivity.class);
                startActivity(intent);
                return false;
            }
        });

        Preference prefDeviceName = findPreference("pref_device_name");
        prefDeviceName.setSummary(CommonUtils.getDeviceName());

        Preference prefCheckUpdate = findPreference("pref_check_update");
        prefCheckUpdate.setSummary("当前版本：" + BuildConfig.VERSION_NAME);
        prefCheckUpdate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ProgressDialog dialog = ProgressDialog.show(SettingsActivity.this, "",
                        "正在检查更新", true);
                dialog.show();
                CommonUtils.updateVersion(SettingsActivity.this, false, dialog);
                return false;
            }
        });

        Preference prefDonate = findPreference("pref_donate");

        prefDonate.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                LinearLayout layout = new LinearLayout(SettingsActivity.this);
                int dpLeftAndRightValue = (int) CommonUtils.convertDpToPixel(24, SettingsActivity.this);
                int dpTopAndBottomValue = (int) CommonUtils.convertDpToPixel(17, SettingsActivity.this);
                layout.setPadding(dpLeftAndRightValue, dpTopAndBottomValue, dpLeftAndRightValue, dpTopAndBottomValue);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setLayoutParams(layoutParams);
                TextView tvDonateMessage = new TextView(SettingsActivity.this);
                tvDonateMessage.setLineSpacing(7, 1.3f);
                // tvDonateMessage.setPadding(60, 40, 60, 40);
                tvDonateMessage.setTextSize(16);
                layout.addView(tvDonateMessage);
                tvDonateMessage.setText(getString(R.string.message_donate));
                new AlertDialog.Builder(SettingsActivity.this).setTitle("捐赠")
                        .setView(layout)
                        .setPositiveButton("复制支付宝用户名", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ClipboardManager clipboardManager = (ClipboardManager) SettingsActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clipData = ClipData.newPlainText("alipay", CommonUtils.decode("ihainan72@163.com"));
                                clipboardManager.setPrimaryClip(clipData);
                                Toast.makeText(SettingsActivity.this, "复制成功", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
                return true;
            }
        });

        /* 通知相关 */
        SwitchPreference prefEnableNotify = (SwitchPreference) findPreference("pref_enable_notify");
        prefEnableSilentMode = (SwitchPreference) findPreference("pref_night_silent_mode");
        prefEnableNotifyType = findPreference("pref_enable_notify_type");

        prefEnableNotify.setChecked(BUApplication.enableNotify);

        setupNotifySettings(BUApplication.enableNotify);
        prefEnableNotify.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Boolean checked = (Boolean) newValue;
                BUApplication.enableNotify = checked;
                BUApplication.setEnableNotify(SettingsActivity.this);
                setupNotifySettings(checked);
                return true;
            }
        });

        prefEnableSilentMode.setChecked(BUApplication.enableSilentMode);
        prefEnableSilentMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                BUApplication.enableSilentMode = (Boolean) newValue;
                BUApplication.setEnableSilentMode(SettingsActivity.this);
                return true;
            }
        });

        final String[] notifyTypes = new String[]{"回复通知", "引用通知", "@ 通知", "关注通知"};
        final boolean[] notifiesIsEnable = new boolean[]{BUApplication.enableReplyNotify.booleanValue(), BUApplication.enableQuoteNotify.booleanValue(),
                BUApplication.enableAtNotify.booleanValue(), BUApplication.enableFollowingNotify.booleanValue()};
        setupPrefEnableNotifyTypeSummary(notifyTypes, notifiesIsEnable);

        prefEnableNotifyType.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle("允许通知类型")
                        .setMultiChoiceItems(notifyTypes, notifiesIsEnable, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int indexSelected, boolean checked) {
                                setupPrefEnableNotifyTypeSummary(notifyTypes, notifiesIsEnable);
                                if (indexSelected == 0) {
                                    BUApplication.enableReplyNotify = checked;
                                    BUApplication.setEnableReplyNotify(SettingsActivity.this);
                                } else if (indexSelected == 1) {
                                    BUApplication.enableQuoteNotify = checked;
                                    BUApplication.setEnableQuoteNotify(SettingsActivity.this);
                                } else if (indexSelected == 2) {
                                    BUApplication.enableAtNotify = checked;
                                    BUApplication.setEnableAtNotify(SettingsActivity.this);
                                } else if (indexSelected == 3) {
                                    BUApplication.enableFollowingNotify = checked;
                                    BUApplication.setEnableFollowNotify(SettingsActivity.this);
                                }
                            }
                        }).create().show();

                return true;
            }
        });

        /* 界面相关 */
        Preference prefDisplaySetting = findPreference("pref_post_display");
        prefDisplaySetting.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(SettingsActivity.this, PostDisplaySettingActivity.class);
                startActivity(intent);
                return false;
            }
        });

        final String[] POST_COUNT_LIST = new String[]{"5", "10", "15", "20"};
        final Preference prefPostCount = findPreference("pref_post_count");
        prefPostCount.setSummary("" + BUApplication.postListLoadingCount);
        prefPostCount.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this).setTitle("每页显示帖子数")
                        .setSingleChoiceItems(POST_COUNT_LIST, Arrays.asList(POST_COUNT_LIST).indexOf("" + BUApplication.postListLoadingCount),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        BUApplication.postListLoadingCount = Integer.valueOf(POST_COUNT_LIST[which]);
                                        BUApplication.setPostListLoadingCount(SettingsActivity.this);
                                        prefPostCount.setSummary(POST_COUNT_LIST[which]);
                                        dialog.dismiss();
                                    }
                                });
                builder.create().show();
                return true;
            }
        });

        final String[] actions = new String[]{"查看回帖楼层", "查看主楼"};
        prefHomePageClick = findPreference("pref_home_page_click");
        prefHomePageClick.setSummary(actions[BUApplication.homePageClickEventType]);
        prefHomePageClick.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this).setTitle("首页帖子点击事件").setSingleChoiceItems(actions, BUApplication.homePageClickEventType, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BUApplication.homePageClickEventType = which;
                        BUApplication.setHomePageClickEventType(SettingsActivity.this);
                        prefHomePageClick.setSummary(actions[BUApplication.homePageClickEventType]);
                        dialog.dismiss();
                    }
                });
                builder.create().show();
                return true;
            }
        });

        /* 回帖相关 */
        SwitchPreference prefDisplayDeviceInfo = (SwitchPreference) findPreference("pref_display_device_information");
        prefDisplayDeviceInfo.setSummary("发自 " + CommonUtils.getDeviceName() + " @BU for Android");
        prefDisplayDeviceInfo.setChecked(BUApplication.enableDisplayDeviceInfo);
        prefDisplayDeviceInfo.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                BUApplication.enableDisplayDeviceInfo = (boolean) (Boolean) newValue;
                BUApplication.setPrefEnableDisplayDeviceInfo(SettingsActivity.this);
                return true;
            }
        });

        SwitchPreference prefAdvancedEditor = (SwitchPreference) findPreference("pref_advanced_editor");
        prefAdvancedEditor.setChecked(BUApplication.enableAdvancedEditor);
        prefAdvancedEditor.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                BUApplication.enableAdvancedEditor = (Boolean) newValue;
                BUApplication.setEnableAdvancedEditor(SettingsActivity.this);
                return true;
            }
        });
    }

    private void setupNotifySettings(boolean checked) {
        prefEnableNotifyType.setEnabled(checked);
        prefEnableSilentMode.setEnabled(checked);
    }

    private void setupPrefEnableNotifyTypeSummary(final String[] notifyTypes, final boolean[] notifiesIsEnable) {
        String summary = "";
        for (int i = 0; i < notifiesIsEnable.length; ++i) {
            Boolean notifyEnable = notifiesIsEnable[i];
            if (notifyEnable) summary = summary + notifyTypes[i] + " / ";
        }

        if (summary.equals("")) summary = "禁用所有通知";
        else summary = summary.substring(0, summary.length() - 3);

        prefEnableNotifyType.setSummary(summary);
    }


    private SwipeLayout swipeLayout;

    /**
     * 是否可以滑动关闭页面
     */
    private boolean swipeEnabled = true;

    /**
     * 是否可以在页面任意位置右滑关闭页面，如果是false则从左边滑才可以关闭。
     */
    private boolean swipeAnyWhere = false;

    public void setSwipeAnyWhere(boolean swipeAnyWhere) {
        this.swipeAnyWhere = swipeAnyWhere;
    }

    public boolean isSwipeAnyWhere() {
        return swipeAnyWhere;
    }

    public void setSwipeEnabled(boolean swipeEnabled) {
        this.swipeEnabled = swipeEnabled;
    }

    public boolean isSwipeEnabled() {
        return swipeEnabled;
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        swipeLayout.replaceLayer(this);
    }

    private static int getScreenWidth(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    private boolean swipeFinished = false;

    @Override
    public void finish() {
        if (swipeFinished) {
            super.finish();
            overridePendingTransition(0, 0);
        } else {
            swipeLayout.cancelPotentialAnimation();
            super.finish();
            overridePendingTransition(0, R.anim.slide_out_right);
        }
    }

    class SwipeLayout extends FrameLayout {

        // private View backgroundLayer;用来设置滑动时的背景色
        private Drawable leftShadow;

        public SwipeLayout(Context context) {
            super(context);
        }

        public SwipeLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public SwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        public void replaceLayer(Activity activity) {
            leftShadow = activity.getResources().getDrawable(R.drawable.left_shadow);
            touchSlop = (int) (touchSlopDP * activity.getResources().getDisplayMetrics().density);
            sideWidth = (int) (sideWidthInDP * activity.getResources().getDisplayMetrics().density);
            mActivity = activity;
            screenWidth = getScreenWidth(activity);
            setClickable(true);
            final ViewGroup root = (ViewGroup) activity.getWindow().getDecorView();
            content = root.getChildAt(0);
            ViewGroup.LayoutParams params = content.getLayoutParams();
            ViewGroup.LayoutParams params2 = new ViewGroup.LayoutParams(-1, -1);
            root.removeView(content);
            this.addView(content, params2);
            root.addView(this, params);
        }

        @Override
        protected boolean drawChild(@NonNull Canvas canvas, @NonNull View child, long drawingTime) {
            boolean result = super.drawChild(canvas, child, drawingTime);
            final int shadowWidth = leftShadow.getIntrinsicWidth();
            int left = (int) (getContentX()) - shadowWidth;
            leftShadow.setBounds(left, child.getTop(), left + shadowWidth, child.getBottom());
            leftShadow.draw(canvas);
            return result;
        }

        boolean canSwipe = false;
        /**
         * 超过了touchslop仍然没有达到没有条件，则忽略以后的动作
         */
        boolean ignoreSwipe = false;
        View content;
        Activity mActivity;
        final int sideWidthInDP = 16;
        int sideWidth = 72;
        int screenWidth = 1080;
        VelocityTracker tracker;

        float downX;
        float downY;
        float lastX;
        float currentX;
        float currentY;

        final int touchSlopDP = 20;
        int touchSlop = 60;

        @Override
        public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
            if (swipeEnabled && !canSwipe && !ignoreSwipe) {
                if (swipeAnyWhere) {
                    switch (ev.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            downX = ev.getX();
                            downY = ev.getY();
                            currentX = downX;
                            currentY = downY;
                            lastX = downX;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            float dx = ev.getX() - downX;
                            float dy = ev.getY() - downY;
                            if (dx * dx + dy * dy > touchSlop * touchSlop) {
                                if (dy == 0f || Math.abs(dx / dy) > 1) {
                                    downX = ev.getX();
                                    downY = ev.getY();
                                    currentX = downX;
                                    currentY = downY;
                                    lastX = downX;
                                    canSwipe = true;
                                    tracker = VelocityTracker.obtain();
                                    return true;
                                } else {
                                    ignoreSwipe = true;
                                }
                            }
                            break;
                    }
                } else if (ev.getAction() == MotionEvent.ACTION_DOWN && ev.getX() < sideWidth) {
                    canSwipe = true;
                    tracker = VelocityTracker.obtain();
                    return true;
                }
            }
            if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
                ignoreSwipe = false;
            }
            return super.dispatchTouchEvent(ev);
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent ev) {
            return canSwipe || super.onInterceptTouchEvent(ev);
        }

        boolean hasIgnoreFirstMove;

        @Override
        public boolean onTouchEvent(@NonNull MotionEvent event) {
            if (canSwipe) {
                tracker.addMovement(event);
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        downX = event.getX();
                        downY = event.getY();
                        currentX = downX;
                        currentY = downY;
                        lastX = downX;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        currentX = event.getX();
                        currentY = event.getY();
                        float dx = currentX - lastX;
                        if (dx != 0f && !hasIgnoreFirstMove) {
                            hasIgnoreFirstMove = true;
                            dx = dx / dx;
                        }
                        if (getContentX() + dx < 0) {
                            setContentX(0);
                        } else {
                            setContentX(getContentX() + dx);
                        }
                        lastX = currentX;
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        tracker.computeCurrentVelocity(10000);
                        tracker.computeCurrentVelocity(1000, 20000);
                        canSwipe = false;
                        hasIgnoreFirstMove = false;
                        int mv = screenWidth * 3;
                        if (Math.abs(tracker.getXVelocity()) > mv) {
                            animateFromVelocity(tracker.getXVelocity());
                        } else {
                            if (getContentX() > screenWidth / 3) {
                                animateFinish(false);
                            } else {
                                animateBack(false);
                            }
                        }
                        tracker.recycle();
                        break;
                    default:
                        break;
                }
            }
            return super.onTouchEvent(event);
        }

        ObjectAnimator animator;

        public void cancelPotentialAnimation() {
            if (animator != null) {
                animator.removeAllListeners();
                animator.cancel();
            }
        }

        public void setContentX(float x) {
            int ix = (int) x;
            content.setX(ix);
            invalidate();
        }

        public float getContentX() {
            return content.getX();
        }


        /**
         * 弹回，不关闭，因为left是0，所以setX和setTranslationX效果是一样的
         *
         * @param withVel 使用计算出来的时间
         */
        private void animateBack(boolean withVel) {
            cancelPotentialAnimation();
            animator = ObjectAnimator.ofFloat(this, "contentX", getContentX(), 0);
            int tmpDuration = withVel ? ((int) (duration * getContentX() / screenWidth)) : duration;
            if (tmpDuration < 100) {
                tmpDuration = 100;
            }
            animator.setDuration(tmpDuration);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.start();
        }

        private void animateFinish(boolean withVel) {
            cancelPotentialAnimation();
            animator = ObjectAnimator.ofFloat(this, "contentX", getContentX(), screenWidth);
            int tmpDuration = withVel ? ((int) (duration * (screenWidth - getContentX()) / screenWidth)) : duration;
            if (tmpDuration < 100) {
                tmpDuration = 100;
            }
            animator.setDuration(tmpDuration);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addListener(new Animator.AnimatorListener() {

                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!mActivity.isFinishing()) {
                        swipeFinished = true;
                        mActivity.finish();
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }
            });
            animator.start();
        }

        private final int duration = 200;

        private void animateFromVelocity(float v) {
            if (v > 0) {
                if (getContentX() < screenWidth / 3 && v * duration / 1000 + getContentX() < screenWidth / 3) {
                    animateBack(false);
                } else {
                    animateFinish(true);
                }
            } else {
                if (getContentX() > screenWidth / 3 && v * duration / 1000 + getContentX() > screenWidth / 3) {
                    animateFinish(false);
                } else {
                    animateBack(true);
                }
            }

        }
    }
}

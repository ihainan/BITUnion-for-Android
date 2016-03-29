package me.ihainan.bu.app.ui.assist;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.LeadingMarginSpan;
import android.text.style.LineBackgroundSpan;
import android.text.style.QuoteSpan;
import android.text.style.URLSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.ui.FullscreenPhotoViewerActivity;
import me.ihainan.bu.app.ui.PostListActivity;
import me.ihainan.bu.app.ui.ProfileActivity;
import me.ihainan.bu.app.ui.ThreadListActivity;
import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.Global;
import me.ihainan.bu.app.utils.network.BUApi;

/**
 * Custom Span
 */
public class CustomSpan {
    public final static String TAG = CustomSpan.class.getSimpleName();

    public static class CustomQuoteSpan implements LeadingMarginSpan, LineBackgroundSpan {
        private final int backgroundColor;
        private final int stripeColor;
        private final float stripeWidth;
        private final float gap;

        public CustomQuoteSpan(int backgroundColor, int stripeColor, float stripeWidth, float gap) {
            this.backgroundColor = backgroundColor;
            this.stripeColor = stripeColor;
            this.stripeWidth = stripeWidth;
            this.gap = gap;
        }

        @Override
        public int getLeadingMargin(boolean first) {
            return (int) (stripeWidth + gap);
        }

        @Override
        public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline, int bottom, CharSequence text, int start, int end, boolean first, Layout layout) {
            Paint.Style style = p.getStyle();
            int color = p.getColor();

            p.setStyle(Paint.Style.FILL);
            p.setColor(stripeColor);

            c.drawRect(x, top, x + dir * stripeWidth, bottom, p);

            p.setStyle(style);
            p.setColor(color);
        }

        @Override
        public void drawBackground(Canvas c, Paint p, int left, int right, int top, int baseline, int bottom, CharSequence text, int start, int end, int lnum) {
            Paint.Style style = p.getStyle();
            int color = p.getColor();

            p.setStyle(Paint.Style.FILL);
            p.setColor((backgroundColor));

            c.drawRect(left, top, right, bottom, p);

            p.setStyle(style);
            p.setColor(color);
        }
    }

    public static class CustomLinkSpan extends ClickableSpan {
        private final int mNormalTextColor;
        private final int mPressedTextColor;
        private final int mBackgroundColor;
        private final String mUrl;
        private final Context mContext;

        public CustomLinkSpan(Context context, int normalTextColor, int pressedTextColor, int backgroundColor, String url) {
            super();
            mContext = context;
            mNormalTextColor = normalTextColor;
            mPressedTextColor = pressedTextColor;
            mUrl = url;
            mBackgroundColor = backgroundColor;
        }

        private boolean mIsPressed;

        public void setPressed(boolean isSelected) {
            mIsPressed = isSelected;
        }

        @Override
        public void onClick(View widget) {
            if (mUrl == null) return;
            else if (mUrl.startsWith(Global.IMAGE_URL_PREFIX)) {
                // 图片
                String newUrl = mUrl.substring(Global.IMAGE_URL_PREFIX.length());
                Intent intent = new Intent(mContext, FullscreenPhotoViewerActivity.class);
                intent.putExtra(FullscreenPhotoViewerActivity.IMAGE_URL_TAG, newUrl);
                mContext.startActivity(intent);
                return;
            } else if ((mUrl.startsWith(BUApi.IN_SCHOOL_BASE_URL)
                    || mUrl.startsWith(BUApi.OUT_SCHOOL_BASE_URL))) {
                String newUrl = mUrl.replace(BUApi.IN_SCHOOL_BASE_URL, "/").replace(BUApi.OUT_SCHOOL_BASE_URL, "/");
                if (newUrl.startsWith("/profile-username-")) {
                    // 个人信息
                    String userName = CommonUtils.decode(newUrl.substring("/profile-username-".length(), mUrl.length() - 5), "GBK");
                    Intent intent = new Intent(mContext, ProfileActivity.class);
                    intent.putExtra(ProfileActivity.USER_NAME_TAG, userName);
                    mContext.startActivity(intent);
                    return;
                } else if (newUrl.startsWith("/thread-")) {
                    // 论坛帖子
                    Pattern pattern = Pattern.compile("thread-(.*?)-");
                    Matcher matcher = pattern.matcher(newUrl);
                    Long tid = null;
                    while (matcher.find()) {
                        tid = Long.valueOf(matcher.group(1));
                    }

                    if (tid != null) {
                        Intent intent = new Intent(mContext, PostListActivity.class);
                        intent.putExtra(PostListActivity.THREAD_ID_TAG, tid);
                        intent.putExtra(PostListActivity.THREAD_JUMP_FLOOR, 0);
                        mContext.startActivity(intent);
                        return;
                    }
                } else if (newUrl.startsWith("/forum-")) {
                    // 论坛组
                    Pattern pattern = Pattern.compile("forum-(.*?)-");
                    Matcher matcher = pattern.matcher(newUrl);
                    Long fid = null;
                    while (matcher.find()) {
                        fid = Long.valueOf(matcher.group(1));
                    }

                    if (fid != null) {
                        Intent intent = new Intent(mContext, ThreadListActivity.class);
                        intent.putExtra(ThreadListActivity.ACTION_TAG, "THREAD_LIST");
                        intent.putExtra(ThreadListActivity.FORUM_FID_TAG, fid);
                        mContext.startActivity(intent);
                        return;
                    }
                }
            }

            CommonUtils.openBrowser(mContext, mUrl);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setColor(mIsPressed ? mPressedTextColor : mNormalTextColor);
            ds.setUnderlineText(true);
            ds.bgColor = mBackgroundColor;
            ds.setTypeface(Typeface.create(ds.getTypeface(), Typeface.BOLD));
        }
    }

    public static class LinkTouchMovementMethod extends LinkMovementMethod {
        private CustomLinkSpan mPressedSpan;

        @Override
        public boolean onTouchEvent(TextView textView, Spannable spannable, MotionEvent event) {
            // 点击连接，选中
            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                mPressedSpan = getPressedSpan(textView, spannable, event);

                if (mPressedSpan != null) {
                    mPressedSpan.setPressed(true);
                }
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                // 移动到其他位置
                CustomLinkSpan customLinkSpan = getPressedSpan(textView, spannable, event);
                if (mPressedSpan != null && customLinkSpan != mPressedSpan) {
                    mPressedSpan.setPressed(false);
                    mPressedSpan = null;
                }
            } else {
                // 离开
                if (mPressedSpan != null) {
                    mPressedSpan.setPressed(false);
                    super.onTouchEvent(textView, spannable, event);
                }
                mPressedSpan = null;
            }

            return true;
            // return super.onTouchEvent(textView, spannable, event);
        }

        private CustomLinkSpan getPressedSpan(TextView textView, Spannable spannable, MotionEvent event) {
            int x = (int) event.getX();
            int y = (int) event.getY();

            x -= textView.getTotalPaddingLeft();
            y -= textView.getTotalPaddingTop();

            x += textView.getScrollX();
            y += textView.getScrollY();

            Layout layout = textView.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, x);

            CustomLinkSpan[] link = spannable.getSpans(off, off, CustomLinkSpan.class);
            CustomLinkSpan customLinkSpan = null;
            if (link.length > 0) customLinkSpan = link[0];
            return customLinkSpan;
        }
    }

    public static void replaceClickableSpan(Context context, Spannable spannable) {
        URLSpan[] urlSpans = spannable.getSpans(0, spannable.length(), URLSpan.class);
        // ClickableSpan[] clickableSpans = spannable.getSpans(0, spannable.length(), ClickableSpan.class);
        for (final URLSpan clickableSpan : urlSpans) {
            int start = spannable.getSpanStart(clickableSpan);
            int end = spannable.getSpanEnd(clickableSpan);
            int flags = spannable.getSpanFlags(clickableSpan);
            Log.d(TAG, start + " " + end + " " + flags);
            spannable.removeSpan(clickableSpan);
            spannable.setSpan(new CustomLinkSpan(context,
                            ContextCompat.getColor(context, R.color.primary),
                            ContextCompat.getColor(context, R.color.primary_dark),
                            ContextCompat.getColor(context, R.color.link_background), clickableSpan.getURL()),
                    start, end, flags);
        }
    }

    public static void replaceQuoteSpans(Context context, Spannable spannable) {
        QuoteSpan[] quoteSpans = spannable.getSpans(0, spannable.length(), QuoteSpan.class);
        for (QuoteSpan quoteSpan : quoteSpans) {
            int start = spannable.getSpanStart(quoteSpan);
            int end = spannable.getSpanEnd(quoteSpan);
            int flags = spannable.getSpanFlags(quoteSpan);
            spannable.removeSpan(quoteSpan);
            spannable.setSpan(new CustomSpan.CustomQuoteSpan(ContextCompat.getColor(context, R.color.blockquote_background),
                            ContextCompat.getColor(context, R.color.primary),
                            10.0f, 50.0f),
                    start, end, flags);
        }
    }

    public static void setUpAllSpans(Context context, Spannable spannable) {
        replaceQuoteSpans(context, spannable);
        replaceClickableSpan(context, spannable);
    }
}

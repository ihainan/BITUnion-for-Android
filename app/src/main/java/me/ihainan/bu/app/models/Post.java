package me.ihainan.bu.app.models;

import android.text.Html;
import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.network.BUApi;
import me.ihainan.bu.app.utils.ui.HtmlUtil;

/**
 * 回帖模型
 */
public class Post implements Serializable {
    private final static String TAG = Post.class.getSimpleName();

    // Extra Fields only used on ExtraAPI
    public long post_id;
    public int floor;
    public String dt_created;
    public String t_subject;    // 主题标题

    // Original Fields
    public long pid;
    public long fid;
    public long tid;
    public long aid;
    public String icon;
    public String author;   // 和 username 区别在于
    public long authorid;   // 和 uid 的区别在于？
    public String subject;
    public long dateline;
    public String originalMessage;
    public String message;
    public long usesig;
    public long bbcodeoff;
    public long smileyoff;
    public long parseurloff;
    public long score;
    public long rate;
    public long ratetimes;
    public long pstatus;
    public String lastedit; // 最后一次编辑时间
    public String postsource;
    public String aaid;
    public String creditsrequire;
    public String filetype;
    public String filename;
    public String attachment;
    public Integer filesize;
    public String downloads;    // 附件下载次数
    public long uid;
    public String username;
    public String avatar;
    public String epid;
    public long maskpost;
    public String attachext;
    public String attachsize;
    public String attachimg;
    public String exif; // EXIF 信息
    public String deviceName;
    public int level;

    @Override
    public String toString() {
        try {
            return BUApi.MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            Log.e(TAG, "Failed to convert object to JSON string", e);
            return null;
        }
    }

    // See: https://github.com/wuhao-ouyang/BUApp/blob/master/src/martin/app/bitunion/model/BUPost.java
    public String toQuote() {
        String quote = message;

        // 表情
        quote = HtmlUtil.replaceQuoteSmiles(quote);

        // 引用
        quote = quote.replaceAll("<blockquote>.*?</blockquote>", "[引用]\n");

        // HTML to UBB
        Pattern p = Pattern.compile("<a href='(.+?)'(?:.target='.+?')>(.+?)</a>");
        Matcher m = p.matcher(quote);
        while (m.find()) {
            String discuz = "[url=" + m.group(1) + "]" + m.group(2) + "[/url]";
            quote = quote.replace(m.group(0), discuz);
            m = p.matcher(quote);
        }

        // 图片
        p = Pattern.compile("<img src='([^>])'>");
        m = p.matcher(quote);
        while (m.find()) {
            quote = quote.replace(m.group(0), "[img]" + m.group(1) + "[/img]");
            m = p.matcher(quote);
        }

        // Cut down the message if it's too long
        String regex = "[.*?].*?[.*?]";
        p = Pattern.compile(regex);
        m = p.matcher(quote);
        boolean hasFound = false;
        while (m.find()) {
            if (m.start() < 250 && m.end() >= 250) {
                hasFound = true;
                quote = quote.substring(0, m.end() + 1) + "......";
                break;
            }
        }

        if (!hasFound && quote.length() > 250) {
            quote = quote.substring(0, 250) + "......";
        }

        // 其他标签
        quote = Html.fromHtml(quote).toString();
        quote = "[quote=" + pid + "][b]" + CommonUtils.decode(author) + "[/b] "
                + CommonUtils.formatDateTime(CommonUtils.unixTimeStampToDate(dateline)) + "\n" + quote + "[/quote]\n";

        return quote;
    }

    public boolean useMobile;
}

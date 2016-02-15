package bit.ihainan.me.bitunionforandroid.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 通用工具类
 */
public class CommonUtils {
    private final static String TAG = CommonUtils.class.getSimpleName();

    /**
     * 显示一个弹窗（dialog）
     *
     * @param context 上下文
     * @param title   弹窗标题
     * @param message 弹窗信息
     */
    public static void showDialog(Context context, String title, String message) {
        (new AlertDialog.Builder(context))
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    /**
     * 获取图片的真实 URL
     *
     * @param originalURL 原始的 URL
     * @return 图片的真实 URL
     */
    public static String getRealImageURL(String originalURL) {
        String ori = originalURL;

        originalURL = originalURL.replaceAll("^images/", Global.getBaseURL() + "images/");

        // 回帖头像
        if (ori.startsWith("<embed src=") || ori.startsWith("<img src=")) {
            originalURL = originalURL.split("\"")[1];
        }

        // 空地址
        if (originalURL == null || originalURL.equals("")) {
            return "http:/out.bitunion.org/images/standard/noavatar.gif";
        }

        // 完整地址和不完整地址
        if (originalURL.startsWith("http"))
            originalURL = Global.isInSchool() ? originalURL : originalURL.replace("www", "out");
        else originalURL = Global.getBaseURL() + originalURL;

        originalURL = originalURL.replaceAll("(http://)?(www|v6|kiss|out).bitunion.org/", Global.getBaseURL());
        // originalURL = originalURL.replace("//", "/");
        originalURL = originalURL.replaceAll("http://bitunion.org/", Global.getBaseURL());

        // 图片
        originalURL = originalURL.replaceAll("^images/", Global.getBaseURL() + "images/");

        // 特殊情况
        if (originalURL.endsWith(",120,120")) originalURL = originalURL.replace(",120,120", "");

        if (originalURL.contains("aid="))
            originalURL = Global.getBaseURL() + "images/standard/noavatar.gif";
        Log.d(TAG, "getRealImageURL >> " + ori + " - " + originalURL);
        return originalURL;
    }

    /**
     * 将字符转换成 utf-8 编码
     *
     * @param originalStr 原始字符串
     * @return 转换后得到的 utf-8 编码字符串
     */
    public static String decode(String originalStr) {
        try {
            return URLDecoder.decode(originalStr, "utf-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Unsupported Encoding", e);
            return originalStr;
        }
    }

    /**
     * 格式化日期为标准格式（yyyy-MM-dd hh:mm"）
     *
     * @param date 需要格式化的日期
     * @return 格式化后的日期字符串
     */
    public static String formatDateTime(Date date) {
        return (new SimpleDateFormat("yyyy-MM-dd hh:mm")).format(date);
    }

    /**
     * 格式化日期为标准格式（yyyy-MM-dd"）
     *
     * @param date 需要格式化的日期
     * @return 格式化后的日期字符串
     */
    public static String formatDateTimeToDay(Date date) {
        return (new SimpleDateFormat("yyyy-MM-dd")).format(date);
    }

    /**
     * 判断一个帖子是否是热门帖
     *
     * @param postDate      帖子发表时间
     * @param lastReplyDate 帖子最后回复时间
     * @param replies       总回帖数
     * @return 是否是热门帖子
     */
    public boolean isHotThread(Date postDate, Date lastReplyDate, int replies) {
        int days = (int) (lastReplyDate.getTime() - postDate.getTime()) / (1000 * 60 * 60 * 24);
        return (replies / days >= 5);
    }

    /**
     * 判断一个帖子是否是热门帖
     *
     * @param postDateStr      帖子发表时间
     * @param lastReplyDateStr 帖子最后回复时间
     * @param replies          总回帖数
     * @return 是否是热门帖子
     */
    public boolean isHotThread(String postDateStr, String lastReplyDateStr, int replies) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        try {
            Date postDate = format.parse(postDateStr);
            Date lastReplyDate = format.parse(lastReplyDateStr);
            return isHotThread(postDate, lastReplyDate, replies);
        } catch (ParseException e) {
            Log.e(TAG, "错误的日期字符串", e);
            return false;
        }
    }

    /**
     * Unix 时间戳转换为 Date 类型
     *
     * @param timeStamp Unix 时间戳
     * @return Date 类型
     */
    public static Date unixTimeStampToDate(long timeStamp) {
        return new java.util.Date(timeStamp * 1000);
    }
}

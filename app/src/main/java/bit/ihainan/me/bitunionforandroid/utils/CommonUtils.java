package bit.ihainan.me.bitunionforandroid.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.models.Member;
import bit.ihainan.me.bitunionforandroid.ui.UserInfoActivity;

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
     * 设置用户头像点击事件，自动跳转到用户的个人页面
     *
     * @param context  上下文
     * @param view     被点击的 View
     * @param userId   用户 ID。若 userName != null 则无视 userId
     * @param userName 用户名
     */
    public static void setUserAvatarClickListener(final Context context, View view, final long userId, final String userName) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, UserInfoActivity.class);
                intent.putExtra(UserInfoActivity.USER_ID_TAG, userId);
                intent.putExtra(UserInfoActivity.USER_NAME_TAG, userName);
                context.startActivity(intent);
            }
        });
    }

    public static abstract class UserInfoAndFillAvatarCallback {
        public abstract void doSomethingIfHasCached(Member member);

        // 有需要的话进行 Overwrite
        public void doSomethingIfHasNotCached(Member member) {
            doSomethingIfHasCached(member);
        }
    }

    /**
     * 获取用户信息并执行特定操作，如果用户信息已经被缓存，则直接从缓存中获取，否则从服务器拉取并进行缓存
     *
     * @param context  上下文
     * @param userName 用户名
     * @param callback 包含回调函数
     */
    public static void getAndCacheUserInfo(final Context context, final String userName,
                                           final UserInfoAndFillAvatarCallback callback) {
        // 从缓存中获取用户信息
        final Member member = (Member) Global.getCache(context)
                .getAsObject(Global.CACHE_USER_INFO + userName);

        if (member != null) {
            Log.i(TAG, "从缓存 " + Global.CACHE_USER_INFO + userName + " 中获取用户 " + userName + " 的缓存数据");

            // Do something HERE!!!
            callback.doSomethingIfHasCached(member);
        } else {
            Log.i(TAG, "准备拉取用户 " + userName + " 的缓存数据");

            // 从服务器拉取数据并写入到缓存当中
            Api.getUserInfo(context, -1,
                    userName,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            if (Api.checkStatus(response)) {
                                try {
                                    Member newMember = Api.MAPPER.readValue(
                                            response.getJSONObject("memberinfo").toString(),
                                            Member.class);

                                    // Do something HERE!!!
                                    callback.doSomethingIfHasNotCached(newMember);

                                    // 将用户信息放入到缓存当中
                                    Log.i(TAG, "拉取得到用户 " + userName + " 的数据，放入缓存 " + Global.CACHE_USER_INFO + userName + " 中：" + newMember);
                                    Global.getCache(context).put(
                                            Global.CACHE_USER_INFO + userName,
                                            newMember,
                                            Global.cacheDays * ACache.TIME_DAY);
                                } catch (Exception e) {
                                    Log.e(TAG, context.getString(R.string.error_parse_json) + "\n" + response, e);
                                }
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, context.getString(R.string.error_network), error);
                        }
                    });
        }
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

    public static boolean checkIfInSchool() {
        try {
            InetAddress in = InetAddress.getByName(Global.DNS_SERVER);
            return in.isReachable(300);
        } catch (IOException e) {
            Log.e(TAG, "Failed to check if in school", e);
            return false;
        }
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

    public static String truncateString(String str, int length) {
        if (str == null) return "";
        if (str.length() > length - 3) str = str.substring(0, length - 3) + "...";
        return str;
    }
}

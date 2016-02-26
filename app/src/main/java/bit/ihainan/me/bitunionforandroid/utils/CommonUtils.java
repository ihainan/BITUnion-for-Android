package bit.ihainan.me.bitunionforandroid.utils;

import android.app.ActivityManager;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UmengUpdateListener;
import com.umeng.update.UpdateResponse;
import com.umeng.update.UpdateStatus;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        if (isRunning(context)) builder.show();
    }

    /**
     * 更新版本
     *
     * @param context       上下文
     * @param ifCheckIgnore 是否检查 ignore 标志，false 表示无视 ignore 标识继续升级
     * @param dialog        检查升级前显示的加载对话框，可为 null
     */
    public static void updateVersion(final Context context, final boolean ifCheckIgnore, final Dialog dialog) {
        // 调用友盟接口实现自动更新
        UmengUpdateAgent.setUpdateAutoPopup(false);
        UmengUpdateAgent.setUpdateListener(new UmengUpdateListener() {
            @Override
            public void onUpdateReturned(int updateStatus, final UpdateResponse updateInfo) {
                if (dialog != null) dialog.dismiss();
                switch (updateStatus) {
                    case UpdateStatus.NoneWifi: // none wifi
                    case UpdateStatus.Yes: // has update
                        debugToast(context, "发现更新，当前状态 ifCheckIgnore = "
                                + ifCheckIgnore + "; isIgnore = "
                                + ifCheckIgnore
                                + "' isWiFi = " + isWifi(context));
                        // 如果
                        Log.d(TAG, "发现更新，当前状态 ifCheckIgnore = "
                                + ifCheckIgnore + "; isIgnore = "
                                + ifCheckIgnore
                                + "' isWiFi = " + isWifi(context));
                        if (!ifCheckIgnore || !UmengUpdateAgent.isIgnore(context, updateInfo)) {
                            CommonUtils.showUpdateDialog(context, updateInfo, dialog == null);
                        }
                        break;
                    case UpdateStatus.No: // has no update
                        debugToast(context, "没有检查到更新，当前 dialog = " + dialog);
                        if (dialog != null) {
                            showDialog(context, "提醒", "没有检查到更新");
                        }
                        break;
                    case UpdateStatus.Timeout: // time out
                        debugToast(context, "连接服务器超时; dialog = " + dialog);
                        if (dialog != null) {
                            showDialog(context, "提醒", "检查更新超时");
                        }
                        break;
                }
            }
        });

        UmengUpdateAgent.update(context);
    }

    /**
     * 输出 Debug 用途的 Toast 信息
     *
     * @param context 上下文
     * @param message 输出信息
     */
    public static void debugToast(Context context, String message) {
        if (Global.debugMode)
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 显示升级对话框
     *
     * @param context    上下文
     * @param updateInfo 升级信息
     */
    public static void showUpdateDialog(final Context context, final UpdateResponse updateInfo, boolean showIgnore) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AlertDialogCustom));
        builder.setTitle("发现新版本 version " + updateInfo.version);
        builder.setMessage(updateInfo.updateLog);

        builder.setPositiveButton("下次再说", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Global.debugMode)
                    Toast.makeText(context, "下次再说", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("现在更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (Global.debugMode)
                    Toast.makeText(context, "现在更新", Toast.LENGTH_SHORT).show();

                // 非 Wi-Fi 条件下给出提醒
                if (!isWifi(context)) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.AlertDialogCustom));
                    builder.setTitle("提醒").setMessage("正在使用移动数据流量，是否仍然更新？").setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            UmengUpdateAgent.startDownload(context, updateInfo);
                        }
                    }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface innerDialog, int which) {
                            innerDialog.dismiss();
                        }
                    }).show();
                } else {
                    // Wi-Fi 条件，直接下载
                    UmengUpdateAgent.startDownload(context, updateInfo);
                }

                dialog.dismiss();
            }
        });

        if (showIgnore) {
            builder.setNeutralButton("不再提醒", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (Global.debugMode)
                        Toast.makeText(context, "不再提醒", Toast.LENGTH_SHORT).show();
                    UmengUpdateAgent.ignoreUpdate(context, updateInfo);
                    dialog.dismiss();
                }
            });
        }

        if (isRunning(context))
            builder.create().show();
    }

    /**
     * 判断 Context 对应的 Activity 是否仍处于运行状态
     *
     * @param ctx Context 对象
     * @return 返回 True 说明仍在运行，否则已经停止
     */
    public static boolean isRunning(Context ctx) {
        ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (ctx.getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName()))
                return true;
        }

        return false;
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

    public static void setAvatarImageView(final Context context, final ImageView imageView, final String imageSrc, final int errorImageId) {
        if (Global.badImages.get(imageSrc) != null) {
            Log.d(TAG, "图片在黑名单中 " + imageSrc);
            Picasso.with(context)
                    .load(R.drawable.default_avatar)
                    .into(imageView);
        } else {
            setImageView(context, imageView, imageSrc, errorImageId);
        }
    }

    public static void setImageView(final Context context, final ImageView imageView, final String imageSrc, final int errorImageId) {
        // 测试 Offline 模式是否能够正确加载图片
        Picasso.with(context)
                .load(imageSrc)
                .placeholder(R.drawable.empty_avatar)
                .error(errorImageId)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        // 成功，那么啥也不用做
                        Log.d(TAG, "setImageView >> 图片 " + imageSrc + " 已经被缓存");
                    }

                    @Override
                    public void onError() {
                        if (!CommonUtils.isWifi(context) && Global.saveDataMode) {
                            // TODO: 以更友好的方式显示默认头像
                            // 节省流量模式，不要下载图片
                            Log.d(TAG, "setImageView >> 节省流量模式且非 Wi-Fi 环境，不下载图片 " + imageSrc);
                            Picasso.with(context)
                                    .load(R.drawable.default_avatar)
                                    .error(errorImageId)
                                    .into(imageView);
                            ;
                        } else {
                            // 非节省流量模式，下载并缓存图片
                            Log.d(TAG, "setImageView >> 非节省流量模式或者 Wi-Fi 环境，正常下载图片 " + imageSrc);
                            Picasso.with(context)
                                    .load(imageSrc)
                                    .placeholder(R.drawable.empty_avatar)
                                    .error(errorImageId)
                                    .into(imageView, new Callback() {
                                        @Override
                                        public void onSuccess() {

                                        }

                                        @Override
                                        public void onError() {
                                            // 放入黑名单
                                            Log.d(TAG, "图片加入到黑名单中 " + imageSrc);
                                            Global.badImages.put(imageSrc, true);
                                        }
                                    });
                            ;
                        }
                    }
                });
    }

    /**
     * 获取用户信息之后的 Callback 类，包含 doSomethingIfHasCached 和 doSomethingIfHasNotCached 两个成员函数
     */
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
        // TODO: ../images/cf/b.gif


        String ori = originalURL;

        originalURL = originalURL.replaceAll("^images/", Global.getBaseURL() + "images/");
        originalURL = originalURL.replaceAll("^../images", Global.getBaseURL() + "images/");

        // 回帖头像
        if (ori.startsWith("<embed src=") || ori.startsWith("<img src=")) {
            originalURL = originalURL.split("\"")[1];
        }

        // 空地址
        if (originalURL == null || originalURL.equals("")) {
            return "http:/out.bitunion.org/images/standard/noavatar.gif";
        }

        // 完整地址和不完整地址¡¡
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
     * @param encode      编码类型
     * @return 转换后得到的 utf-8 编码字符串
     */
    public static String decode(String originalStr, String encode) {
        try {
            return URLDecoder.decode(originalStr, encode);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Unsupported Encoding", e);
            return originalStr;
        }
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

    /**
     * 截断字符串
     *
     * @param str    原始字符串
     * @param length 截断字符串的最大长度
     * @return 截断后的字符串
     */
    public static String truncateString(String str, int length) {
        if (str == null) return "";
        if (str.length() > length - 3) str = str.substring(0, length - 3) + "...";
        return str;
    }

    /**
     * 获取当前设备名称
     *
     * @return 当前设备名称
     */
    public static String getDeviceName() {
        BluetoothAdapter myDevice = BluetoothAdapter.getDefaultAdapter();
        String deviceName = myDevice.getName();
        return deviceName;
    }

    private static Map<String, String> realDeviceName = new HashMap<>();

    static {
        realDeviceName.put("Sony E6683", "Sony Xperia Z5 Dual");
        // TODO: 添加其他设备信息
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) return "";


        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static boolean isWifi(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI;
    }
}

package me.ihainan.bu.app.utils;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;
import com.umeng.update.UmengUpdateAgent;
import com.umeng.update.UpdateConfig;
import com.xiaomi.channel.commonutils.logger.LoggerInterface;
import com.xiaomi.mipush.sdk.Logger;
import com.xiaomi.mipush.sdk.MiPushClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import me.ihainan.bu.app.models.ForumListGroup;
import me.ihainan.bu.app.models.Member;
import me.ihainan.bu.app.models.Session;
import me.ihainan.bu.app.utils.network.BUApi;

/**
 * 系统设置与全局变量
 */
public class BUApplication extends Application {
    /* 会话相关 */
    public static Session userSession = null;   // 用户会话实例，向服务器发送请求时候需要附上会话字符串
    public static String username, password;    // 用户名，密码，用于重新登陆
    public static Member userInfo;  // 用户信息
    private final static String SESSION_STR = "{\"result\":\"success\",\"uid\":108263,\"username\":\"ihainan\",\"session\":\"SgAIeVTX\",\"status\":\"Member\",\"credit\":\"0\",\"lastactivity\":1454659703}";

    /* 缓存相关 */
    private static ACache cache;
    public final static String CACHE_USER_INFO = "CACHE_USER_INFO";
    public final static String CACHE_MESSAGE_IMAGE = "CACHE_MESSAGE_IMAGE";
    public final static String CACHE_USER_SESSION = "CACHE_SESSION";
    public final static String CACHE_REPLY_CONTENT = "CACHE_REPLY_CONTENT";
    public final static String CACHE_VIEW_POSITION = "CACHE_VIEW_POSITION";
    public final static String CACHE_MOST_VISITED_FORUMS = "CACHE_MOST_VISITED_FORUMS";

    public static int cacheDays = 10;
    public final static Integer VIEW_POSITION_CACHE_DAY = 1;


    private synchronized static void initCache(Context context) {
        if (cache == null) cache = ACache.get(context);
    }

    public static ACache getCache(Context context) {
        if (cache == null) initCache(context);
        return cache;
    }

    /* 系统配置相关*/
    public final static int MAX_USER_NAME_LENGTH = 15;  // 列表用户名最长显示的长度
    public final static int HOT_TOPIC_THREAD = 30; // 热门帖子阈值
    public final static int LOADING_COUNT = 10; // 一次最多 Loading 的帖子数目
    public final static int LOADING_POSTS_COUNT = 10; // 一次最多 Loading 的回复数目
    public final static int LOADING_TIMELINE_COUNT = 10; // 一次最多 Loading 的动态数目
    public final static int LOADING_FOLLOWING_COUNT = 20; // 一次最多 Loading 的关注用户数目

    public final static int LOADING_FAVORITES_COUNT = 10; // 一次最多 Loading 的收藏数目

    public final static String IMAGE_URL_PREFIX = "IMAGE_URL_PREFIX"; // 图片 URL 前缀，用于标记某个 URL 是图片

    public final static int RETRY_LIMIT = 2;    // 重新登录尝试次数
    public final static int SWIPE_LAYOUT_TRIGGER_DISTANCE = 400;    // 手指在屏幕下拉多少距离会触发下拉刷新
    public static Boolean debugMode = true;  // 是否启动 debug 模式
    public static Boolean saveDataMode = false; // 是否启动省流量模式
    public static Boolean uploadData = true;    // 是否自动上传数据
    public static Boolean ascendingOrder = true; // 是否按照升序排序
    public static boolean hasUpdateFavor = false;

    public enum NETWORK_TYPE {
        IN_SCHOOL,
        OUT_SCHOOL,
    }

    public static NETWORK_TYPE networkType = NETWORK_TYPE.OUT_SCHOOL;

    public static boolean isInSchool() {
        return networkType == NETWORK_TYPE.IN_SCHOOL;
    }

    public final static String PREF_USER_NAME = "PREF_USER_NAME";
    public final static String CONF_SESSION_STR = "CONF_SESSION_STR";
    public final static String PREF_PASSWORD = "PREF_PASSWORD";
    public final static String PREF_NETWORK_TYPE = "PREF_NETWORK_TYPE";
    public final static String PREF_REPLY_ORDER = "PREF_REPLY_ORDER";
    public final static String PREF_SAVE_DATA = "PREF_SAVE_DATA";
    public final static String PREF_DEBUG_MODE = "PREF_DEBUG_MODE";
    public final static String PREF_UPLOAD_DATA = "PREF_UPLOAD_DATA";

    public static void readConfig(Context context) {
        username = BUApplication.getCache(context).getAsString(PREF_USER_NAME);

        userSession = (Session) BUApplication.getCache(context).getAsObject(CONF_SESSION_STR);

        password = BUApplication.getCache(context).getAsString(PREF_PASSWORD);

        String networkTypeStr = BUApplication.getCache(context).getAsString(PREF_NETWORK_TYPE);
        networkType = networkTypeStr == null ? NETWORK_TYPE.OUT_SCHOOL : NETWORK_TYPE.valueOf(networkTypeStr);
        BUApi.currentEndPoint = networkType == NETWORK_TYPE.OUT_SCHOOL ? BUApi.OUT_SCHOOL_ENDPOINT : BUApi.IN_SCHOOL_ENDPOINT;

        ascendingOrder = (Boolean) BUApplication.getCache(context).getAsObject(PREF_REPLY_ORDER);
        if (ascendingOrder == null) ascendingOrder = true;

        uploadData = (Boolean) BUApplication.getCache(context).getAsObject(PREF_UPLOAD_DATA);
        if (uploadData == null) uploadData = true;

        saveDataMode = (Boolean) BUApplication.getCache(context).getAsObject(PREF_SAVE_DATA);
        if (saveDataMode == null) saveDataMode = false;

        debugMode = (Boolean) BUApplication.getCache(context).getAsObject(PREF_DEBUG_MODE);
        if (debugMode == null) debugMode = false;

        printConf();
    }

    public static void printConf() {
        Log.d(TAG, "printConf >> User Name：" + username);
        Log.d(TAG, "printConf >> User Session：" + userSession);
        Log.d(TAG, "printConf >> Password：" + (password == null ? "NULL" : "****"));
        Log.d(TAG, "printConf >> Network Type：" + networkType);
        Log.d(TAG, "printConf >> Increase Order：" + ascendingOrder);
        Log.d(TAG, "printConf >> Upload Data：" + uploadData);
        Log.d(TAG, "printConf >> Save Data Mode：" + saveDataMode);
        Log.d(TAG, "printConf >> Debug Mode：" + debugMode);
    }

    public static void saveConfig(Context context) {
        if (userSession != null) BUApplication.getCache(context).put(CONF_SESSION_STR, userSession);
        BUApplication.getCache(context).put(PREF_USER_NAME, BUApplication.username == null ? "" : BUApplication.username);
        BUApplication.getCache(context).put(PREF_PASSWORD, BUApplication.password == null ? "" : BUApplication.password);
        if (ascendingOrder != null)
            BUApplication.getCache(context).put(PREF_REPLY_ORDER, ascendingOrder);
        if (networkType != null)
            BUApplication.getCache(context).put(PREF_NETWORK_TYPE, networkType.toString());
        if (saveDataMode != null) BUApplication.getCache(context).put(PREF_SAVE_DATA, saveDataMode);
        if (uploadData != null) BUApplication.getCache(context).put(PREF_UPLOAD_DATA, uploadData);
        if (debugMode != null) BUApplication.getCache(context).put(PREF_DEBUG_MODE, debugMode);

        printConf();
    }


    /* 论坛列表相关 */
    public static List<ForumListGroup> forumListGroupList;
    public final static int MAX_MOST_VISITED = 5;

    public static void makeForumGroupList(Context context) {
        forumListGroupList = new ArrayList<>();
        HashMap<Integer, ForumListGroup.ForumList> forumListHashMap = new HashMap<>();

        // 系统管理区
        List<ForumListGroup.ForumList> forumLists = new ArrayList<>();
        ForumListGroup.ForumList forumList = new ForumListGroup.ForumList("联盟公告板", 3, "file:///android_asset/forumicon/announce.gif");
        forumListHashMap.put(3, forumList);
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("联盟意见箱", 4, "file:///android_asset/forumicon/chest.gif");
        forumListHashMap.put(4, forumList);
        forumList.addSubForum(new ForumListGroup.SubForum("处罚通告", 121));
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("后台管理区", 170, "file:///android_asset/forumicon/zhPg_aW1nNDE=.jpg");
        forumListHashMap.put(170, forumList);
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("新手交流区", 92, "file:///android_asset/forumicon/newbie.gif");
        forumListHashMap.put(192, forumList);
        forumList.addSubForum(new ForumListGroup.SubForum("积分恢复申请", 120));
        forumLists.add(forumList);

        ForumListGroup forumListGroup = new ForumListGroup(forumLists, "系统管理区");
        forumListGroupList.add(forumListGroup);

        // 直通理工区
        forumLists = new ArrayList<>();
        forumList = new ForumListGroup.ForumList("校园求助热线", 108, "file:///android_asset/forumicon/handshake.gif");
        forumListHashMap.put(108, forumList);
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("叫卖场", 59, "file:///android_asset/forumicon/money.gif");
        forumListHashMap.put(59, forumList);
        forumList.addSubForum(new ForumListGroup.SubForum("联盟旺铺", 114));
        forumList.addSubForum(new ForumListGroup.SubForum("团购专区", 145));
        forumList.addSubForum(new ForumListGroup.SubForum("已完成交易记录", 93));
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("前程似锦", 83, "file:///android_asset/forumicon/scroll.gif");
        forumListHashMap.put(83, forumList);
        forumList.addSubForum(new ForumListGroup.SubForum("考研兄弟连", 117));
        forumList.addSubForum(new ForumListGroup.SubForum("兼职信息", 153));
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("海外BITer", 150, "file:///android_asset/forumicon/graduation.gif");
        forumListHashMap.put(150, forumList);
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("外语园地", 89, "file:///android_asset/forumicon/locale.gif");
        forumListHashMap.put(89, forumList);
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("知识海报", 151, "file:///android_asset/forumicon/93.gif");
        forumListHashMap.put(151, forumList);
        forumLists.add(forumList);

        forumListGroup = new ForumListGroup(forumLists, "直通理工区");
        forumListGroupList.add(forumListGroup);

        // 时尚生活区
        forumLists = new ArrayList<>();

        forumList = new ForumListGroup.ForumList("购前咨询", 167, "file:///android_asset/forumicon/CQMr_aGVscDE=.jpg");
        forumListHashMap.put(167, forumList);
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("硬件与数码时尚", 80, "file:///android_asset/forumicon/hwinfo.gif");
        forumListHashMap.put(80, forumList);
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("驴游四海", 168, "file:///android_asset/forumicon/tnT6_MTMxMDEy.jpg");
        forumListHashMap.put(168, forumList);
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("摄影与艺术", 116, "file:///android_asset/forumicon/cam.gif");
        forumListHashMap.put(116, forumList);
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("车行天下", 140, "file:///android_asset/forumicon/tzQr_Mjk3NTk3M18xNDQwMzcwNzFfMg==.jpg");
        forumListHashMap.put(140, forumList);
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("生活会馆", 96, "file:///android_asset/forumicon/cookie.gif");
        forumListHashMap.put(96, forumList);
        forumLists.add(forumList);

        forumListGroup = new ForumListGroup(forumLists, "时尚生活区");
        forumListGroupList.add(forumListGroup);

        // 技术讨论区
        forumLists = new ArrayList<>();
        forumList = new ForumListGroup.ForumList("网络技术与信息", 10, "file:///android_asset/forumicon/browser.gif");
        forumListHashMap.put(10, forumList);
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("GNU/Linux 交流区", 84, "file:///android_asset/forumicon/linux.gif");
        forumListHashMap.put(84, forumList);
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("嵌入式开发技术", 101, "file:///android_asset/forumicon/embedded.png");
        forumListHashMap.put(101, forumList);
        forumList.addSubForum(new ForumListGroup.SubForum("嵌入式 LiNUX 开发", 113));
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("程序员集中营", 32, "file:///android_asset/forumicon/text_color.gif");
        forumListHashMap.put(32, forumList);
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("软件使用与交流", 21, "file:///android_asset/forumicon/software.gif");
        forumListHashMap.put(21, forumList);
        forumList.addSubForum(new ForumListGroup.SubForum("新软交流区", 107));
        forumLists.add(forumList);

        forumListGroup = new ForumListGroup(forumLists, "技术讨论区");
        forumListGroupList.add(forumListGroup);

        // 苦中作乐区
        forumLists = new ArrayList<>();
        forumList = new ForumListGroup.ForumList("游戏人生", 22, "file:///android_asset/forumicon/game.gif");
        forumListHashMap.put(22, forumList);
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("影视天地", 23, "file:///android_asset/forumicon/movie.gif");
        forumListHashMap.put(23, forumList);
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("音乐殿堂", 25, "file:///android_asset/forumicon/music.gif");
        forumListHashMap.put(25, forumList);
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("灌水乐园", 14, "file:///android_asset/forumicon/water.gif");
        forumListHashMap.put(14, forumList);
        forumList.addSubForum(new ForumListGroup.SubForum("站庆专版", 65));
        forumList.addSubForum(new ForumListGroup.SubForum("导师风采", 175));
        forumList.addSubForum(new ForumListGroup.SubForum("个人展示区", 106));
        forumList.addSubForum(new ForumListGroup.SubForum("教工之家", 122));
        forumList.addSubForum(new ForumListGroup.SubForum("原创文学", 66));
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("贴图欣赏", 24, "file:///android_asset/forumicon/image.gif");
        forumListHashMap.put(24, forumList);
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("动漫天空", 27, "file:///android_asset/forumicon/mascot.gif");
        forumListHashMap.put(27, forumList);
        forumList.addSubForum(new ForumListGroup.SubForum("动漫美图", 63));
        forumList.addSubForum(new ForumListGroup.SubForum("日语学习交流版", 110));
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("体坛风云", 115, "file:///android_asset/forumicon/run.gif");
        forumListHashMap.put(115, forumList);
        forumList.addSubForum(new ForumListGroup.SubForum("舞动桑巴", 102));
        forumList.addSubForum(new ForumListGroup.SubForum("菠菜组内部版面", 143));
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("职场生涯", 124, "file:///android_asset/forumicon/businessmen.gif");
        forumListHashMap.put(124, forumList);
        forumLists.add(forumList);

        forumListGroup = new ForumListGroup(forumLists, "苦中作乐区");
        forumListGroupList.add(forumListGroup);

        // 联盟交流区
        forumLists = new ArrayList<>();

        forumList = new ForumListGroup.ForumList("资源分享区", 171, "file:///android_asset/forumicon/Dbfr_z8LU2DE=.jpg");
        forumListHashMap.put(171, forumList);
        forumList.addSubForum(new ForumListGroup.SubForum("联盟 FTP 专版", 79));
        forumList.addSubForum(new ForumListGroup.SubForum("分享专版", 174));
        forumList.addSubForum(new ForumListGroup.SubForum("索档专版", 15));
        forumList.addSubForum(new ForumListGroup.SubForum("新手 FTP 交流", 78));
        forumList.addSubForum(new ForumListGroup.SubForum("D2R小组内部版面", 112));
        forumList.addSubForum(new ForumListGroup.SubForum("联盟字幕组内部版面", 133));
        forumLists.add(forumList);

        forumListGroup = new ForumListGroup(forumLists, "联盟交流区");
        forumListGroupList.add(forumListGroup);
        for (ForumListGroup group : forumListGroupList) {
            for (ForumListGroup.ForumList forums : group.getChildItemList()) {
                if (forums.getChildItemList().size() > 0) {
                    forums.addSubForum(new ForumListGroup.SubForum("主板块", forums.getForumId()), 0);
                }
            }
        }

        // 最常访问
        forumLists = new ArrayList<>();
        Map<Long, Long> visitedForumsMap = (Map<Long, Long>) getCache(context).getAsObject(CACHE_MOST_VISITED_FORUMS);
        if (visitedForumsMap == null || visitedForumsMap.size() == 0) {
            visitedForumsMap = new HashMap<>();
            visitedForumsMap.put(3L, 10L);
            visitedForumsMap.put(59L, 20L);
            visitedForumsMap.put(14L, 30L);
        }

        visitedForumsMap = sortByValue(visitedForumsMap);
        Set<Long> mostVisitedForumIndex = visitedForumsMap.keySet();

        int i = 0;
        for (Long index : mostVisitedForumIndex) {
            if (forumListHashMap.get(index.intValue()) != null) {
                forumList = forumListHashMap.get(index.intValue());
                forumLists.add(forumList);
                if (++i >= MAX_MOST_VISITED) break;
            }
        }

        forumListGroup = new ForumListGroup(forumLists, "最常访问");
        forumListGroupList.add(0, forumListGroup);
    }

    /**
     * 寻找一个子板块对应的主板块
     *
     * @param subForumID 子板块 ID
     * @return 子版块归属的主板块 ID
     */
    public static Long findMainForumID(Long subForumID) {
        for (ForumListGroup forumListGroup : BUApplication.forumListGroupList) {
            for (ForumListGroup.ForumList forumList : forumListGroup.getChildItemList()) {
                if (forumList.getForumId() == subForumID)
                    return subForumID;
                for (ForumListGroup.SubForum subForum : forumList.getChildItemList()) {
                    if (subForum.getSubForumId().equals(subForumID)) {
                        return forumList.getForumId();
                    }
                }
            }
        }

        return -1L;
    }

    /**
     * 更新板块访问频率 Map
     *
     * @param context     上下文
     * @param mainForumId 访问的板块对应的主板块 ID
     */
    public static void updateForumsMap(Context context, Long mainForumId) {
        HashMap<Long, Long> visitedForumsMap = (HashMap<Long, Long>) getCache(context).getAsObject(BUApplication.CACHE_MOST_VISITED_FORUMS);
        if (visitedForumsMap == null || visitedForumsMap.size() == 0) {
            visitedForumsMap = new HashMap<>();
        }

        Long value = visitedForumsMap.get(mainForumId);
        if (value == null) visitedForumsMap.put(mainForumId, 1L);
        else
            visitedForumsMap.put(mainForumId, value >= Long.MAX_VALUE ? Long.MAX_VALUE : 1 + value);
        getCache(context).put(BUApplication.CACHE_MOST_VISITED_FORUMS, visitedForumsMap);
    }

    /**
     * 根据 value 对 map 进行排序
     *
     * @param map 需要排序的 map
     * @param <K> key 类型
     * @param <V> value 类型
     * @return 排序后的新 map
     */
    public static <K, V extends Comparable<? super V>> Map<K, V>
    sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list =
                new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public final static Map<String, Boolean> badImages = new HashMap<>();

    /* 小米推送 */
    public static final String APP_ID = "2882303761517451788";
    public static final String APP_KEY = "5871745172788";
    public static final String TAG = "me.ihainan.bu.app";

    private boolean shouldInit() {
        ActivityManager am = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = getPackageName();
        int myPid = android.os.Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // 小米推送
        if (shouldInit()) {
            MiPushClient.registerPush(this, APP_ID, APP_KEY);
        }

        LoggerInterface newLogger = new LoggerInterface() {

            @Override
            public void setTag(String tag) {
                // ignore
            }

            @Override
            public void log(String content, Throwable t) {
                Log.d(TAG, content, t);
            }

            @Override
            public void log(String content) {
                Log.d(TAG, content);
            }
        };

        Logger.setLogger(this, newLogger);

        // 自动更新配置
        UmengUpdateAgent.setDeltaUpdate(true);  // 增量更新
        if (debugMode) UpdateConfig.setDebug(true);
        UmengUpdateAgent.setRichNotification(true);
        UmengUpdateAgent.setUpdateOnlyWifi(false);

        // 从缓存中读取数据
        readConfig(this);

        // 配置 Picasso 的磁盘缓存（配合  OKHttp）
        Picasso.Builder builder = new Picasso.Builder(this);
        builder.downloader(new OkHttpDownloader(this, Integer.MAX_VALUE));
        builder.listener(new Picasso.Listener() {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri, Exception exception) {
                Log.e(TAG, "failed to load image from " + uri, exception);
            }
        });
        Picasso built = builder.build();
        if (debugMode) {
            built.setIndicatorsEnabled(true);
            built.setLoggingEnabled(true);
        }
        Picasso.setSingletonInstance(built);
    }
}
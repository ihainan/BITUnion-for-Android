package bit.ihainan.me.bitunionforandroid.utils;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.models.ForumListGroup;
import bit.ihainan.me.bitunionforandroid.models.Member;
import bit.ihainan.me.bitunionforandroid.models.Session;

/**
 * 系统设置与全局变量
 */
public class Global extends Application {
    public final static String TAG = Global.class.getSimpleName();

    /* 会话相关 */
    public static Session userSession = null;   // 用户会话实例，向服务器发送请求时候需要附上会话字符串
    public static String userName, password;    // 用户名，密码，用于重新登陆
    public static Member userInfo;  // 用户信息
    private final static String SESSION_STR = "{\"result\":\"success\",\"uid\":108263,\"username\":\"ihainan\",\"session\":\"SgAIeVTX\",\"status\":\"Member\",\"credit\":\"0\",\"lastactivity\":1454659703}";

    /* 缓存相关 */
    private static ACache cache;
    public final static String CACHE_USER_INFO = "CACHE_USER_INFO";
    public final static String CACHE_USER_SESSION = "CACHE_SESSION";
    public final static String CACHE_REPLY_CONTENT = "CACHE_REPLY_CONTENT";
    public static int cacheDays = 1;

    private synchronized static void initCache(Context context) {
        if (cache == null) cache = ACache.get(context);
    }

    public static ACache getCache(Context context) {
        if (cache == null) initCache(context);
        return cache;
    }

    /* 系统配置相关*/
    public final static int HOT_TOPIC_THREAD = 30; // 热门帖子阈值
    public final static int LOADING_COUNT = 10; // 一次最多 Loading 的帖子数目
    public final static int LOADING_REPLIES_COUNT = 10; // 一次最多 Loading 的回复数目
    public final static int RETRY_LIMIT = 3;    // 重新登录尝试次数
    public final static int SWIPE_LAYOUT_TRIGGER_DISTANCE = 400;    // 手指在屏幕下拉多少距离会触发下拉刷新
    public static Boolean debugMode = true;
    public static Boolean increaseOrder = true;

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

    public static void readConfig(Context context) {
        userName = Global.getCache(context).getAsString(PREF_USER_NAME);
        userSession = (Session) Global.getCache(context).getAsObject(CONF_SESSION_STR);
        password = Global.getCache(context).getAsString(PREF_PASSWORD);
        String networkTypeStr = Global.getCache(context).getAsString(PREF_NETWORK_TYPE);
        networkType = networkTypeStr == null ? NETWORK_TYPE.OUT_SCHOOL : NETWORK_TYPE.valueOf(networkTypeStr);
        increaseOrder = (Boolean) Global.getCache(context).getAsObject(PREF_REPLY_ORDER);
        if (increaseOrder == null) increaseOrder = true;

        Log.i(TAG, "readConfig >> " + userName + " " + (password == null ? "NULL" : "****") + " " + networkType + " " + userSession + " " + increaseOrder);

//        if (userSession == null) try {
//            Log.i(TAG, "readConfig >>  Session == null, 使用特定 Session 值： " + userSession);
//            userSession = Api.MAPPER.readValue(SESSION_STR, Session.class);
//        } catch (IOException e) {
//            Log.e(TAG, "readConfig >> 解析服务器返回数据失败", e);
//        }
    }

    public static void saveConfig(Context context) {
        if (userSession != null) Global.getCache(context).put(CONF_SESSION_STR, userSession);
        if (userName != null) Global.getCache(context).put(PREF_USER_NAME, userName);
        if (password != null) Global.getCache(context).put(PREF_PASSWORD, password);
        if (increaseOrder != null) Global.getCache(context).put(PREF_REPLY_ORDER, increaseOrder);
        if (networkType != null)
            Global.getCache(context).put(PREF_NETWORK_TYPE, networkType.toString());

        Log.i(TAG, "saveConfig >> " + userName + " " + (password == null ? "NULL" : "****") + " " + networkType + " " + userSession + " " + increaseOrder);
    }

    // END POINT
    public final static String IN_SCHOOL_BASE_URL = "http://www.bitunion.org/";
    public final static String OUT_SCHOOL_BASE_URL = "http://out.bitunion.org/";
    public final static String IN_SCHOOL_ENDPOINT = IN_SCHOOL_BASE_URL + "open_api/";
    public final static String OUT_SCHOOL_ENDPOINT = OUT_SCHOOL_BASE_URL + "open_api/";
    public static String currentEndPoint = OUT_SCHOOL_ENDPOINT;

    public static String getBaseURL() {
        if (currentEndPoint.startsWith(IN_SCHOOL_ENDPOINT)) return IN_SCHOOL_BASE_URL;
        else return OUT_SCHOOL_BASE_URL;
    }

    public static String getLoginURL() {
        return Global.currentEndPoint + "bu_logging.php";
    }

    public static String getHomePageURL() {
        return Global.currentEndPoint + "bu_home.php";
    }

    public static String getUserInfoURL() {
        return Global.currentEndPoint + "bu_profile.php";
    }

    public static String getThreadDetailURL() {
        return Global.currentEndPoint + "bu_post.php";
    }

    public static String getThreadListURL() {
        return Global.currentEndPoint + "bu_thread.php";
    }

    public static String getForumListURL() {
        return Global.currentEndPoint + "bu_thread.php";
    }

    public static List<ForumListGroup> forumListGroupList;

    /* 论坛列表相关 */
    public static void makeForumGroupList(Context context) {
        forumListGroupList = new ArrayList<ForumListGroup>();

        // 系统管理区
        List<ForumListGroup.ForumList> forumLists = new ArrayList<>();
        ForumListGroup.ForumList forumList = new ForumListGroup.ForumList("联盟公告板", 3, CommonUtils.getRealImageURL("images/forumicon/announce.gif"));
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("联盟意见箱", 4, CommonUtils.getRealImageURL("images/forumicon/chest.gif"));
        forumList.addSubForum(new ForumListGroup.SubForum("处罚通告", 121));
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("后台管理区", 170, CommonUtils.getRealImageURL("attachments/forumid_92/z/h/zhPg_aW1nNDE=.jpg"));
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("新手交流区", 92, CommonUtils.getRealImageURL("images/forumicon/newbie.gif"));
        forumList.addSubForum(new ForumListGroup.SubForum("积分恢复申请", 120));
        forumLists.add(forumList);

        ForumListGroup forumListGroup = new ForumListGroup(forumLists, "系统管理区");
        forumListGroupList.add(forumListGroup);

        // 直通理工区
        forumLists = new ArrayList<>();
        forumList = new ForumListGroup.ForumList("校园求助热线", 108, CommonUtils.getRealImageURL("images/forumicon/handshake.gif"));
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("叫卖场", 59, CommonUtils.getRealImageURL("http://out.bitunion.org/images/forumicon/money.gif"));
        forumList.addSubForum(new ForumListGroup.SubForum("联盟旺铺", 114));
        forumList.addSubForum(new ForumListGroup.SubForum("团购专区", 145));
        forumList.addSubForum(new ForumListGroup.SubForum("已完成交易记录", 93));
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("前程似锦", 83, CommonUtils.getRealImageURL("http://out.bitunion.org/images/forumicon/scroll.gif"));
        forumList.addSubForum(new ForumListGroup.SubForum("考研兄弟连", 117));
        forumList.addSubForum(new ForumListGroup.SubForum("兼职信息", 153));
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("海外BITer", 150, CommonUtils.getRealImageURL("images/forumicon/graduation.gif"));
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("外语园地", 89, CommonUtils.getRealImageURL("images/forumicon/locale.gif"));
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("知识海报", 151, CommonUtils.getRealImageURL("images/bz/93.gif"));
        forumLists.add(forumList);

        forumListGroup = new ForumListGroup(forumLists, "直通理工区");
        forumListGroupList.add(forumListGroup);

        // 时尚生活区
        forumLists = new ArrayList<>();

        forumList = new ForumListGroup.ForumList("购前咨询", 167, CommonUtils.getRealImageURL("attachments/forumid_81/C/Q/CQMr_aGVscDE=.jpg"));
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("硬件与数码时尚", 80, CommonUtils.getRealImageURL("images/forumicon/hwinfo.gif"));
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("驴游四海", 168, CommonUtils.getRealImageURL("attachments/forumid_81/t/n/tnT6_MTMxMDEy.jpg"));
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("摄影与艺术", 116, CommonUtils.getRealImageURL("images/forumicon/cam.gif"));
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("车行天下", 140, CommonUtils.getRealImageURL("attachments/forumid_81/t/z/tzQr_Mjk3NTk3M18xNDQwMzcwNzFfMg==.jpg"));
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("生活会馆", 96, CommonUtils.getRealImageURL("images/forumicon/cookie.gif"));
        forumLists.add(forumList);

        forumListGroup = new ForumListGroup(forumLists, "时尚生活区");
        forumListGroupList.add(forumListGroup);

        // 技术讨论区
        forumLists = new ArrayList<>();
        forumList = new ForumListGroup.ForumList("网络技术与信息", 10, CommonUtils.getRealImageURL("images/forumicon/browser.gif"));
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("GNU/Linux 交流区", 84, CommonUtils.getRealImageURL("images/forumicon/linux.gif"));
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("嵌入式开发技术", 101, CommonUtils.getRealImageURL("images/forumicon/embedded.png"));
        forumList.addSubForum(new ForumListGroup.SubForum("嵌入式 LiNUX 开发", 113));
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("程序员集中营", 32, CommonUtils.getRealImageURL("images/forumicon/text_color.gif"));
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("软件使用与交流", 21, CommonUtils.getRealImageURL("images/forumicon/software.gif"));
        forumList.addSubForum(new ForumListGroup.SubForum("新软交流区", 107));
        forumLists.add(forumList);

        forumListGroup = new ForumListGroup(forumLists, "技术讨论区");
        forumListGroupList.add(forumListGroup);

        // 苦中作乐区
        forumLists = new ArrayList<>();
        forumList = new ForumListGroup.ForumList("游戏人生", 22, CommonUtils.getRealImageURL("images/forumicon/game.gif"));
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("影视天地", 23, CommonUtils.getRealImageURL("images/forumicon/movie.gif"));
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("音乐殿堂", 25, CommonUtils.getRealImageURL("images/forumicon/music.gif"));
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("灌水乐园", 14, CommonUtils.getRealImageURL("images/forumicon/water.gif"));
        forumList.addSubForum(new ForumListGroup.SubForum("站庆专版", 65));
        forumList.addSubForum(new ForumListGroup.SubForum("导师风采", 175));
        forumList.addSubForum(new ForumListGroup.SubForum("个人展示区", 106));
        forumList.addSubForum(new ForumListGroup.SubForum("教工之家", 122));
        forumList.addSubForum(new ForumListGroup.SubForum("原创文学", 66));
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("贴图欣赏", 24, CommonUtils.getRealImageURL("images/forumicon/image.gif"));
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("动漫天空", 27, CommonUtils.getRealImageURL("images/forumicon/mascot.gif"));
        forumList.addSubForum(new ForumListGroup.SubForum("动漫美图", 63));
        forumList.addSubForum(new ForumListGroup.SubForum("日语学习交流版", 110));
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("体坛风云", 115, CommonUtils.getRealImageURL("images/forumicon/run.gif"));
        forumList.addSubForum(new ForumListGroup.SubForum("舞动桑巴", 102));
        forumList.addSubForum(new ForumListGroup.SubForum("菠菜组内部版面", 143));
        forumLists.add(forumList);

        forumList = new ForumListGroup.ForumList("职场生涯", 124, CommonUtils.getRealImageURL("images/forumicon/businessmen.gif"));
        forumLists.add(forumList);

        forumListGroup = new ForumListGroup(forumLists, "苦中作乐区");
        forumListGroupList.add(forumListGroup);

        // 联盟交流区
        forumLists = new ArrayList<>();

        forumList = new ForumListGroup.ForumList("资源分享区", 171, CommonUtils.getRealImageURL("attachments/forumid_92/D/b/Dbfr_z8LU2DE=.jpg"));
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
    }

    @Override
    public void onCreate() {
        super.onCreate();

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
        // built.setIndicatorsEnabled(true);
        // built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);

        // 论坛列表
        // TODO: 从服务器获取
        // makeForumGroupList(getApplicationContext());
    }
}
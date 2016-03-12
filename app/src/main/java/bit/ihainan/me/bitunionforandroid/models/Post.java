package bit.ihainan.me.bitunionforandroid.models;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.Serializable;

import bit.ihainan.me.bitunionforandroid.utils.network.BUApi;

/**
 * 回帖模型
 */
public class Post implements Serializable {
    public final static String TAG = Post.class.getSimpleName();

    // Json mapping fields
    public long post_id;
    public int floor;
    public long pid;
    public long fid;
    public long tid;
    public long aid;
    public String icon;
    public String author;   // 和 username 区别在于
    public long authorid;   // 和 uid 的区别在于？
    public String subject;
    public long dateline;
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
    public String filesize;
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
    public String dt_created;

    @Override
    public String toString() {
        try {
            return BUApi.MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            Log.e(TAG, "Failed to convert object to JSON string", e);
            return null;
        }
    }

    public boolean useMobile;
}

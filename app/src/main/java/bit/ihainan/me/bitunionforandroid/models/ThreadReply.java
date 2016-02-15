package bit.ihainan.me.bitunionforandroid.models;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.Serializable;

import bit.ihainan.me.bitunionforandroid.utils.Api;

/**
 * 回帖模型
 */
public class ThreadReply implements Serializable {
    public final static String TAG = ThreadReply.class.getSimpleName();

    // Json mapping fields
    public long pid;
    public long fid;
    public long tid;
    public long aid;
    public String icon;
    public String author;
    public long authorid;
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
    public String lastedit;
    public String postsource;
    public String aaid;
    public String creditsrequire;
    public String filetype;
    public String filename;
    public String attachment;
    public String filesize;
    public String downloads;
    public long uid;
    public String username;
    public String avatar;
    public String epid;
    public long maskpost;
    public String attachext;
    public String attachsize;
    public String attachimg;
    public String exif;

    @Override
    public String toString() {
        try {
            return Api.MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            Log.e(TAG, "Failed to convert object to JSON string", e);
            return null;
        }
    }

    public boolean useMobile;
}

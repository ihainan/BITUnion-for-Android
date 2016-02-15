package bit.ihainan.me.bitunionforandroid.models;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.io.Serializable;

import bit.ihainan.me.bitunionforandroid.utils.Api;

/**
 * Member Model
 */
public class Member implements Serializable {
    public final static String TAG = Member.class.getSimpleName();

    // Json mapping fields
    public long uid;
    public String status;
    public String username;
    public String avatar;
    public long credit;
    public long regdate;
    public long lastvisit;
    public String bday;
    public String signature;
    public int postnum;
    public int threadnum;
    public String email;
    public String site;
    public String icq;
    public String oicq;
    public String yahoo;
    public String msn;

    @Override
    public String toString() {
        try {
            return Api.MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            Log.e(TAG, "Failed to convert object to JSON string", e);
            return null;
        }
    }
}

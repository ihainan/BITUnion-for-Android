package bit.ihainan.me.bitunionforandroid.models;

import android.util.Log;

import com.fasterxml.jackson.core.JsonProcessingException;

import bit.ihainan.me.bitunionforandroid.utils.network.BUApi;

/**
 * Favorite Model
 */
public class Favorite {
    public final static String TAG = Favorite.class.getSimpleName();

    public Long tid;
    public String subject;
    public String author;
    public String dt_created;
    public String username;

    @Override
    public String toString() {
        try {
            return BUApi.MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            Log.e(TAG, "Failed to convert object to JSON string", e);
            return null;
        }
    }
}

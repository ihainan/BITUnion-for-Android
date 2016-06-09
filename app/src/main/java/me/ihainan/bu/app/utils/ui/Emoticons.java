package me.ihainan.bu.app.utils.ui;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;

import java.util.Iterator;
import java.util.LinkedHashMap;

import me.ihainan.bu.app.adapters.EmoticonAdapter;
import me.ihainan.bu.app.utils.CommonUtils;

/**
 * This class maps emoticon strings to asset images
 */
public class Emoticons {
    public static final String TAG = Emoticons.class.getSimpleName();

    public static final LinkedHashMap<String, String> EMOTICONS = new LinkedHashMap<>();
    public static final LinkedHashMap<String, Bitmap> EMOTICON_BITMAPS = new LinkedHashMap<>();
    public static final LinkedHashMap<String, Bitmap> EMOTICON_BITMAPS_SCALED = new LinkedHashMap<>();

    static {
        EMOTICONS.put(":sweating:", "smilies_sweatingbullets.gif");
        EMOTICONS.put(":nugget:", "smilies_nugget.gif");
        EMOTICONS.put(":cool:", "smilies_cool.gif");
        EMOTICONS.put(":mad:", "smilies_mad.gif");
        EMOTICONS.put(":sad:", "smilies_sad.gif");
        EMOTICONS.put(":shifty:", "smilies_shifty.gif");
        EMOTICONS.put(":shocked:", "smilies_shocked.gif");
        EMOTICONS.put(":smile:", "smilies_smile.gif");
        EMOTICONS.put(":tongue:", "smilies_tongue.gif");
        EMOTICONS.put(":wink:", "smilies_wink.gif");

        for (int i = 71; i <= 142; ++i) {
            if (i != 129) EMOTICONS.put(":bz" + i + ":", "bz_" + i + ".gif");
        }

        for (int i = 401; i <= 432; ++i) {
            if (i != 407) EMOTICONS.put(":bz" + i + ":", "bz_" + i + ".gif");
        }
    }

    public static void init(Context context) {

        int size = CommonUtils.getFontHeight(context, 16f);

        AssetManager am = context.getAssets();

        for (String key : EMOTICONS.keySet()) {
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(am.open("faces/" + EMOTICONS.get(key)));
                EMOTICON_BITMAPS.put(key, bitmap);

                // Scale by font size
                Matrix matrix = new Matrix();
                matrix.postScale((float) size / bitmap.getWidth(), (float) size / bitmap.getHeight());
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                EMOTICON_BITMAPS_SCALED.put(key, bitmap);

            } catch (Exception e) {
                Log.e(TAG, "init >> ", e);
            }

        }

        EmoticonAdapter.init();
    }
}

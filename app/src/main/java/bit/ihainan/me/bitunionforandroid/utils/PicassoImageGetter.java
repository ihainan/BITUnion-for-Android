package bit.ihainan.me.bitunionforandroid.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ResourceBundle;

/**
 * Picasso Image Getter
 */
public class PicassoImageGetter implements Html.ImageGetter {
    final Context mContext;
    final Picasso picasso;
    final Resources resources;
    final TextView textView;

    public PicassoImageGetter(Context context, TextView textView) {
        this.mContext = context;
        resources = context.getResources();
        picasso = Picasso.with(context);
        this.textView = textView;
    }

    @Override
    public Drawable getDrawable(final String source) {
        final BitmapDrawablePlaceHolder result = new BitmapDrawablePlaceHolder();

        new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Void... params) {
                try {
                    // 获取图片
                    return picasso.load(source).get();
                } catch (IOException e) {
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                final BitmapDrawable drawable = new BitmapDrawable(resources, bitmap);
                drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                result.setDrawable(drawable);
                result.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

                textView.setText(textView.getText());
            }
        }.execute((Void) null);

        return result;
    }

    public static class BitmapDrawablePlaceHolder extends BitmapDrawable {
        protected Drawable mDrawable;

        @Override
        public void draw(Canvas canvas) {
            if (mDrawable != null) {
                mDrawable.draw(canvas);
            }
        }

        public void setDrawable(Drawable drawable) {
            this.mDrawable = drawable;
        }
    }
}

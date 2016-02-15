package bit.ihainan.me.bitunionforandroid.ui.assist;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import bit.ihainan.me.bitunionforandroid.BuildConfig;
import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.utils.Global;

/**
 * ABout Page
 */
public class AboutFragment extends Fragment {
    private final static String TAG = AboutFragment.class.getSimpleName();
    private Context mContext;

    private View mRootView;
    private TextView mVersion, mAuthor;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mContext = getActivity();

            mRootView = inflater.inflate(R.layout.fragment_about, container, false);

            Global.makeForumGroupList(mContext);

            // UI references
            mVersion = (TextView) mRootView.findViewById(R.id.version);
            mVersion.setText("Version " + BuildConfig.VERSION_NAME + " (Code " + BuildConfig.VERSION_CODE + ")");
            mAuthor = (TextView) mRootView.findViewById(R.id.author);
            mAuthor.setText(Html.fromHtml("Author: <a href='http://github.com/ihainan'>@ihainan</a>"));
            mAuthor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse("http://www.github.com/ihainan"));
                    startActivity(i);
                }
            });
        }

        return mRootView;
    }
}

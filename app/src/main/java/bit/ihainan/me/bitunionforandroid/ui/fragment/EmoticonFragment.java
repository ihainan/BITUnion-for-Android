package bit.ihainan.me.bitunionforandroid.ui.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.adapters.EmoticonAdapter;
import bit.ihainan.me.bitunionforandroid.utils.ui.Emoticons;

/**
 * Emoticon Fragment
 */
public class EmoticonFragment extends Fragment implements AdapterView.OnItemClickListener {
    public static interface EmoticonListener {
        public void onEmoticonSelected(String name);
    }

    private GridView mGrid;
    private EmoticonAdapter mAdapter;

    // listener
    private EmoticonListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_emoticon, null);
        mGrid = (GridView) v.findViewById(R.id.emoticon_grid);

        // Ensure Emoticons are not null
        if (Emoticons.EMOTICON_BITMAPS.size() == 0) {
            Emoticons.init(getActivity());
        }

        // adapter
        mAdapter = new EmoticonAdapter(getActivity());
        mGrid.setAdapter(mAdapter);

        // listener
        mGrid.setOnItemClickListener(this);

        return v;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mListener != null) {
            mListener.onEmoticonSelected(mAdapter.getItem(position));
        }
    }

    public void setEmoticonListener(EmoticonListener listener) {
        mListener = listener;
    }
}

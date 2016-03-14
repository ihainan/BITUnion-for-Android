package bit.ihainan.me.bitunionforandroid.ui.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.core.type.TypeReference;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import bit.ihainan.me.bitunionforandroid.R;
import bit.ihainan.me.bitunionforandroid.adapters.PostListAdapter;
import bit.ihainan.me.bitunionforandroid.models.Post;
import bit.ihainan.me.bitunionforandroid.ui.ThreadDetailNewActivity;
import bit.ihainan.me.bitunionforandroid.ui.assist.SimpleDividerItemDecoration;
import bit.ihainan.me.bitunionforandroid.utils.CommonUtils;
import bit.ihainan.me.bitunionforandroid.utils.Global;
import bit.ihainan.me.bitunionforandroid.utils.network.BUApi;
import bit.ihainan.me.bitunionforandroid.utils.ui.HtmlUtil;

/**
 * Post List Fragment
 */
public class PostListFragment extends Fragment {
    // Tags
    private final static String TAG = PostListFragment.class.getSimpleName();
    public final static String PAGE_POSITION_TAG = "PAGE_POSITION_TAG";

    private Context mContext;

    // UI references
    private View mRootView;
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LinearLayoutManager mLayoutManager;

    // Data
    private Long mTid, mReplyCount;
    private Integer mPagePosition;
    private String mAuthorName;
    private List<Post> mList = new ArrayList<>();
    private PostListAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mContext = getActivity();
            mRootView = inflater.inflate(R.layout.fragment_recyclerview, container, false);

            // Tid & page position
            mTid = getArguments().getLong(ThreadDetailNewActivity.THREAD_ID_TAG);
            mAuthorName = getArguments().getString(ThreadDetailNewActivity.THREAD_AUTHOR_NAME_TAG);
            mReplyCount = getArguments().getLong(ThreadDetailNewActivity.THREAD_REPLY_COUNT_TAG);
            mPagePosition = getArguments().getInt(PAGE_POSITION_TAG);

            // Setup RecyclerView
            mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
            setupRecyclerView();

            // Setup SwipeLayout
            mSwipeRefreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.swipe_refresh_layout);
            setupSwipeRefreshLayout();
        }

        return mRootView;
    }

    private void setupRecyclerView() {
        mLayoutManager = new LinearLayoutManager(mContext);

        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(mContext));
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Adapter
        mAdapter = new PostListAdapter(mContext, mList, mAuthorName, mReplyCount);
        mRecyclerView.setAdapter(mAdapter);
    }

    private void setupSwipeRefreshLayout() {
        mSwipeRefreshLayout.setDistanceToTriggerSync(Global.SWIPE_LAYOUT_TRIGGER_DISTANCE);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 重新加载数据
                reloadData();
            }
        });

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                // 第一次加载数据
                reloadData();
            }
        });
    }


    /**
     * 重新拉取数据
     */
    private void reloadData() {
        mSwipeRefreshLayout.setRefreshing(true);
        mList.clear();
        refreshData(mPagePosition * Global.LOADING_POSTS_COUNT, (mPagePosition + 1) * Global.LOADING_COUNT);
    }

    /**
     * 更新列表数据
     */
    private void refreshData(final long from, final long to) {
        BUApi.getPostReplies(mContext, mTid, from, to,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        mSwipeRefreshLayout.setRefreshing(false);

                        if (BUApi.checkStatus(response)) {
                            try {
                                JSONArray newListJson = response.getJSONArray("postlist");
                                List<Post> newThreads = BUApi.MAPPER.readValue(newListJson.toString(),
                                        new TypeReference<List<Post>>() {
                                        });

                                // 处理数据
                                for (Post reply : newThreads) {
                                    // 处理正文
                                    String body = CommonUtils.decode(reply.message);
                                    reply.useMobile = body.contains("From BIT-Union Open API Project");
                                    HtmlUtil htmlUtil = new HtmlUtil(CommonUtils.decode(reply.message));
                                    reply.message = htmlUtil.makeAll();

                                    // 楼层
                                    reply.floor = mPagePosition * Global.LOADING_POSTS_COUNT + newThreads.indexOf(reply) + 1;

                                    // 获取设备信息
                                    getDeviceName(reply);
                                }

                                // 更新 RecyclerView
                                mList.addAll(newThreads);
                                mAdapter.notifyDataSetChanged();
                            } catch (Exception e) {
                                Log.e(TAG, getString(R.string.error_parse_json) + "\n" + response, e);

                                Snackbar.make(mRecyclerView, getString(R.string.error_parse_json),
                                        Snackbar.LENGTH_INDEFINITE).setAction("RETRY", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        reloadData();
                                    }
                                }).show();
                            }
                        } else {
                            Log.i(TAG, "refreshData >> " + getString(R.string.error_unknown_json) + "" + response);
                            Snackbar.make(mRecyclerView, getString(R.string.error_unknown_json), Snackbar.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        mSwipeRefreshLayout.setRefreshing(false);

                        Snackbar.make(mRecyclerView, getString(R.string.error_network), Snackbar.LENGTH_INDEFINITE).setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                reloadData();
                            }
                        }).show();

                        Log.e(TAG, getString(R.string.error_network), error);
                    }
                });
    }

    private void getDeviceName(Post reply) {
        // Log.d(TAG, "getDeviceName >> " + reply.message);
        String[] regexStrArray = new String[]{"<a .*?>\\.\\.::发自(.*?)::\\.\\.</a>$",
                "<br><br>发送自 <a href='.*?' target='_blank'><b>(.*?) @BUApp</b></a>",
                "<i>来自傲立独行的(.*?)客户端</i>$",
                "<br><br><i>发自联盟(.*?)客户端</i>$",
                "<a href='.*?>..::发自联盟(.*?)客户端::..</a>"};
        if (reply.message.contains("客户端") || reply.message.contains("发自"))
            Log.d(TAG, "getDeviceName >> " + reply.message);
        for (String regex : regexStrArray) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(reply.message);
            while (matcher.find()) {
                // 找到啦！
                reply.deviceName = matcher.group(1);
                if (reply.deviceName.equals("WindowsPhone8"))
                    reply.deviceName = "Windows Phone 8";
                if (reply.deviceName.equals("联盟iOS客户端"))
                    reply.deviceName = "iPhone";
                Log.d(TAG, "deviceName >> " + reply.deviceName);
                reply.message = reply.message.replace(matcher.group(0), "");
                reply.message = HtmlUtil.replaceOther(reply.message);
                return;
            }
        }

        reply.deviceName = "";
    }
}

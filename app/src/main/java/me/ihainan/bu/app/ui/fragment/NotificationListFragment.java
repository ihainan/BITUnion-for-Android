package me.ihainan.bu.app.ui.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.fasterxml.jackson.core.type.TypeReference;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.adapters.NotificationAdapter;
import me.ihainan.bu.app.models.Notification;
import me.ihainan.bu.app.utils.BUApplication;
import me.ihainan.bu.app.utils.CommonUtils;
import me.ihainan.bu.app.utils.network.BUApi;
import me.ihainan.bu.app.utils.network.ExtraApi;

/**
 * 通知 Fragment
 */
public class NotificationListFragment extends BasicRecyclerViewFragment<Notification> {
    // TAGs
    public final static String TAG = NotificationListFragment.class.getSimpleName();

    @Override
    protected String getNoNewDataMessage() {
        return mContext.getString(R.string.error_no_notifications);
    }

    @Override
    protected String getFragmentTag() {
        return TAG;
    }

    @Override
    protected List<Notification> processList(List<Notification> list) {
        return list;
    }

    @Override
    protected List<Notification> parseResponse(JSONObject response) throws Exception {
        JSONArray newListJson = response.getJSONArray("data");
        return BUApi.MAPPER.readValue(newListJson.toString(), new TypeReference<List<Notification>>() {
        });
    }

    @Override
    protected boolean checkStatus(JSONObject response) {
        return ExtraApi.checkStatus(response);
    }

    @Override
    protected void getExtra() {

    }

    @Override
    protected RecyclerView.Adapter<RecyclerView.ViewHolder> getAdapter() {
        return new NotificationAdapter(mContext, mList);
    }

    @Override
    protected int getLoadingCount() {
        return BUApplication.LOADING_NOTIFICATION_COUNT;
    }

    @Override
    protected void refreshData() {
        ExtraApi.getNotificationList(mContext, BUApplication.username, from, to, listener, errorListener);
        // ExtraApi.getNotificationList(mContext, "manoflake", from, to, listener, errorListener);
    }

    public void checkAll() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getString(R.string.title_warning))
                .setMessage(mContext.getString(R.string.message_mark_all_as_read))
                .setPositiveButton(mContext.getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ExtraApi.markAllAsRead(mContext, BUApplication.username, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d(TAG, "markAsRead >> " + response.toString());
                                CommonUtils.debugToast(mContext, "标为已读成功");
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                String message = mContext.getString(R.string.error_network);
                                String debugMessage = TAG + " >> " + message;
                                CommonUtils.debugToast(mContext, debugMessage);
                                Log.e(TAG, debugMessage, error);
                            }
                        });

                        for (Notification notification : mList) {
                            notification.is_read = 1;
                        }

                        mAdapter.notifyDataSetChanged();
                    }
                }).setNegativeButton(mContext.getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        if (!((Activity) mContext).isFinishing()) {
            builder.show();
        }
    }
}

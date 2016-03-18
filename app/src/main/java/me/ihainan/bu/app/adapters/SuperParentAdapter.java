package me.ihainan.bu.app.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.ihainan.bu.app.R;
import me.ihainan.bu.app.models.ForumListGroup;
import me.ihainan.bu.app.ui.ThreadListActivity;
import de.hdodenhof.circleimageview.CircleImageView;

public class SuperParentAdapter extends BaseExpandableListAdapter {
    public final static String TAG = SuperParentAdapter.class.getSimpleName();
    private Context mContext;
    private List<ForumListGroup> mForumListGroups;

    public SuperParentAdapter(Context context, List<ForumListGroup> forumListGroups) {
        mContext = context;
        mForumListGroups = forumListGroups;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mForumListGroups.get(groupPosition).getChildItemList().get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    private Map<String, SecondLevelExpandableListView> secondLevelExpandableListViewMap = new HashMap<>();

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (secondLevelExpandableListViewMap.get(groupPosition + "-" + childPosition) != null) {
            return secondLevelExpandableListViewMap.get(groupPosition + "-" + childPosition);
        } else {
            SecondLevelExpandableListView secondLevelELV = new SecondLevelExpandableListView(mContext);
            secondLevelELV.setAdapter(new SecondLevelAdapter(mContext, mForumListGroups.get(groupPosition).getChildItemList()));
            secondLevelELV.setGroupIndicator(null);
            secondLevelExpandableListViewMap.put(groupPosition + "-" + childPosition, secondLevelELV);
            return secondLevelELV;
        }
    }

    // See: http://stackoverflow.com/questions/15286219/repeating-children-in-expandable-listview-android
    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mForumListGroups.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return mForumListGroups == null ? 0 : mForumListGroups.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ForumListGroup forumListGroup = mForumListGroups.get(groupPosition);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_head, null);
        }

        TextView text = (TextView) convertView.findViewById(R.id.form_group_head);
        text.setText(forumListGroup.getForumGroupName());

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public static class SecondLevelExpandableListView extends ExpandableListView {

        public SecondLevelExpandableListView(Context context) {
            super(context);
            setDivider(null);
        }

        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            //999999 is a size in pixels. ExpandableListView requires a maximum height in order to do measurement calculations.
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(999999, MeasureSpec.AT_MOST);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public static class SecondLevelAdapter extends BaseExpandableListAdapter {

        private Context mContext;
        private List<ForumListGroup.ForumList> mForumLists;

        public SecondLevelAdapter(Context context, List<ForumListGroup.ForumList> forumLists) {
            mContext = context;
            mForumLists = forumLists;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return mForumLists.get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return mForumLists == null ? 0 : mForumLists.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        // Two
        @Override
        public View getGroupView(final int groupPosition, final boolean isExpanded, View convertView, final ViewGroup parent) {
            final ForumListGroup.ForumList forumList = mForumLists.get(groupPosition);

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item_forum_parent, null);
            }

            // 展开 / 收缩按钮
            final ImageButton expandableButton = (ImageButton) convertView.findViewById(R.id.parent_list_item_expand_arrow);
            if (forumList.getChildItemList() == null || forumList.getChildItemList().size() == 0)
                expandableButton.setVisibility(View.INVISIBLE);
            else
                expandableButton.setVisibility(View.VISIBLE);

            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (forumList.getChildItemList() == null || forumList.getChildItemList().size() == 0) {
                        Intent intent = new Intent(mContext, ThreadListActivity.class);
                        intent.putExtra(ThreadListActivity.ACTION_TAG, "THREAD_LIST");
                        intent.putExtra(ThreadListActivity.MAIN_FORUM_TAG, mForumLists.get(groupPosition));
                        mContext.startActivity(intent);
                    }

                    if (isExpanded) {
                        ((ExpandableListView) parent).collapseGroup(groupPosition);
                        expandableButton.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_keyboard_arrow_down_black_24dp));
                    } else {
                        ((ExpandableListView) parent).expandGroup(groupPosition, true);
                        expandableButton.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_keyboard_arrow_up_black_24dp));
                    }
                }
            };

            expandableButton.setOnClickListener(onClickListener);
            convertView.setOnClickListener(onClickListener);

            // 论坛名
            TextView forumName = (TextView) convertView.findViewById(R.id.item_forum_parent_forum_name);
            forumName.setText(forumList.getForumName());

            // 论坛图片
            CircleImageView icon = (CircleImageView) convertView.findViewById(R.id.item_forum_parent_icon_round);
            Picasso.with(mContext).load(forumList.getForumIcon())
                    .into(icon);

            return convertView;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return mForumLists.get(groupPosition).getChildItemList().get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        // Three
        @Override
        public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            final ForumListGroup.SubForum subForum = mForumLists.get(groupPosition).getChildItemList().get(childPosition);

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.item_forum_parent, null);
            }

            final ImageButton expandableButton = (ImageButton) convertView.findViewById(R.id.parent_list_item_expand_arrow);
            expandableButton.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_keyboard_arrow_right_black_24dp));

            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, ThreadListActivity.class);
                    intent.putExtra(ThreadListActivity.ACTION_TAG, "THREAD_LIST");
                    intent.putExtra(ThreadListActivity.MAIN_FORUM_TAG, mForumLists.get(groupPosition));
                    intent.putExtra(ThreadListActivity.SUB_FORUM_TAG, subForum);
                    mContext.startActivity(intent);
                }
            };

            expandableButton.setOnClickListener(onClickListener);
            convertView.setOnClickListener(onClickListener);

            CircleImageView icon = (CircleImageView) convertView.findViewById(R.id.item_forum_parent_icon_round);
            icon.setVisibility(View.INVISIBLE);


            TextView subForumName = (TextView) convertView.findViewById(R.id.item_forum_parent_forum_name);
            subForumName.setText(subForum.getSubForumName());

            return convertView;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            List<ForumListGroup.SubForum> subForum = mForumLists.get(groupPosition).getChildItemList();
            return subForum == null ? 0 : subForum.size();
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}

package me.ihainan.bu.app.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 论坛组模型（一个论坛组包含多个主论坛，主论坛内部可能会包含多个子论坛）
 */
public class ForumListGroup {
    public List<ForumList> mForumLists;    // 一个论坛组
    public String mForumGroupName;

    public ForumListGroup(List<ForumList> list, String forumGroupName) {
        mForumLists = list;
        mForumGroupName = forumGroupName;
    }

    /**
     * 获取主论坛组列表
     *
     * @return 主论坛组列表
     */
    public List<ForumList> getChildItemList() {
        return mForumLists;
    }

    /**
     * 获取论坛组名
     *
     * @return 论坛组名
     */
    public String getForumGroupName() {
        return mForumGroupName;
    }

    /**
     * 主论坛
     */
    public static class ForumList implements Serializable {
        private String mForumName;
        private String mIcon;
        private Long mForumId;
        private List<SubForum> mSubForumList;

        /**
         * 获取论坛名
         *
         * @return 论坛名
         */
        public String getForumName() {
            return mForumName;
        }

        public Long getForumId() {
            return mForumId;
        }

        /**
         * 获取论坛图标
         *
         * @return 论坛图标
         */
        public String getForumIcon() {
            return mIcon;
        }

        public ForumList(String forumName, long forumId, String icon) {
            mForumName = forumName;
            mIcon = icon;
            mForumId = forumId;
            mSubForumList = new ArrayList<>();
        }

        public void addSubForum(SubForum subForum) {
            mSubForumList.add(subForum);
        }

        public void addSubForum(SubForum subForum, int index) {
            mSubForumList.add(index, subForum);
        }

        public List<SubForum> getChildItemList() {
            return mSubForumList;
        }
    }

    /**
     * 子论坛
     */
    public static class SubForum implements Serializable {
        private String mForumName;
        private Long mSubForumId;

        public SubForum(String forumName, long fid) {
            mForumName = forumName;
            mSubForumId = fid;
        }

        /**
         * 获取子论坛名
         *
         * @return 子论坛名
         */
        public String getSubForumName() {
            return mForumName;
        }

        /**
         * 获取子论坛 ID
         *
         * @return 子论坛 ID
         */
        public Long getSubForumId() {
            return mSubForumId;
        }
    }
}

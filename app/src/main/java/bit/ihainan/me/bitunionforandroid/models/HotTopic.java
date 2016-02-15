package bit.ihainan.me.bitunionforandroid.models;

import java.util.List;

/**
 * Hot Topics Model 热点话题
 */
public class HotTopic {
    public String name; // 话题名称
    public String image_url;  // 画图封面地址
    public long sum;    // 帖子总数
    public List<LatestThread> latestThreads;    // 话题帖子 (从 start 到 start + count - 1)
    public long start;  // 起始位置
    public long count;  // 当前帖子数量
}

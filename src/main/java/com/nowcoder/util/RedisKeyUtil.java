package com.nowcoder.util;

/**
 * Created by nowcoder on 2016/7/30.
 */

//专门用来生成 redis key，每个业务有自己的 key, 以防止覆盖
public class RedisKeyUtil {
    private static String SPLIT = ":";
    private static String BIZ_LIKE = "LIKE";
    private static String BIZ_DISLIKE = "DISLIKE";
    private static String BIZ_EVENTQUEUE = "EVENT_QUEUE";
    private static String BIZ_FOLLOWER = "FOLLOWER";
    private static String BIZ_FOLLOWEE = "FOLLOWEE";
    private static String BIZ_TIMELINE = "TIMELINE";



    public static String getLikeKey(int entityType, int entityId) {
        return BIZ_LIKE + SPLIT + String.valueOf(entityType) + SPLIT + String.valueOf(entityId);
    }

    public static String getDislikeKey(int entityType, int entityId) {
        return BIZ_DISLIKE + SPLIT + String.valueOf(entityType) + SPLIT + String.valueOf(entityId);
    }

    public static String getEventQueueKey() {
        return BIZ_EVENTQUEUE;
    }

    public static String getFollowerKey(int entityType, int entityId){ //得到该实体的所有关注者
        return BIZ_FOLLOWER + SPLIT + String.valueOf(entityType) + SPLIT + String.valueOf(entityId);
    }

    public static String getFolloweeKey(int userId, int entityType){ //得到该用户关注的所有entityType 为某一个值的的东西
        return BIZ_FOLLOWEE +SPLIT + String.valueOf(userId) + SPLIT + String.valueOf(entityType);

    }

    public static String getTimelineKey(int userId){
        return BIZ_FOLLOWEE +SPLIT + String.valueOf(userId) ;

    }


}

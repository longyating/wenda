package com.nowcoder.async.handler;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.async.EventHandler;
import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventType;
import com.nowcoder.model.EntityType;
import com.nowcoder.model.Feed;
import com.nowcoder.model.Question;
import com.nowcoder.model.User;
import com.nowcoder.service.FeedService;
import com.nowcoder.service.FollowService;
import com.nowcoder.service.QuestionService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.util.*;

@Component
public class FeedHandler implements EventHandler {
    @Autowired
    FeedService feedService;

    @Autowired
    UserService userService;

    @Autowired
    QuestionService questionService;

    @Autowired
    JedisAdapter jedisAdapter;

    @Autowired
    FollowService followService;

    //专门创建一个方法来得到data
    private String buildFeedData(EventModel model){
        //先把信息都存到Map 里面，最后转换成JSON
        Map<String,String> map = new HashMap<>();
        User actor = userService.getUser(model.getActorId());
        if (actor == null){
            return null;
        }
        map.put("userId",String.valueOf(actor.getId()));
        map.put("userHead",actor.getHeadUrl());
        map.put("userName",actor.getName());

        //只有是评论 和 关注（问题） 才能得到相关的问题信息。

        if(model.getEntityType() == EntityType.ENTITY_COMMENT ||(model.getType() == EventType.FOLLOW && model.getEntityType() == EntityType.ENTITY_QUESTION)){
            Question question = questionService.getQuestionById(model.getEntityId());
            map.put("questionId", String.valueOf(question.getId()));
            map.put("questionTitle", question.getTitle());
            return JSONObject.toJSONString(map);
        }

        return null;


    }


    @Override
    public void doHandle(EventModel model) {

        Feed feed = new Feed();
        feed.setUserId(model.getActorId());
        feed.setCreatedDate(new Date());
        feed.setType(model.getType().getValue());
        feed.setData(buildFeedData(model));

        if(feed.getData() == null){  //直接返回
            return;
        }
        feedService.addFeed(feed);
        //feed产生后放到数据库里面
        //拉：在controller 里获得所有关注者的动态
        //推：事件发生后，将事件推给所有的粉丝。获得粉丝列表，将feed 的Id存在 redis 里面
        List<Integer> followersId = followService.getFollowers(EntityType.ENTITY_USER, model.getActorId(), Integer.MAX_VALUE);
        followersId.add(0); //这是系统
        for (int follower : followersId){
            String timeLineKey = RedisKeyUtil.getTimelineKey(follower);
            jedisAdapter.lpush(timeLineKey, String.valueOf(feed.getId()));  //然后 controller 找这个队列就行
        }
    }


    @Override
    public List<EventType> getSupportEventTypes() {
        return Arrays.asList(new EventType[] {EventType.COMMENT, EventType.FOLLOW}); //评论了你，或者关注了你 就产生一个feed
        //return null;
    }
}

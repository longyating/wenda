package com.nowcoder.controller;

import com.nowcoder.model.EntityType;
import com.nowcoder.model.Feed;
import com.nowcoder.model.HostHolder;
import com.nowcoder.model.User;
import com.nowcoder.service.FeedService;
import com.nowcoder.service.FollowService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.JedisAdapter;
import com.nowcoder.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;


@Controller
public class FeedController {
    @Autowired
    FeedService feedService;

    @Autowired
    UserService userService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    FollowService followService;

    @Autowired
    JedisAdapter jedisAdapter;

    //推: 如果是登陆的状态，从redis 数据库里面找到
    @RequestMapping(path = {"/pushfeeds"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String getPushFeeds(Model model){
        int localUserId = hostHolder.getUser() == null ? 0 : hostHolder.getUser().getId();
        List<String> feedsId = jedisAdapter.lrange(RedisKeyUtil.getTimelineKey(localUserId),0,10);
        List<Feed> feeds = new ArrayList<>();
        for(String feedId : feedsId){
            Feed feed = feedService.getFeedById(Integer.parseInt(feedId));
            if(feed != null){
                feeds.add(feed);
            }
        }

        model.addAttribute("feeds",feeds);
        return "feeds";

    }

    //拉: 如果是登陆的状态，得到关注的人的列表，从数据库得到我关注的人的feed
    @RequestMapping(path = {"/pullfeeds"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String getPullFeeds(Model model){
        int localUserId = hostHolder.getUser() == null ? 0 : hostHolder.getUser().getId();
        List<Integer> followeesId = new ArrayList<>();
        if(localUserId != 0){
           followeesId = followService.getFollowees(localUserId, EntityType.ENTITY_USER, Integer.MAX_VALUE);

        }
        List<Feed> feeds = feedService.selectUserFeeds(Integer.MAX_VALUE, followeesId, 10);  // 从头开始找。所有的都找出来，先取10个
        model.addAttribute("feeds",feeds);
        return "feeds";

    }
}

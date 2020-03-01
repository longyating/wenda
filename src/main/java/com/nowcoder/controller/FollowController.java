package com.nowcoder.controller;

import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventProducer;
import com.nowcoder.async.EventType;
import com.nowcoder.model.*;
import com.nowcoder.service.CommentService;
import com.nowcoder.service.FollowService;
import com.nowcoder.service.QuestionService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.WendaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class FollowController {
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
    @Autowired
    CommentService commentService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    QuestionService questionService;

    @Autowired
    FollowService followService;

    @Autowired
    EventProducer eventProducer;

    @Autowired
    UserService userService;


    //关注一个用户
    @RequestMapping(path = {"/followUser"}, method = {RequestMethod.POST})
    @ResponseBody
    public String followUser(@RequestParam("userId") int userId){
        if(hostHolder.getUser() == null){
            return WendaUtil.getJSONString(999);
        }

        boolean ret = followService.follow(hostHolder.getUser().getId(), EntityType.ENTITY_USER, userId);  //true or false
        int actorId = hostHolder.getUser().getId();

        EventModel model = new EventModel(EventType.FOLLOW)
                .setActorId(actorId)
                .setEntityId(userId)
                .setEntityType(EntityType.ENTITY_USER).setEntityOwnerId(userId);
        eventProducer.fireEvent(model);
        /*
        eventProducer.fireEvent(new EventModel(EventType.FOLLOW)
                .setActorId(actorId)
                .setEntityId(userId)
                .setEntityType(EntityType.ENTITY_USER).setEntityOwnerId(userId));
*/
        //如果ret返回了true 则成功，返回字符串0 ，并返回我关注的人的个数
        return WendaUtil.getJSONString(ret ?0 :1, String.valueOf(followService.getFolloweeCount(hostHolder.getUser().getId(), EntityType.ENTITY_USER) ));
    }

    //取消关注一个用户
    @RequestMapping(path = {"/unfollowUser"}, method = {RequestMethod.POST})
    @ResponseBody
    public String unfollowUser(@RequestParam("userId") int userId){
        if(hostHolder.getUser() == null){
            return WendaUtil.getJSONString(999);
        }

        boolean ret = followService.unfollow(hostHolder.getUser().getId(), EntityType.ENTITY_USER, userId);  //true or false
        eventProducer.fireEvent(new EventModel(EventType.UNFOLLOW).setActorId(hostHolder.getUser().getId()).setEntityType(EntityType.ENTITY_USER).setEntityId(userId).setEntityOwnerId(userId));

        //如果ret返回了true 则成功，返回字符串0 ，并返回关注的人的个数
        return WendaUtil.getJSONString(ret ?0 :1, String.valueOf(followService.getFolloweeCount(hostHolder.getUser().getId(), EntityType.ENTITY_USER) ));
    }

    //关注一个问题
    @RequestMapping(path = {"/followQuestion"}, method = {RequestMethod.POST})
    @ResponseBody
    public String followQuestion(@RequestParam("questionId") int questionId){
        if(hostHolder.getUser() == null){
            return WendaUtil.getJSONString(999);
        }
        Question question = questionService.getQuestionById(questionId);
        if(question == null){
            return WendaUtil.getJSONString(1, "这个问题不存在");
        }

        boolean ret = followService.follow(hostHolder.getUser().getId(), EntityType.ENTITY_QUESTION, questionId);  //true or false
        eventProducer.fireEvent(new EventModel(EventType.FOLLOW)
                .setActorId(hostHolder.getUser().getId()).setEntityId(questionId)
                .setEntityType(EntityType.ENTITY_QUESTION).setEntityOwnerId(question.getUserId()));
        //如果ret返回了true 则成功，返回字符串0 ，并返回关注这个问题的人的一些信息，以及多少人关注这个问题。
        Map<String, Object> info = new HashMap<>();
        info.put("headUrl", hostHolder.getUser().getHeadUrl());
        info.put("name", hostHolder.getUser().getName());
        info.put("id",hostHolder.getUser().getId());
        info.put("count", followService.getFollowerCount(EntityType.ENTITY_QUESTION,questionId));
        return WendaUtil.getJSONString(ret ? 0 : 1, info);
}


    //取消关注一个问题
    @RequestMapping(path = {"/unfollowQuestion"}, method = {RequestMethod.POST})
    @ResponseBody
    public String unfollowQuestion(@RequestParam("questionId") int questionId){
        if(hostHolder.getUser() == null){
            return WendaUtil.getJSONString(999);
        }
        Question question = questionService.getQuestionById(questionId);
        if(question == null){
            return WendaUtil.getJSONString(1, "这个问题不存在");
        }


        boolean ret = followService.unfollow(hostHolder.getUser().getId(), EntityType.ENTITY_QUESTION, questionId);  //true or false
        eventProducer.fireEvent(new EventModel(EventType.UNFOLLOW).setActorId(hostHolder.getUser().getId()).setEntityType(EntityType.ENTITY_QUESTION).setEntityId(questionId).setEntityOwnerId(question.getUserId()));

        //如果ret返回了true 则成功，返回字符串0 ，并返回关注这个问题的人的一些信息，以及多少人关注这个问题。
        Map<String, Object> info = new HashMap<>();
        info.put("id",hostHolder.getUser().getId());
        info.put("count", followService.getFollowerCount(EntityType.ENTITY_QUESTION,questionId));
        return WendaUtil.getJSONString(ret ? 0 : 1, info);
    }

    //实现一个网页，用来展示粉丝列表,粉丝是人
    @RequestMapping(path = {"/user/{uid}/followers"}, method = {RequestMethod.GET})
    public String followers(Model model, @PathVariable("uid") int userId){
        List<Integer> followerIds = followService.getFollowers(EntityType.ENTITY_USER, userId, 0,10);

        if(hostHolder.getUser() != null){
            model.addAttribute("followers", getUsersInfo(hostHolder.getUser().getId(), followerIds));
        }else{
            model.addAttribute("followers",getUsersInfo(0, followerIds));
        }
        model.addAttribute("followerCount",followService.getFollowerCount(EntityType.ENTITY_USER, userId));
        model.addAttribute("curUser",userService.getUser(userId));

        return "followers"; //网页

    }

    //实现一个网页，用来展示我关注的列表，只考虑EntityType是人。不包括评论和问题。
    @RequestMapping(path = {"/user/{uid}/followees"}, method = {RequestMethod.GET})
    public String followees(Model model, @PathVariable("uid") int userId){
        List<Integer> followerIds = followService.getFollowees(userId, EntityType.ENTITY_USER, 0, 10);
        if(hostHolder.getUser() != null){
            model.addAttribute("followees", getUsersInfo(hostHolder.getUser().getId(), followerIds));
        }else{
            model.addAttribute("followees",getUsersInfo(0, followerIds));
        }
        model.addAttribute("followeeCount",followService.getFolloweeCount(userId,EntityType.ENTITY_USER));
        model.addAttribute("curUser",userService.getUser(userId));

        return "followees"; //网页


    }

    //这个方法被上面这两个方法利用，用于得到这个用户列表的信息，比如
    private List<ViewObject> getUsersInfo(int localUser, List<Integer> userIds){
        List<ViewObject> usersInfo = new ArrayList<>();
        for (Integer uid : userIds){
            ViewObject vo = new ViewObject();
            User user = userService.getUser(uid);
            vo.set("user",user);
            //vo.set("commentCount",commentService.);
            vo.set("followerCount",followService.getFollowerCount(EntityType.ENTITY_USER, uid));
            vo.set("followeeCount", followService.getFolloweeCount(uid, EntityType.ENTITY_USER));
            vo.set("commentCount", commentService.getUserCommentCount(uid));
            if (localUser != 0){  //是否互相关注
                vo.set("followed", followService.isFollower(localUser,EntityType.ENTITY_USER,uid));
            }else{
                vo.set("followed", false);
            }
            usersInfo.add(vo);

        }
        return usersInfo;


    }


}


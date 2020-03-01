package com.nowcoder.controller;

import com.nowcoder.model.*;
import com.nowcoder.service.MessageService;
import com.nowcoder.service.UserService;
import com.nowcoder.util.WendaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class MessageController {
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
    @Autowired
    HostHolder hostHolder;

    @Autowired
    UserService userService;

    @Autowired
    MessageService messageService;

    @RequestMapping(path = {"/msg/list"},method = {RequestMethod.GET})
    public String getConversationList(Model model) {
        try{
            User localUser = hostHolder.getUser();
            List<Message> messageList = messageService.getConversationList(localUser.getId(),0,10);
            List<ViewObject> vos = new ArrayList<>();

            for(Message message : messageList){
                ViewObject vo = new ViewObject();
                vo.set("message",message);
                int targetId = (message.getFromId() == localUser.getId()? message.getToId() : message.getFromId());
                vo.set("user",userService.getUser(targetId));
                vo.set("unread",messageService.getConversationUnreadCount(localUser.getId(),message.getConversationId()));
                vos.add(vo);
            }
            model.addAttribute("conversations",vos);

        }catch(Exception e){
            logger.error("获取消息列表失败"+e.getMessage());
        }
        return "letter";
    }

    @RequestMapping(path = {"/msg/detail"},method = {RequestMethod.GET})
    public String getConversationDetail(Model model, @RequestParam("conversationId") String conversationId){
        try{
            List<Message> messageList = messageService.getConversationDetail(conversationId, 0 ,10);
            List<ViewObject> vos = new ArrayList<>();

            for (Message message : messageList){
                ViewObject vo = new ViewObject();
                vo.set("message",message);
                vo.set("user",userService.getUser(message.getFromId()));

                vos.add(vo);
            }

            model.addAttribute("messages",vos);
        }catch(Exception e){
            logger.error("获取详情失败"+ e.getMessage());
        }
        return "letterDetail";
    }

    @RequestMapping(path = {"/msg/addMessage"}, method = {RequestMethod.POST})
    @ResponseBody  //弹窗以json返回 所以写ResponseBody
    public String addComment(@RequestParam("toName") String toName,
                             @RequestParam("content") String content){
        try{
            Message message = new Message();
            if( hostHolder.getUser() == null){
                return WendaUtil.getJSONString(999,"未登录");}


            User user =  userService.selectByName(toName);
            if (user == null){
                return WendaUtil.getJSONString(1,"用户不存在");

            }
            message.setFromId(hostHolder.getUser().getId());
            message.setToId(user.getId());
            message.setContent(content);
            message.setCreatedDate(new Date());

            messageService.addMessage(message);
            return WendaUtil.getJSONString(0); //0表示成功


        }catch(Exception e){
            logger.error("增加站内信失败" + e.getMessage());
            return WendaUtil.getJSONString(1,"增加站内信失败了");
        }



    }


}

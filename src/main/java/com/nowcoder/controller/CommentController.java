package com.nowcoder.controller;

import com.nowcoder.async.EventHandler;
import com.nowcoder.async.EventModel;
import com.nowcoder.async.EventProducer;
import com.nowcoder.async.EventType;
import com.nowcoder.model.Comment;
import com.nowcoder.model.EntityType;
import com.nowcoder.model.HostHolder;
import com.nowcoder.model.Question;
import com.nowcoder.service.CommentService;
import com.nowcoder.service.QuestionService;
import com.nowcoder.util.WendaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@Controller
public class CommentController {
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
    @Autowired
    CommentService commentService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    QuestionService questionService;

    @Autowired
    EventProducer eventProducer;




    @RequestMapping(path = {"/addComment"}, method = {RequestMethod.POST})
    public String addComment(@RequestParam("questionId") int questionId,
                             @RequestParam("content") String content
                             ){
        try{
            Comment comment = new Comment();
            comment.setContent(content);
            comment.setCreatedDate(new Date());
            comment.setEntityId(questionId);
            comment.setEntityType(EntityType.ENTITY_QUESTION);
            comment.setStatus(0);

            if(hostHolder.getUser() != null){
                comment.setUserId(hostHolder.getUser().getId());
            }else{
                comment.setUserId(WendaUtil.ANONYMOUS_USERID);
            }

            commentService.addComment(comment);

            Question question = questionService.getQuestionById(questionId);
            int userId = question.getUserId();

            eventProducer.fireEvent(new EventModel(EventType.COMMENT)
                    .setActorId(hostHolder.getUser().getId())
                    .setEntityType(EntityType.ENTITY_QUESTION)
                    .setEntityId(questionId)
                    .setEntityOwnerId(userId)  //评论的谁的问题
                    .setExt("questionId", String.valueOf(comment.getEntityId()))
                    .setExt("questionTitle", question.getTitle())
                    .setExt("commentId",String.valueOf(comment.getId())));

            //更新评论里的数量
            int count = commentService.getCommentCount(comment.getEntityId(), comment.getEntityType());
            //System.out.println(count);
            questionService.updateCommentCount(comment.getEntityId(), count);

        }catch (Exception e){
            logger.error("增加评论失败" + e.getMessage());

        }
        return "redirect:/question/" + questionId;

    }

}

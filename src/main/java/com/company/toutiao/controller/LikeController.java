package com.company.toutiao.controller;

import com.company.toutiao.async.EventModel;
import com.company.toutiao.async.EventProducer;
import com.company.toutiao.async.EventType;
import com.company.toutiao.model.Comment;
import com.company.toutiao.model.EntityType;
import com.company.toutiao.model.HostHolder;
import com.company.toutiao.service.CommentService;
import com.company.toutiao.service.LikeService;
import com.company.toutiao.utils.WendaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class LikeController {

    @Autowired
    LikeService likeService;

    @Autowired
    HostHolder hostHolder;

    @Autowired
    CommentService commentService;

    @Autowired
    EventProducer eventProducer;

    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(@RequestParam("commentId") int commentId) {
        if (hostHolder.getUser() == null) {
            return WendaUtil.getJSONString(999);
        }

        Comment comment = commentService.getCommentById(commentId);
        //事件生产者将点赞事件添加到队列中
        eventProducer.fireEvent(new EventModel(EventType.LIKE).
                setActorId(hostHolder.getUser().getId()).
                setEntityId(commentId).
                setEntityType(EntityType.ENTITY_COMMENT).
                setExt("questionId", String.valueOf(comment.getEntityId())).
                setEntityOwnerId(comment.getUserId()));
        long likeCount = likeService.like(hostHolder.getUser().getId(), EntityType.ENTITY_COMMENT, commentId);
        return WendaUtil.getJSONString(0, String.valueOf(likeCount));
    }

    @RequestMapping(path = "/dislike", method = RequestMethod.POST)
    @ResponseBody
    public String dislike(@RequestParam("commentId") int commentId) {
        if (hostHolder.getUser() == null) {
            return WendaUtil.getJSONString(999);
        }
        long likeCount = likeService.disLike(hostHolder.getUser().getId(), EntityType.ENTITY_COMMENT, commentId);
        return WendaUtil.getJSONString(0, String.valueOf(likeCount));
    }
}

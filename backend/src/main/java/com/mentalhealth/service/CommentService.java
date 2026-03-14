package com.mentalhealth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mentalhealth.entity.Comment;

import java.util.List;

public interface CommentService extends IService<Comment> {

    /**
     * 发表评论（包含敏感词过滤）
     *
     * @param comment 评论实体
     */
    boolean publishComment(Comment comment);

    /**
     * 获取指定树洞的所有评论列表（附带用户脱敏信息）
     *
     * @param postId 树洞ID
     */
    List<Comment> getCommentsByPostId(Long postId);

    /**
     * 获取指定树洞的评论列表（分页，附带用户脱敏信息）
     */
    Page<Comment> getCommentsByPostIdPaged(Long postId, int pageNum, int pageSize);

    /**
     * 评论点赞
     *
     * @param commentId 评论ID
     */
    boolean likeComment(Long commentId);
}

package com.mentalhealth.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mentalhealth.entity.Comment;
import com.mentalhealth.entity.Post;
import com.mentalhealth.entity.Report;
import com.mentalhealth.mapper.ReportMapper;
import com.mentalhealth.service.CommentService;
import com.mentalhealth.service.PostService;
import com.mentalhealth.service.ReportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class ReportServiceImpl extends ServiceImpl<ReportMapper, Report> implements ReportService {

    @Resource
    private PostService postService;
    @Resource
    private CommentService commentService;

    @Override
    public boolean submitReport(Report report) {
        if (StrUtil.isBlank(report.getTargetType()) || report.getTargetId() == null || StrUtil.isBlank(report.getReason())) {
            throw new RuntimeException("举报信息不完整");
        }
        
        // 校验目标是否存在
        if ("POST".equalsIgnoreCase(report.getTargetType())) {
            Post post = postService.getById(report.getTargetId());
            if (post == null) throw new RuntimeException("举报内容不存在");
        } else if ("COMMENT".equalsIgnoreCase(report.getTargetType())) {
            Comment comment = commentService.getById(report.getTargetId());
            if (comment == null) throw new RuntimeException("举报评论不存在");
        } else {
            throw new RuntimeException("不支持的举报类型");
        }

        report.setStatus("UNPROCESSED");
        return this.save(report);
    }

    @Override
    public Page<Report> getReportList(int pageNum, int pageSize, String status) {
        Page<Report> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(status)) {
            wrapper.eq(Report::getStatus, status);
        }
        wrapper.orderByDesc(Report::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean processReport(Long id, String action) {
        Report report = this.getById(id);
        if (report == null) throw new RuntimeException("举报记录不存在");
        if (!"UNPROCESSED".equals(report.getStatus())) throw new RuntimeException("该举报已被处理");

        if ("PROCESS".equalsIgnoreCase(action)) {
            // 屏蔽关联内容
            report.setStatus("PROCESSED");
            if ("POST".equalsIgnoreCase(report.getTargetType())) {
                Post post = postService.getById(report.getTargetId());
                if (post != null) {
                    post.setStatus(0); // 屏蔽
                    postService.updateById(post);
                }
            } else if ("COMMENT".equalsIgnoreCase(report.getTargetType())) {
                Comment comment = commentService.getById(report.getTargetId());
                if (comment != null) {
                    comment.setStatus(0); // 屏蔽
                    commentService.updateById(comment);
                }
            }
        } else if ("REJECT".equalsIgnoreCase(action)) {
            report.setStatus("REJECTED");
        } else {
            throw new RuntimeException("未知的处理动作");
        }

        return this.updateById(report);
    }
}

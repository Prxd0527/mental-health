package com.mentalhealth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mentalhealth.common.Result;
import com.mentalhealth.entity.Report;
import com.mentalhealth.service.ReportService;
import com.mentalhealth.utils.SecurityUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Resource
    private ReportService reportService;

    /**
     * 用户提交举报
     */
    @PostMapping("/submit")
    public Result<String> submitReport(@RequestBody Report report) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Result.error(401, "请先登录");
        }
        report.setReporterId(userId);
        reportService.submitReport(report);
        return Result.success("举报已提交，感谢您的监督");
    }

    /**
     * 管理员获取举报列表
     */
    @GetMapping("/admin/list")
    public Result<Page<Report>> getReportList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String status) {
        
        if (!SecurityUtils.isAdmin()) {
            return Result.error(403, "无权限执行此操作");
        }
        Page<Report> page = reportService.getReportList(pageNum, pageSize, status);
        return Result.success(page);
    }

    /**
     * 管理员处理举报
     * @param action PROCESS-处理并屏蔽内容, REJECT-驳回(忽略)
     */
    @PostMapping("/admin/{id}/process")
    public Result<String> processReport(@PathVariable Long id, @RequestParam String action) {
        if (!SecurityUtils.isAdmin()) {
            return Result.error(403, "无权限执行此操作");
        }
        reportService.processReport(id, action);
        return Result.success("处理成功");
    }
}

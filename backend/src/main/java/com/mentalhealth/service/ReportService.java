package com.mentalhealth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mentalhealth.entity.Report;

public interface ReportService extends IService<Report> {
    boolean submitReport(Report report);
    Page<Report> getReportList(int pageNum, int pageSize, String status);
    boolean processReport(Long id, String action);
}

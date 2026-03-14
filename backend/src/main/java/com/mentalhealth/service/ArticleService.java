package com.mentalhealth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mentalhealth.entity.Article;

public interface ArticleService extends IService<Article> {

    /**
     * 分页查询科普文章列表（支持根据标题关键字检索）
     *
     * @param pageNum  当前页
     * @param pageSize 每页条数
     * @param keyword  检索关键字（可为 null）
     * @return 分页结果
     */
    Page<Article> getArticlePage(int pageNum, int pageSize, String keyword);

    /**
     * 获取文章详情（自动增加阅读量）
     */
    Article getArticleDetail(Long id);
}

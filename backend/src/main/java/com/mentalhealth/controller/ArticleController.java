package com.mentalhealth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mentalhealth.common.Result;
import com.mentalhealth.entity.Article;
import com.mentalhealth.service.ArticleService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/article")
public class ArticleController {

    @Resource
    private ArticleService articleService;

    @GetMapping("/list")
    public Result<Page<Article>> getList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {

        Page<Article> page = articleService.getArticlePage(pageNum, pageSize, keyword);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<Article> getDetail(@PathVariable Long id) {
        Article article = articleService.getArticleDetail(id);
        return Result.success(article);
    }
}

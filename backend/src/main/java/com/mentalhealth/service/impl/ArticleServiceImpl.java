package com.mentalhealth.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mentalhealth.entity.Article;
import com.mentalhealth.mapper.ArticleMapper;
import com.mentalhealth.service.ArticleService;
import org.springframework.stereotype.Service;

@Service
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    @Override
    public Page<Article> getArticlePage(int pageNum, int pageSize, String keyword) {
        Page<Article> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        // 仅展示已发布的文章
        wrapper.eq(Article::getStatus, 1);

        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(Article::getTitle, keyword);
        }

        // 按创建时间倒序
        wrapper.orderByDesc(Article::getCreateTime);

        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public Article getArticleDetail(Long id) {
        Article article = baseMapper.selectById(id);
        if (article != null && article.getStatus() == 1) {
            // 增加阅读量
            article.setViews(article.getViews() + 1);
            baseMapper.updateById(article);
            return article;
        }
        throw new RuntimeException("文章不存在或已下架");
    }
}

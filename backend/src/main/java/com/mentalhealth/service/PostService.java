package com.mentalhealth.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mentalhealth.entity.Post;

public interface PostService extends IService<Post> {

    /**
     * 发布树洞
     * 
     * @param post 树洞实体
     * @return true 成功, false 失败
     */
    boolean publishPost(Post post);

    /**
     * 分页查询树洞广场列表
     * 
     * @param pageNum  当前页
     * @param pageSize 每页条数
     * @param sortBy   排序方式: latest-最新, hot-最热
     * @param tags     按标签过滤
     * @return 包含脱敏用户信息的树洞分页数据
     */
    Page<Post> getPostSquare(int pageNum, int pageSize, String sortBy, String tags);

    /**
     * 树洞点赞
     */
    boolean likePost(Long postId);

    /**
     * 获取单一树洞详情及部分信息
     */
    Post getPostDetail(Long postId);
}
